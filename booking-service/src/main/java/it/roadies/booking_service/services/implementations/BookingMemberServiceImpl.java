package it.roadies.booking_service.services.implementations;

import it.roadies.booking_service.data.dao.MemberDocumentRepository;
import it.roadies.booking_service.data.dto.MemberDocumentDTO;
import it.roadies.booking_service.data.entities.MemberDocument;
import it.roadies.booking_service.data.entities.enumeration.DocumentStatus;
import it.roadies.booking_service.exceptions.DocumentNotFoundException;
import it.roadies.booking_service.services.BookingMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingMemberServiceImpl implements BookingMemberService {
    private final MemberDocumentRepository memberDocumentRepository;

    @Override
    public void updateDocument(MemberDocumentDTO memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException("Documento inesistente"));
        member.setFileUrl(memberDocument.getUrl());
        memberDocumentRepository.save(member);
    }

    @Override
    public void acceptDocument(MemberDocumentDTO memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException("Documento inesistente"));
        member.setStatus(DocumentStatus.VERIFIED);
        memberDocumentRepository.save(member);

    }

    @Override
    public void rejectDocument(MemberDocumentDTO memberDocument) {
        MemberDocument member = memberDocumentRepository.findById(memberDocument.getId()).orElseThrow(()-> new DocumentNotFoundException("Documento inesistente"));
        member.setStatus(DocumentStatus.REJECTED);
        member.setRejectionReason(memberDocument.getRejectionReason());
        memberDocumentRepository.save(member);
    }
}
