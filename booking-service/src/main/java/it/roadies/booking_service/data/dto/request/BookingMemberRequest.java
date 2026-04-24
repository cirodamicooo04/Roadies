package it.roadies.booking_service.data.dto.request;

import it.roadies.booking_service.data.dto.BookingMemberDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingMemberRequest {
    private UUID bookingId;
    private List<BookingMemberDTO> members;
}
