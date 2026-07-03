package it.roadies.shared.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelBatchResponse {
    private UUID pricipalTravelId;
    private UUID departureId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}
