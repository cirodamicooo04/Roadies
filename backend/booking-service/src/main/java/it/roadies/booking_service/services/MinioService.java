package it.roadies.booking_service.services;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    String uploadFile(MultipartFile m) throws MinioException;
    public void deleteFileByUrl(String fileUrl);
}
