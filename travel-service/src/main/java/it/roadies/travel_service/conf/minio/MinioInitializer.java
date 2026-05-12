package it.roadies.travel_service.conf.minio;

import io.minio.*;
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

    @PostConstruct
    public void init() {
        try {
            boolean travelBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(travelBucket).build());
            if (!travelBucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(travelBucket).build());
            }
            setPublicReadOnlyPolicy(travelBucket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPublicReadOnlyPolicy(String bucketName) throws Exception {
        String policyJson = "{\n" +
                "    \"Version\": \"2012-10-17\",\n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": \"s3:GetObject\",\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": \"*\",\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policyJson)
                        .build()
        );
    }
}
