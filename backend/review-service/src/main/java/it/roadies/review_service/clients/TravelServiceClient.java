package it.roadies.review_service.clients;

import it.roadies.shared.feign.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "travelService", url = "${travel-service.url}", configuration = FeignConfiguration.class)
public interface TravelServiceClient {

    @GetMapping("/api/v1/travels/review/{travelId}")
    void verifyTravelExists(@PathVariable UUID travelId);

    @GetMapping("/api/v1/activities/review/{activityId}")
    void verifyActivityExistsAndIsNotIntoATravel(@PathVariable UUID activityId);
}
