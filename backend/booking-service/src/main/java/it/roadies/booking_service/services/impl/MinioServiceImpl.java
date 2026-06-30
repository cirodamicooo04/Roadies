package it.roadies.booking_service.services.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.roadies.booking_service.config.i8n.MessageLang;
import it.roadies.booking_service.exceptions.StorageException;
import it.roadies.booking_service.services.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MessageLang messageLang;
    private final Tika tika = new Tika();

    private final List<String> allowedImageTypes = List.of("image/jpeg", "image/png", "image/webp");

    @Value("${minio.bookingBucket}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public String uploadFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            String mimeType = tika.detect(is);
            log.info("Detected file type: {}", mimeType);

            if (!allowedImageTypes.contains(mimeType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.type.not.allowed"));
            }
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), (long) -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException(messageLang.getMessage("error.minio"));
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/")) {
            return;
        }
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File rimosso da MinIO con successo: {}", fileName);
        } catch (Exception e) {
            log.error("Impossibile eliminare il file da MinIO: {}", e.getMessage());
            throw new StorageException(messageLang.getMessage("error.minio.delete"));
        }
    }
}