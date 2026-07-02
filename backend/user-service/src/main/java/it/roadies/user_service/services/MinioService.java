package it.roadies.user_service.services;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadFile(MultipartFile file, String bucketName);

    void deleteFile(String fileName, String bucketName);

    //Utilizzato per ottenere il link pubblico per accedere agli avatar
    String getPublicUrl(String fileName, String bucketName);

    //Utlizzato per generare un link privato per accedere ai documenti
    String generatePresignedUrl(String fileName, String bucketName);
}