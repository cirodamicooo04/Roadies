package it.roadies.travel_service.services.listeners;

import it.roadies.shared.contracts.ReviewActivityUpdateEvent;
import it.roadies.shared.contracts.ReviewTravelUpdateEvent;
import it.roadies.travel_service.services.ActivityService;
import it.roadies.travel_service.services.TravelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewListener {

    private final TravelService travelService;
    private final ActivityService activityService;

    @RabbitListener(queues = "travel-service.added.tr.review.queue")
    public void handleTravelReviewAdded(ReviewTravelUpdateEvent event){
        try {
            travelService.updateTravelReviews(event);
            log.info("Travel reviews updated with success");
        } catch (Exception e) {
            log.error("Error while updating travel reviews: {}", e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "travel-service.added.ac.review.queue")
    public void handleActivityReviewAdded(ReviewActivityUpdateEvent event){
        try {
            activityService.updateActivityReviews(event);
            log.info("Activity reviews updated with success");
        } catch (Exception e) {
            log.error("Error while updating activity reviews: {}", e.getMessage());
            throw e;
        }
    }
}
