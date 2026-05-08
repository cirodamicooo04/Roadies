package it.roadies.booking_service.services;

import it.roadies.booking_service.data.dto.MemberDocumentDTO;

public interface BookingMemberService {
    public void updateDocument (MemberDocumentDTO memberDocument);
    public void acceptDocument (MemberDocumentDTO memberDocument);
    public void rejectDocument (MemberDocumentDTO memberDocument);
}
