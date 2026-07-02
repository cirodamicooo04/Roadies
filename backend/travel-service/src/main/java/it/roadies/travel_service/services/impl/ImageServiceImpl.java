package it.roadies.travel_service.services.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import it.roadies.travel_service.conf.i8n.MessageLang;
import it.roadies.travel_service.data.dao.ImageRepository;
import it.roadies.travel_service.data.dto.response.ImageResponse;
import it.roadies.travel_service.data.entity.Image;
import it.roadies.travel_service.data.entity.enumerations.ImageStatus;
import it.roadies.travel_service.services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;
    private final ImageRepository imageRepository;
    private final MessageLang messageLang;
    private final Tika tika = new Tika();

    @Value("${minio.travelBucket}")
    private String travelBucket;

    @Value("${minio.url}")
    private String minioUrl;

    private final List<String> allowedImageTypes = List.of("image/jpeg", "image/png", "image/webp");

    @Override
    public void deleteImageFromMinio(String path) {
        try {
            minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                    .bucket(travelBucket)
                    .object(path)
                    .build());
        } catch (Exception e) {
            log.error("Error deleting image from MinIO: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public ImageResponse uploadImage(MultipartFile file, String ownerId) {
        try {
            try (InputStream is = file.getInputStream()) {
                String mimeType = tika.detect(is);
                log.info("Detected file type: {}", mimeType);

                if (!allowedImageTypes.contains(mimeType)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.type.not.allowed"));
                }
            }

            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename().replace(" ", "_");

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(travelBucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );

            String fileUrl = minioUrl + "/" + travelBucket + "/" + filename;

            Image image = new Image();
            image.setPath(filename);
            image.setUrl(fileUrl);
            image.setStatus(ImageStatus.TEMPORARY);
            image.setTravel(null);
            image.setOwnerId(ownerId);

            Image savedImage = imageRepository.save(image);

            ImageResponse response = new ImageResponse();
            response.setId(savedImage.getId());
            response.setUrl(savedImage.getUrl());

            return response;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messageLang.getMessage("error.image.minio.upload"));
        }

    }
}
