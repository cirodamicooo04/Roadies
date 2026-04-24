package it.roadies.travel_service.listeners;

import it.roadies.travel_service.data.dto.event.BookingCreatedEvent;
import it.roadies.travel_service.data.dto.event.SpotsFailedEvent;
import it.roadies.travel_service.data.dto.event.SpotsReservedEvent;
import it.roadies.travel_service.services.impl.TravelDepartureServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingSagaListener {

    private final TravelDepartureServiceImpl travelDepartureService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queuesToDeclare = @Queue("booking.created.queue"))
    public void handleBookingCreated(BookingCreatedEvent event) {
        try {
            travelDepartureService.reserveSpots(event.getTravelId(), event.getSpots());

            SpotsReservedEvent successEvent = new SpotsReservedEvent(event.getBookingId());
            rabbitTemplate.convertAndSend("spots.reserved.queue", successEvent);

        } catch (Exception e) {
            SpotsFailedEvent failEvent = new SpotsFailedEvent(event.getBookingId(), e.getMessage());
            rabbitTemplate.convertAndSend("spots.failed.queue", failEvent);
        }
    }
}
