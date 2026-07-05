package it.roadies.user_service.services.impl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.roadies.shared.i18n.MessageLang;
import it.roadies.user_service.exception.StorageException;
import io.minio.Http.Method;
import it.roadies.user_service.services.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements MinioService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "application/pdf"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final MinioClient minioClient;
    private final MessageLang messageLang;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(messageLang.getMessage("minio.image.big"));
        }
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(messageLang.getMessage("error.type.not.allowed") + contentType);
        }

        try {

            String extension = getFileExtension(file.getOriginalFilename());
            if (extension.isEmpty()) {
                if ("application/pdf".equals(contentType)) {
                    extension = ".pdf";
                } else if ("image/jpeg".equals(contentType)) {
                    extension = ".jpg";
                } else if ("image/png".equals(contentType)) {
                    extension = ".png";
                }
            }
            String fileName = UUID.randomUUID() + extension;

            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );

            return fileName;

        } catch (Exception e) {
            log.error("Errore durante il caricamento del file su MinIO: {}", e.getMessage());
            throw new StorageException(messageLang.getMessage("error.image.minio.upload"));
        }
    }

    @Override
    public void deleteFile(String fileName, String bucketName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File rimosso da MinIO con successo dal bucket {}: {}", bucketName, fileName);
        } catch (Exception e) {
            log.error("Impossibile eliminare il file {} dal bucket {}: {}", fileName, bucketName, e.getMessage());
            throw new StorageException(messageLang.getMessage("error.minio.delete"));
        }
    }

    @Override
    public String getPublicUrl(String fileName, String bucketName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        return minioUrl + "/" + bucketName + "/" + fileName;
    }

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Override
    public String generatePresignedUrl(String fileName, String bucketName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        try {
            // Genera il client per i presigned url sostituendo localhost con 10.0.2.2
            // In questo modo la firma AWS viene calcolata per l'host 10.0.2.2 (utilizzato dall'emulatore Android),
            // evitando l'errore HTTP 403 SignatureDoesNotMatch quando l'app rimpiazza localhost con 10.0.2.2.
            String presignedEndpoint = minioUrl.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2");
            MinioClient presignedClient = MinioClient.builder()
                    .endpoint(presignedEndpoint)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1") // Evita il discovery della region che causa Connection Timeout
                    .build();

            String url = presignedClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );
            log.info("Generato Presigned URL per {}: {}", fileName, url);
            return url;
        } catch (Exception e) {
            log.error("Errore nella generazione del Presigned URL per {}: {}", fileName, e.getMessage());
            throw new StorageException(messageLang.getMessage("error.access.private.image"));
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}