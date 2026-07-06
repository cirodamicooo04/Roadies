package it.roadies.booking_service.services;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BookingMemberService {

    String uploadDocumentPhoto(UUID documentId, MultipartFile file, String userJwt) throws MinioException;
}
