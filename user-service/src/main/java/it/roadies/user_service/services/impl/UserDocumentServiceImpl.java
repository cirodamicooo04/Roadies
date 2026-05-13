package it.roadies.user_service.services.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.UserDocument;
import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.repositories.UserDocumentRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.mappers.UserDocumentMapper;
import it.roadies.user_service.services.UserDocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDocumentServiceImpl implements UserDocumentService {

    private final UserDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final UserDocumentMapper userDocumentMapper;
    private final MinioClient minioClient;

    @Value("${minio.documentBucket:user-documents}")
    private String documentBucket;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    @Transactional
    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    public UserDocumentResponseDTO uploadDocument(String userId, UserDocumentRequestDTO dto, MultipartFile file){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));

        String fileUrl;
        try {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename().replace(" ", "_");

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(documentBucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );

            fileUrl = minioUrl + "/" + documentBucket + "/" + filename;

        } catch (Exception e) {
            log.error("Errore durante il caricamento del documento su MinIO", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il salvataggio del file");
        }

        UserDocument doc = userDocumentMapper.toEntity(dto);
        doc.setUserId(user);
        doc.setFileUrl(fileUrl);

        UserDocument saved = documentRepository.save(doc);
        return userDocumentMapper.toDto(saved);
    }

    @Override
    @PreAuthorize("(hasRole('TRAVELER') and #userId == authentication.name) or hasAnyRole('ORGANIZER', 'ADMIN')")
    public List<UserDocumentResponseDTO> getUserDocuments(String userId){
        List<UserDocument> docs = documentRepository.findByUserId(userId);
        return userDocumentMapper.toDtoList(docs);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
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

    @Override
    @Transactional
    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    public void deleteDocument(UUID docId, String userId) {
        UserDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Impossibile eliminare: documento non trovato"));

        if (!doc.getUserId().getKeycloakId().equals(userId)) {
            throw new RuntimeException("Non sei autorizzato a eliminare questo documento");
        }

        try {
            String fileUrl = doc.getFileUrl();
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(documentBucket)
                            .object(filename)
                            .build()
            );
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione del file da MinIO: {}", e.getMessage());
        }

        documentRepository.deleteById(docId);
    }
}