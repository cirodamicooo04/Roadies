package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.UserDocument;
import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.repositories.UserDocumentRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.mappers.UserDocumentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDocumentService {

    private final UserDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final UserDocumentMapper userDocumentMapper;

    @Transactional
    public UserDocumentResponseDTO uploadDocument(String userId, UserDocumentRequestDTO dto){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));

    UserDocument doc= userDocumentMapper.toEntity(dto);
    doc.setUserId(user);

    UserDocument saved = documentRepository.save(doc);
    return userDocumentMapper.toDto(saved);
    }

    public List<UserDocumentResponseDTO> getUserDocuments(String userId){

        List<UserDocument> docs = documentRepository.findByUserId(userId);
        return userDocumentMapper.toDtoList(docs);

    }

    @Transactional
    public UserDocumentResponseDTO verifyDocument(UUID docId, boolean approved, String reason){
        UserDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Documento non trovato"));

        if(approved){
            doc.setStatus(DocumentStatus.VERIFIED);
            doc.setRejectionReason(null);
            doc.setVerifiedAt(LocalDateTime.now());
        }
        else{
            doc.setStatus(DocumentStatus.REJECTED);
            doc.setRejectionReason(reason);
            doc.setVerifiedAt(null);
        }

        return userDocumentMapper.toDto(documentRepository.save(doc));

    }
    @Transactional
    public void deleteDocument(UUID docId) {
        if (!documentRepository.existsById(docId)) {
            throw new RuntimeException("Impossibile eliminare: documento non trovato");
        }
        documentRepository.deleteById(docId);
    }
}
