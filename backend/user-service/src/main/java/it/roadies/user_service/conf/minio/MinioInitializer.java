package it.roadies.user_service.conf.minio;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer {
    private final MinioClient minioClient;

    @Value("${minio.documentBucket}")
    private String documentBucket;
    @Value("${minio.avatarBucket}")
    private String avatarBucket;

    @PostConstruct
    public void init() {
        try {
            documentBucket = documentBucket.trim();
            avatarBucket = avatarBucket.trim();

            boolean documentBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(documentBucket).build());
            boolean avatarBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(avatarBucket).build());

            if (!documentBucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(documentBucket).build());
            }

            if (!avatarBucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(avatarBucket).build());

                //Policy pubblica di sola lettura
                String policyJson = "{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "      \"Action\": [\"s3:GetObject\"],\n" +
                        "      \"Resource\": [\"arn:aws:s3:::" + avatarBucket + "/*\"]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(avatarBucket)
                                .config(policyJson)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'inizializzazione dei bucket MinIO", e);
        }
    }
}