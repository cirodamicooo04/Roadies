package it.roadies.booking_service.data.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingHomeResponse {
    private UUID bookingId;
    private UUID principalId;
    private String travelName;
    private Integer peopleCount;
    private BigDecimal totalPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private DepartureType departureType;
}

