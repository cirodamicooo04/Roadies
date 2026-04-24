package it.roadies.booking_service.services.implementations;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.event.ReserveSeatCommand;
import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.BookingMember;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.data.mapper.BookingMemberMapper;
import it.roadies.booking_service.exceptions.BookingNotFoundException;
import it.roadies.booking_service.data.mapper.BookingMapper;
import it.roadies.booking_service.services.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RabbitTemplate rabbitTemplate;
    private final BookingMemberMapper bookingMemberMapper;

    @Transactional
    public BookingDraftResponse createDraft(BookingDraftRequest requestDto) {
        Booking booking = bookingMapper.toEntity(requestDto);
        booking.setTotalPrice(java.math.BigDecimal.ZERO);
        booking.setPeopleCount(1);
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDto(saved);
    }

    @Transactional
    public void createBookingStep1(BookingCreateRequest requestDto) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(requestDto.getBookingId()));

        booking.setStatus(BookingStatus.PENDING);
        booking.setTravelId(requestDto.getTravelId());
        booking.setActivityId(requestDto.getActivityId());
        booking.setTotalPrice(requestDto.getTotalPrice());
        booking.setPeopleCount(requestDto.getPeopleCount());
        bookingRepository.save(booking);

        ReserveSeatCommand command = new ReserveSeatCommand(
                booking.getId(),
                requestDto.getTravelId(),
                requestDto.getActivityId(),
                requestDto.getPeopleCount()
        );

        if (requestDto.getTravelId()!=null) {
            rabbitTemplate.convertAndSend("travel.exchange", "travel.reserve", command);
        }
        else rabbitTemplate.convertAndSend("activity.exchange", "activity.reserve", command);
    }

    @Transactional
    public void createBookingStep2(BookingMemberRequest requestDto) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId()).orElseThrow(() -> new BookingNotFoundException(requestDto.getBookingId()));
        List <BookingMember> entities = requestDto.getMembers().stream().map(member -> bookingMemberMapper.toEntity(member)).toList();
        entities.forEach(member -> member.setBooking(booking));
        booking.setMembers(entities);
        bookingRepository.save(booking);
    }

    @Override
    public BookingStatusResponse getBookingStatus(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
        BookingStatusResponse response = new BookingStatusResponse();
        response.setStatus(booking.getStatus());
        return response;
    }
}