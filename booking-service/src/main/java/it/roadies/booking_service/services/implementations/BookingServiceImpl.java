package it.roadies.booking_service.services.implementations;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.event.BookingCreatedEvent;
import it.roadies.booking_service.data.dto.request.BookingRequest;
import it.roadies.booking_service.data.dto.response.BookingResponse;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.mapper.BookingMapper;
import it.roadies.booking_service.services.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public BookingResponse createBooking(BookingRequest requestDto) {

        Booking booking = bookingMapper.toEntity(requestDto);
        booking.setStatus(BookingStatus.DRAFT);
        Booking savedBooking = bookingRepository.save(booking);

        BookingCreatedEvent event = new BookingCreatedEvent(
                savedBooking.getId(),
                requestDto.getTravelId(),
                requestDto.getPeopleCount()
        );

        rabbitTemplate.convertAndSend("booking.created.queue", event);
        return bookingMapper.toDto(savedBooking);
    }
}
