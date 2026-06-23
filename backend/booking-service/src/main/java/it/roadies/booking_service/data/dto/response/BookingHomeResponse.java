package it.roadies.booking_service.data.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingHomeResponse {
    private String travelName;
    private Integer peopleCount;
    private BigDecimal totalPrice;
}
