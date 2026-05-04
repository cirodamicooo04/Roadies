package it.roadies.booking_service.data.dto;

import it.roadies.booking_service.data.entities.Booking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingMemberDTO {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Past
    private LocalDate birthDate;
    private String notes;
    @NotBlank
    private String phoneNumber;
    private Booking booking;
    private List<MemberDocumentDTO> documents;
}
