package it.roadies.travel_service.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBatchResponse {
    private UUID id;
    private String title;
    private LocalDateTime endDate;
}
