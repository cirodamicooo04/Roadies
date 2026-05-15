package it.roadies.booking_service.services;

import io.minio.errors.MinioException;
import it.roadies.booking_service.data.dto.request.MemberDocumentUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BookingMemberService {
//    public void updateDocument (MemberDocumentUpdateRequest memberDocument);
     void acceptDocument (MemberDocumentUpdateRequest memberDocument);
     void rejectDocument (MemberDocumentUpdateRequest memberDocument);

    String uploadDocumentPhoto(UUID documentId, MultipartFile file, String userJwt) throws MinioException;
}
