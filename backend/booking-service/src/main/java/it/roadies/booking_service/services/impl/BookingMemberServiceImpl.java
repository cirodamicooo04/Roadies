package it.roadies.booking_service.services.impl;

import io.minio.errors.MinioException;
import it.roadies.booking_service.config.i8n.MessageLang;
import it.roadies.booking_service.data.dao.MemberDocumentRepository;
import it.roadies.booking_service.data.dto.request.MemberDocumentUpdateRequest;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.MemberDocument;
import it.roadies.booking_service.data.entities.enumeration.DocumentStatus;
import it.roadies.booking_service.exceptions.UnauthorizedActionException;
import it.roadies.booking_service.exceptions.DocumentNotFoundException;
import it.roadies.booking_service.services.BookingMemberService;
import it.roadies.booking_service.services.BookingService;
import it.roadies.booking_service.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingMemberServiceImpl implements BookingMemberService {
    private final MemberDocumentRepository memberDocumentRepository;
    private final MinioService minioService;
    private final MessageLang messageLang;
    private final BookingService bookingService;

//    @Override
//    public void updateDocument(MemberDocumentUpdateRequest memberDocument) {
//        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException(messageLang.getMessage("error.document.not.exists")));
//        member.setFileUrl(memberDocument.getUrl());
//        memberDocumentRepository.save(member);
//    }

    @Transactional
    @Override
    public void acceptDocument(MemberDocumentUpdateRequest memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException(messageLang.getMessage("error.document.not.exists")));
        member.setStatus(DocumentStatus.VERIFIED);
        memberDocumentRepository.save(member);
    }

    @Transactional
    @Override
    public void rejectDocument(MemberDocumentUpdateRequest memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException(messageLang.getMessage("error.document.not.exists")));
        member.setStatus(DocumentStatus.REJECTED);
        member.setRejectionReason(memberDocument.getRejectionReason());
        memberDocumentRepository.save(member);
    }

    @Override
    public String uploadDocumentPhoto(UUID documentId, MultipartFile file, String userId) throws MinioException {
        MemberDocument document = memberDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(messageLang.getMessage("error.document.not.exists")));

        Booking booking = document.getMember().getBooking();
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedActionException(messageLang.getMessage("error.access.denied"));
        }

        String fileUrl = minioService.uploadFile(file);

        document.setFileUrl(fileUrl);
        memberDocumentRepository.save(document);

        bookingService.updateBookingIfAllDocumentsUploaded(booking.getId());

        return fileUrl;
    }
}
