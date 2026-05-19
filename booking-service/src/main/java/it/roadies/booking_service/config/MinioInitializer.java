package it.roadies.booking_service.config;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer {
    private final MinioClient minioClient;

    @Value("${minio.bookingBucket}")
    private String bookingBucket;

    @PostConstruct
    public void init() {
        try {
            boolean bookingBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bookingBucket).build());
            if (!bookingBucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bookingBucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
