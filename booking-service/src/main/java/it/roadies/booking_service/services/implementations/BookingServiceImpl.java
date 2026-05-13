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
import it.roadies.booking_service.exceptions.StatusException;
import it.roadies.booking_service.services.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Pageable;
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

    //flusso caso d'uso di successo
    @Transactional
    public BookingDraftResponse createDraft(BookingDraftRequest requestDto) {
        log.info("Iniziata creazione draft per userId: {}", requestDto.getUserId());
        Booking booking = bookingMapper.toEntity(requestDto);
        booking.setTotalPrice(java.math.BigDecimal.ZERO);
        booking.setPeopleCount(0);
        Booking saved = bookingRepository.save(booking);
        log.info("Draft creato con successo con Booking ID: {}", saved.getId());
        return bookingMapper.toDto(saved);
    }

    @Transactional
    public void createBookingStep1(BookingCreateRequest requestDto) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(requestDto.getBookingId()));

        int oldPeopleCount = booking.getPeopleCount();
        int newPeopleCount = requestDto.getPeopleCount();

        int difference = Math.abs(newPeopleCount - oldPeopleCount);

        if (difference != 0) {
            booking.setStatus(BookingStatus.PENDING);
        }
        booking.setTravelId(requestDto.getTravelId());
        booking.setActivityId(requestDto.getActivityId());
        //il prezzo lo andrò a prendere dal travel, non lo manda il client
        booking.setTotalPrice(requestDto.getTotalPrice());
        booking.setPeopleCount(newPeopleCount);
        bookingRepository.save(booking);

        ReserveSeatCommand command = new ReserveSeatCommand(
                booking.getId(),
                requestDto.getTravelId(),
                requestDto.getActivityId(),
                difference
        );

        log.info("Invio evento RabbitMQ per Booking ID: {}. Variazione posti: {}", booking.getId(), (newPeopleCount - oldPeopleCount));

        if (newPeopleCount - oldPeopleCount > 0) {
            if (requestDto.getTravelId() != null && requestDto.getActivityId() == null) {
                rabbitTemplate.convertAndSend("travel.reserve.queue", command);
            } else if (requestDto.getTravelId() == null && requestDto.getActivityId() != null)
                rabbitTemplate.convertAndSend("activity.reserve.queue", command);

        } else if (newPeopleCount - oldPeopleCount < 0){
            if (requestDto.getTravelId() != null && requestDto.getActivityId() == null) {
                rabbitTemplate.convertAndSend("travel.release.queue", command);
            } else if (requestDto.getTravelId() == null && requestDto.getActivityId() != null)
                rabbitTemplate.convertAndSend("activity.release.queue", command);
        }
    }

    @Transactional
    public void createBookingStep2(BookingMemberRequest requestDto) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId()).orElseThrow(() -> new BookingNotFoundException(requestDto.getBookingId()));
        if (booking.getStatus() == BookingStatus.RESERVE_CONFIRMED) {
            List<BookingMember> entities = requestDto.getMembers().stream().map(member -> bookingMemberMapper.toEntity(member)).toList();
            entities.forEach(member -> member.setBooking(booking));
            booking.setMembers(entities);
            bookingRepository.save(booking);
            log.info("Aggiunti {} membri al Booking ID: {}", requestDto.getMembers().size(), booking.getId());
        } else {
            log.warn("Tentativo di aggiungere membri fallito: il Booking ID {} è nello stato {}", booking.getId(), booking.getStatus());
            throw new StatusException("Status non consentito");
        }
    }

    @Override
    public BookingStatusResponse getBookingStatus(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
        BookingStatusResponse response = new BookingStatusResponse();
        response.setStatus(booking.getStatus());
        return response;
    }

    @Transactional
    @Override
    public void confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        //qui aggiungerò un qualche evento
    }

    //metodi caso d'insuccesso

    @Transactional
    @Override
    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getStatus() == BookingStatus.RESERVE_CONFIRMED) {
            ReserveSeatCommand command = new ReserveSeatCommand(
                    booking.getId(),
                    booking.getTravelId(),
                    booking.getActivityId(),
                    booking.getPeopleCount()
            );
            if (booking.getTravelId() != null && booking.getActivityId() == null) {
                rabbitTemplate.convertAndSend("travel.exchange", "travel.release", command);
            } else if (booking.getTravelId() == null && booking.getActivityId() != null)
                rabbitTemplate.convertAndSend("activity.exchange", "activity.release", command);
        }
        //qui aggiungerò un qualche evento
    }

    @Override
    public List<UUID> getUserBookings(String userId) {
        List<Booking> bookings = bookingRepository.findAllByUserIdAndStatus(userId,BookingStatus.CONFIRMED, Pageable.ofSize(100));
        return bookings.stream().map(Booking::getId).toList();
    }
}