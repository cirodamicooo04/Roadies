package it.roadies.user_service.services.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import it.roadies.user_service.conf.i8n.MessageLang;
import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.UserDocument;
import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.repositories.UserDocumentRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.exception.ConflictException;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.mappers.UserDocumentMapper;
import it.roadies.user_service.services.MinioService;
import it.roadies.user_service.services.UserDocumentService;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Objects;
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
    private final MinioService minioService;

    @Value("${minio.documentBucket:user-documents}")
    private String documentBucket;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    @Transactional
    public UserDocumentResponseDTO uploadDocument(String userId, UserDocumentRequestDTO dto, MultipartFile file,String id) {
        log.info("Iniziato caricamento documento per l'utente");

        String actualUserId = id;
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(messageLang.getMessage("error.file.empty"));
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException(messageLang.getMessage("error.type.not.supported"));
        }

        User user = userRepository.findById(actualUserId)
                .orElseThrow(() -> {
                    log.error("Upload documento fallito: Utente non trovato");
                    return new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
                });

        String fileUrl;
        try {
            fileUrl = minioService.uploadFile(file, documentBucket);
        } catch (Exception e) {
            log.error("Errore durante l'upload del file su MinIO per l'utente", e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il caricamento del documento su MinIO");
        }

        UserDocument doc = userDocumentMapper.toEntity(dto);
        doc.setUserId(user);
        doc.setFileUrl(fileUrl);

        try {
            UserDocument saved = documentRepository.save(doc);
            log.info("Documento salvato nel database");
            
            UserDocumentResponseDTO responseDto = userDocumentMapper.toDto(saved);
            if (responseDto.getFileUrl() != null && !responseDto.getFileUrl().isBlank()) {
                String presignedUrl = minioService.generatePresignedUrl(responseDto.getFileUrl(), documentBucket);
                responseDto.setFileUrl(presignedUrl);
            }
            return responseDto;
        } catch (Exception e) {
            log.error("Errore salvataggio DB. Eseguo rollback: elimino il file da MinIO...", e);
            try {
                minioService.deleteFile(fileUrl, documentBucket);
            } catch (Exception minioEx) {
                log.error("Fallita rimozione file orfano su MinIO ({})", fileUrl, minioEx);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messageLang.getMessage("error.internal.server.error.message"));
        }
    }

    @Override
    public List<UserDocumentResponseDTO> getMyDocuments(String userId, String userJWT){
        log.info("Recupero documenti per l'utente ID: {}", userId);

        // Stessa logica: se c'è un disallineamento tra cache client (username) e token (keycloakId),
        // ci fidiamo unicamente del JWT per recuperare i documenti.
        String actualUserId = userJWT;

        List<UserDocument> docs = documentRepository.findByUserId(actualUserId);
        List<UserDocumentResponseDTO> dtoList = userDocumentMapper.toDtoList(docs);

        dtoList.forEach(dto -> {
            if (dto.getFileUrl() != null && !dto.getFileUrl().isBlank()) {
                String presignedUrl = minioService.generatePresignedUrl(dto.getFileUrl(), documentBucket);
                dto.setFileUrl(presignedUrl);
            }
        });

        return dtoList;
    }

    @Override
    public List<UserDocumentResponseDTO> getUserDocuments(String userId){
        log.info("Recupero documenti per l'utente");

        List<UserDocument> docs = documentRepository.findByUserId(userId);
        List<UserDocumentResponseDTO> dtoList = userDocumentMapper.toDtoList(docs);

        dtoList.forEach(dto -> {
            if (dto.getFileUrl() != null && !dto.getFileUrl().isBlank()) {
                String presignedUrl = minioService.generatePresignedUrl(dto.getFileUrl(), documentBucket);
                dto.setFileUrl(presignedUrl);
            }
        });

        return dtoList;
    }

    @Override
    @Transactional
    public UserDocumentResponseDTO verifyDocument(UUID docId, boolean approved, String reason){
        log.info("Iniziata verifica documento. Esito approvazione: {}", approved);

        UserDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> {
                    log.error("Verifica fallita: Documento non trovato");
                    return new ResourceNotFoundException(messageLang.getMessage("error.document.notfound"));
                });

        // Se si vuole implementare lo status dei documenti scommentare:
        // if (doc.getStatus() != DocumentStatus.PENDING) {
        //     throw new ConflictException(messageLang.getMessage("error.document.already.approved"));
        // }
        //
        // if(approved){
        //     doc.setStatus(DocumentStatus.VERIFIED);
        //     doc.setRejectionReason(null);
        //     doc.setVerifiedAt(LocalDateTime.now());
        //     log.info("Documento ID: {} contrassegnato come VERIFICATO", docId);
        // }
        // else{
        //     doc.setStatus(DocumentStatus.REJECTED);
        //     doc.setRejectionReason(reason);
        //     doc.setVerifiedAt(null);
        //     log.info("Documento RIFIUTATO. Motivo: {}", reason);
        // }

        UserDocumentResponseDTO responseDto = userDocumentMapper.toDto(documentRepository.save(doc));
        if (responseDto.getFileUrl() != null && !responseDto.getFileUrl().isBlank()) {
            String presignedUrl = minioService.generatePresignedUrl(responseDto.getFileUrl(), documentBucket);
            responseDto.setFileUrl(presignedUrl);
        }
        return responseDto;
    }

    @Override
    @Transactional
    public void deleteDocument(UUID docId, String userId) {
        log.info("Richiesta di eliminazione documento da parte dell'utente");

        UserDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> {
                    log.error("Eliminazione fallita: Documento non trovato");
                    return new ResourceNotFoundException(messageLang.getMessage("error.document.notfound"));
                });

        if (!doc.getUserId().getKeycloakId().equals(userId)) {
            log.error("Tentativo non autorizzato di eliminare il documento dall'utente");
            throw new org.springframework.security.access.AccessDeniedException(messageLang.getMessage("error.unauthorized"));
        }

        try {
            String fileUrl = doc.getFileUrl();
            if (fileUrl != null && fileUrl.contains("/")) {
                String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                log.info("Cancellazione file {} dal bucket MinIO {}", filename, documentBucket);
                minioService.deleteFile(fileUrl, documentBucket);
                log.info("File eliminato da MinIO con successo");
            }
        } catch (Exception e) {
            log.warn("Fallita eliminazione fisica da MinIO. Il file potrebbe non esistere. URL: {}", doc.getFileUrl(), e);
        }

        documentRepository.deleteById(docId);
        log.info("Documento rimosso dal database");
    }
}