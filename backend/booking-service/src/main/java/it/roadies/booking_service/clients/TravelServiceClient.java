package it.roadies.booking_service.clients;

import it.roadies.shared.feign.FeignConfiguration;
import it.roadies.shared.contracts.TravelBatchResponse;
import it.roadies.shared.contracts.ActivityBatchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "travel-service", configuration = FeignConfiguration.class)
public interface TravelServiceClient {

    @GetMapping("/api/v1/travels/{travelId}")
    void verifyTravelExists(@PathVariable UUID travelId);

    @GetMapping("/api/v1/travels/{travelId}/price")
    BigDecimal getTravelPrice(@PathVariable UUID travelId);

    @GetMapping("/api/v1/activities/{activityId}")
    void verifyActivityExists(@PathVariable UUID activityId);

    @GetMapping("/api/v1/activities/{activityId}/price")
    BigDecimal getActivityPrice(@PathVariable UUID activityId);

    @PostMapping("/api/v1/travels/batch")
    List<TravelBatchResponse> getTravelsBatch(@RequestBody List<UUID> travelIds);

    @PostMapping("/api/v1/activities/batch")
    List<ActivityBatchResponse> getActivitiesBatch(@RequestBody List<UUID> activityIds);
}
