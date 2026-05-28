package it.roadies.booking_service.clients;

import it.roadies.booking_service.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "travelService", url = "${travel-service.url}", configuration = FeignConfiguration.class)
public interface TravelServiceClient {

    @GetMapping("/api/v1/travels/{travelId}")
    void verifyTravelExists(@PathVariable UUID travelId);

    @GetMapping("/api/v1/travels/{travelId}/price")
    BigDecimal getTravelPrice(@PathVariable UUID travelId);

    @GetMapping("/api/v1/activities/{activityId}")
    void verifyActivityExists(@PathVariable UUID activityId);

    @GetMapping("/api/v1/activities/{activityId}/price")
    BigDecimal getActivityPrice(@PathVariable UUID activityId);


}
