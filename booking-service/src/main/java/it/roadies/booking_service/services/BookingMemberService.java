package it.roadies.booking_service.services;

import io.minio.errors.MinioException;
import it.roadies.booking_service.data.dto.request.MemberDocumentUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BookingMemberService {
    public void updateDocument (MemberDocumentUpdateRequest memberDocument);
    public void acceptDocument (MemberDocumentUpdateRequest memberDocument);
    public void rejectDocument (MemberDocumentUpdateRequest memberDocument);

    String uploadDocumentPhoto(UUID documentId, MultipartFile file) throws MinioException;
}
