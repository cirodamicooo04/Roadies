package it.roadies.booking_service.data.dto;

import it.roadies.booking_service.data.dto.request.MemberDocumentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingMemberDTO {
    @NotBlank
    @Size(max = 100)
    private String firstName;
    @NotBlank
    @Size(max = 100)
    private String lastName;
    @Past
    @NotNull
    private LocalDate birthDate;
    @Size(max = 1000)
    private String notes;
    @NotBlank
    private String phoneNumber;
    @Valid
    private List<MemberDocumentRequest> documents;
}
