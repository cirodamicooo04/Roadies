package it.roadies.booking_service.data.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BookingMemberDTO {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String notes;
    private String phoneNumber;
    private UUID booking;
    private List<MemberDocumentDTO> documents;
}
