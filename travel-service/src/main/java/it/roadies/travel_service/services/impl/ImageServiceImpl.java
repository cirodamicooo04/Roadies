package it.roadies.travel_service.services.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import it.roadies.travel_service.data.dao.ImageRepository;
import it.roadies.travel_service.data.dto.response.ImageResponse;
import it.roadies.travel_service.data.entity.Image;
import it.roadies.travel_service.data.entity.enumerations.ImageStatus;
import it.roadies.travel_service.services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;
    private final ImageRepository imageRepository;

    @Value("${minio.travelBucket}")
    private String travelBucket;

    @Value("${minio.url}")
    private String minioUrl;

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

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading image to MinIO");
        }

    }
}
