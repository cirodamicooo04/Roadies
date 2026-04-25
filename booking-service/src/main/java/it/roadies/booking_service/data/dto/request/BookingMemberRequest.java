package it.roadies.booking_service.data.dto.request;

import it.roadies.booking_service.data.dto.BookingMemberDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingMemberRequest {
    @NotNull
    private UUID bookingId;
    @NotNull
    private List<BookingMemberDTO> members;
}
