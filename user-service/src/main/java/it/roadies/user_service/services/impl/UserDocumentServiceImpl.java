package it.roadies.user_service.services.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.roadies.user_service.conf.i8n.MessageLang;
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
    private final MessageLang messageLang;

    @Value("${minio.documentBucket:user-documents}")
    private String documentBucket;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    @Transactional
    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    public UserDocumentResponseDTO uploadDocument(String userId, UserDocumentRequestDTO dto, MultipartFile file){
        log.info("Iniziato caricamento documento per l'utente ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Upload documento fallito: Utente non trovato con ID: {}", userId);
                    return new RuntimeException(messageLang.getMessage("error.user.notfound"));
                });

        String fileUrl;
        try {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename().replace(" ", "_");
            log.info("Salvataggio file {} su MinIO nel bucket {}", filename, documentBucket);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(documentBucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );

            fileUrl = minioUrl + "/" + documentBucket + "/" + filename;
            log.info("File salvato con successo su MinIO. URL: {}", fileUrl);

        } catch (Exception e) {
            log.error("Errore critico durante il caricamento del documento su MinIO per l'utente {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messageLang.getMessage("error.image.minio.upload"));
        }

        UserDocument doc = userDocumentMapper.toEntity(dto);
        doc.setUserId(user);
        doc.setFileUrl(fileUrl);

        UserDocument saved = documentRepository.save(doc);
        log.info("Documento salvato nel database con ID: {}", saved.getId());
        return userDocumentMapper.toDto(saved);
    }

    @Override
    @PreAuthorize("(hasRole('TRAVELER') and #userId == authentication.name) or hasAnyRole('ORGANIZER', 'ADMIN')")
    public List<UserDocumentResponseDTO> getUserDocuments(String userId){
        log.info("Recupero documenti per l'utente ID: {}", userId);
        List<UserDocument> docs = documentRepository.findByUserId(userId);
        return userDocumentMapper.toDtoList(docs);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public UserDocumentResponseDTO verifyDocument(UUID docId, boolean approved, String reason){
        log.info("Iniziata verifica documento ID: {}. Esito approvazione: {}", docId, approved);

        UserDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> {
                    log.error("Verifica fallita: Documento ID: {} non trovato", docId);
                    return new RuntimeException(messageLang.getMessage("error.document.notfound"));
                });

        if(approved){
            doc.setStatus(DocumentStatus.VERIFIED);
            doc.setRejectionReason(null);
            doc.setVerifiedAt(LocalDateTime.now());
            log.info("Documento ID: {} contrassegnato come VERIFICATO", docId);
        }
        else{
            doc.setStatus(DocumentStatus.REJECTED);
            doc.setRejectionReason(reason);
            doc.setVerifiedAt(null);
            log.info("Documento ID: {} RIFIUTATO. Motivo: {}", docId, reason);
        }

        return userDocumentMapper.toDto(documentRepository.save(doc));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    public void deleteDocument(UUID docId, String userId) {
        log.info("Richiesta di eliminazione documento ID: {} da parte dell'utente ID: {}", docId, userId);

        UserDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> {
                    log.error("Eliminazione fallita: Documento ID: {} non trovato", docId);
                    return new RuntimeException(messageLang.getMessage("error.document.notfound"));
                });

        if (!doc.getUserId().getKeycloakId().equals(userId)) {
            log.error("Tentativo non autorizzato di eliminare il documento ID: {} dall'utente ID: {}", docId, userId);
            throw new RuntimeException(messageLang.getMessage("error.unauthorized"));
        }

        try {
            String fileUrl = doc.getFileUrl();
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

            log.info("Cancellazione file {} dal bucket MinIO {}", filename, documentBucket);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(documentBucket)
                            .object(filename)
                            .build()
            );
            log.info("File eliminato da MinIO con successo");
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione del file da MinIO per il documento ID: {}", docId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messageLang.getMessage("error.image.minio.delete"));
        }

        documentRepository.deleteById(docId);
        log.info("Documento ID: {} rimosso dal database", docId);
    }
}