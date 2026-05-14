package it.roadies.booking_service.data.dto.response;

import it.roadies.booking_service.data.dto.MemberIdResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingStep2Response {
    private UUID bookingId;
    private List<MemberIdResponse> members;
}
