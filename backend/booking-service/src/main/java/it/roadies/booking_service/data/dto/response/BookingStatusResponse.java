package it.roadies.booking_service.data.dto.response;

import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusResponse {
    @NotNull
    private BookingStatus status;
}
