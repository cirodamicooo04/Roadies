package it.roadies.booking_service.services.implementations;

import io.minio.errors.MinioException;
import it.roadies.booking_service.data.dao.MemberDocumentRepository;
import it.roadies.booking_service.data.dto.request.MemberDocumentUpdateRequest;
import it.roadies.booking_service.data.entities.MemberDocument;
import it.roadies.booking_service.data.entities.enumeration.DocumentStatus;
import it.roadies.booking_service.exceptions.DocumentNotFoundException;
import it.roadies.booking_service.services.BookingMemberService;
import it.roadies.booking_service.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingMemberServiceImpl implements BookingMemberService {
    private final MemberDocumentRepository memberDocumentRepository;
    private final MinioService minioService;

    @Override
    public void updateDocument(MemberDocumentUpdateRequest memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException("Documento inesistente"));
        member.setFileUrl(memberDocument.getUrl());
        memberDocumentRepository.save(member);
    }

    @Override
    public void acceptDocument(MemberDocumentUpdateRequest memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException("Documento inesistente"));
        member.setStatus(DocumentStatus.VERIFIED);
        memberDocumentRepository.save(member);
    }

    @Override
    public void rejectDocument(MemberDocumentUpdateRequest memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException("Documento inesistente"));
        member.setStatus(DocumentStatus.REJECTED);
        member.setRejectionReason(memberDocument.getRejectionReason());
        memberDocumentRepository.save(member);
    }

    @Override
    public String uploadDocumentPhoto(UUID documentId, MultipartFile file) throws MinioException {
        MemberDocument document = memberDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Documento inesistente"));

        String fileUrl = minioService.uploadFile(file);

        document.setFileUrl(fileUrl);
        memberDocumentRepository.save(document);

        return fileUrl;
    }
}
