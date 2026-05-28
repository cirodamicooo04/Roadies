package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserDocumentService {
    UserDocumentResponseDTO uploadDocument(String userId, UserDocumentRequestDTO dto, MultipartFile file);
    List<UserDocumentResponseDTO> getUserDocuments(String userId);
    UserDocumentResponseDTO verifyDocument(UUID docId, boolean approved, String reason);
    void deleteDocument(UUID docId,String userId);
}