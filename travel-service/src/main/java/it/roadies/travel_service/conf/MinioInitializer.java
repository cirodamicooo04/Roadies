package it.roadies.travel_service.conf;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer {
    private final MinioClient minioClient;

    @Value("${minio.travelBucket}")
    private String travelBucket;

    @Value("${minio.activityBucket}")
    private String activityBucket;

    @PostConstruct
    public void init() {
        try {
            boolean travelBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(travelBucket).build());
            boolean activityBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(activityBucket).build());
            if (!travelBucketExists) { minioClient.makeBucket(MakeBucketArgs.builder().bucket(travelBucket).build());}
            if (!activityBucketExists) { minioClient.makeBucket(MakeBucketArgs.builder().bucket(activityBucket).build());}
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
