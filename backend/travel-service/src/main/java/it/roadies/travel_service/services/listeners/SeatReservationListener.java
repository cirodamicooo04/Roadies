package it.roadies.travel_service.services.listeners;

import it.roadies.shared.contracts.ReserveSeatCommand;
import it.roadies.shared.contracts.SeatReservationFailedEvent;
import it.roadies.shared.contracts.SeatReservedEvent;
import it.roadies.travel_service.exceptions.NotEnoughSeatsException;
import it.roadies.travel_service.services.impl.ActivityDepartureServiceImpl;
import it.roadies.travel_service.services.TravelDepartureService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatReservationListener {
    private final TravelDepartureService travelDepartureService;
    private final ActivityDepartureServiceImpl activityDepartureService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "travel.reserve.queue")
    public void handleReservationTravelCommand(ReserveSeatCommand command) {
        try {
            travelDepartureService.reserveSeats(command.getTravelId(), command.getPeopleCount());
            SeatReservedEvent successEvent = new SeatReservedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.reserved", successEvent);

        } catch (Exception e) {
            SeatReservationFailedEvent failedEvent = new SeatReservationFailedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.failed", failedEvent);
        }
    }

    @RabbitListener(queues = "activity.reserve.queue")
    public void handleReservationActivityCommand(ReserveSeatCommand command) {
        try {
            activityDepartureService.reserveSeats(command.getActivityId(), command.getPeopleCount());

            SeatReservedEvent successEvent = new SeatReservedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.reserved", successEvent);

        } catch (Exception e) {
            SeatReservationFailedEvent failedEvent = new SeatReservationFailedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.failed", failedEvent);
        }
    }

    @RabbitListener(queues = "travel.release.queue")
    public void handleReleaseTravelCommand(ReserveSeatCommand command) {
        try {
            travelDepartureService.releaseSeats(command.getTravelId(), command.getPeopleCount());
            SeatReservedEvent successEvent = new SeatReservedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.reserved", successEvent);

        } catch (Exception e) {
            SeatReservationFailedEvent failedEvent = new SeatReservationFailedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.failed", failedEvent);
        }
    }

    @RabbitListener(queues = "activity.release.queue")
    public void handleReleaseActivityCommand(ReserveSeatCommand command) {
        try {
            activityDepartureService.releaseSeats(command.getActivityId(), command.getPeopleCount());

            SeatReservedEvent successEvent = new SeatReservedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.reserved", successEvent);

        } catch (Exception e) {
            SeatReservationFailedEvent failedEvent = new SeatReservationFailedEvent(command.getBookingId());
            rabbitTemplate.convertAndSend("travel.exchange", "travel.seat.failed", failedEvent);
        }
    }


}