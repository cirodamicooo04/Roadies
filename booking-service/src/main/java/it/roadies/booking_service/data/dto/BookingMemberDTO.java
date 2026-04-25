package it.roadies.booking_service.data.dto;

import it.roadies.booking_service.data.entities.Booking;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingMemberDTO {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String notes;
    private String phoneNumber;
    private Booking booking;
    private List<MemberDocumentDTO> documents;
}
