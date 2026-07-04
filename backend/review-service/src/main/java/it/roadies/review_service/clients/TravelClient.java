package it.roadies.review_service.clients;

import it.roadies.shared.feign.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "travel-service", configuration = FeignConfiguration.class)
public interface TravelClient {

    @GetMapping("/api/v1/travels/review/{travelId}")
    void verifyTravel(@PathVariable UUID travelId, @RequestParam boolean isReply);

    @GetMapping("/api/v1/activities/review/{activityId}")
    void verifyActivity(@PathVariable UUID activityId, @RequestParam boolean isReply);
}
