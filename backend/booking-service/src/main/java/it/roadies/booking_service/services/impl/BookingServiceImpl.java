package it.roadies.booking_service.services.impl;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.BookingMemberDTO;
import it.roadies.shared.contracts.GamificationEvent;
import it.roadies.shared.contracts.NotificationEvent;
import it.roadies.booking_service.data.dto.response.*;
import it.roadies.shared.contracts.ActivityBatchResponse;
import it.roadies.shared.contracts.TravelBatchResponse;
import it.roadies.shared.contracts.ReserveSeatCommand;
import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.BookingMember;
import it.roadies.booking_service.data.entities.MemberDocument;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.data.mapper.BookingMemberMapper;
import it.roadies.booking_service.exceptions.UnauthorizedActionException;
import it.roadies.booking_service.exceptions.BookingNotFoundException;
import it.roadies.booking_service.data.mapper.BookingMapper;
import it.roadies.booking_service.exceptions.StatusException;
import it.roadies.booking_service.services.BookingService;
import it.roadies.booking_service.services.MinioService;
import it.roadies.booking_service.services.clients.TravelService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingMemberMapper bookingMemberMapper;
    private final RabbitTemplate rabbitTemplate;
    private final TravelService travelService;
    private final MessageLang messageLang;
    private final MinioService minioService;

    //flusso caso d'uso di successo
    @Transactional
    public BookingDraftResponse createDraft(BookingDraftRequest requestDto, String userId) {
        travelService.verifyTravelExists(requestDto.getTravelId(), requestDto.getActivityId());

        log.info("Iniziata creazione draft per userId={}", userId);
        Booking booking = bookingMapper.toEntity(requestDto, userId);
        booking.setTotalPrice(BigDecimal.ZERO);
        booking.setPeopleCount(0);
        Booking saved = bookingRepository.save(booking);
        log.info("Draft creato con successo con Booking ID: {}", saved.getId());
        return bookingMapper.toDto(saved);
    }

    @Transactional
    public void createPendingAndReserveSeats(BookingCreateRequest requestDto, String userId) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", requestDto.getBookingId())));

        if (!booking.getUserId().equals(userId) || booking.getStatus().equals(BookingStatus.CONFIRMED) || booking.getStatus().equals(BookingStatus.EXPIRED) || booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new UnauthorizedActionException(messageLang.getMessage("error.access.denied"));
        }


        int oldPeopleCount = booking.getPeopleCount();
        int newPeopleCount = requestDto.getPeopleCount();
        if (oldPeopleCount == 0) log.info("Inizio modica booking precedentemente in stato di draft per la prenotazione {}", booking.getId());

        log.debug("bookingId={} oldPeopleCount={} newPeopleCount={}", booking.getId(), oldPeopleCount, newPeopleCount);

        int difference = Math.abs(newPeopleCount - oldPeopleCount);

        if (difference != 0) {
            if (oldPeopleCount == 0){
                log.info("L'utente {} richiede la riserva posti per la prima volta per {} persone", userId, difference);
            }
            else {
                log.info("L'utente {} ha cambiato il numero di posti da {} a {}", userId, oldPeopleCount, newPeopleCount);
            }
            booking.setStatus(BookingStatus.PENDING);
        }

        BigDecimal price = travelService.priceForTravel(requestDto.getTravelId(), requestDto.getActivityId());

        booking.setTravelId(requestDto.getTravelId());
        booking.setActivityId(requestDto.getActivityId());
        booking.setTotalPrice(BigDecimal.valueOf(newPeopleCount).multiply(price));
        booking.setPeopleCount(newPeopleCount);
        bookingRepository.save(booking);

        ReserveSeatCommand command = new ReserveSeatCommand(
                booking.getId(),
                requestDto.getTravelId(),
                requestDto.getActivityId(),
                difference
        );

        if (newPeopleCount - oldPeopleCount > 0) {
            log.info("Invio evento RabbitMQ per Booking ID: {}. Aggiunta posti: {}", booking.getId(), difference);
            if (requestDto.getTravelId() != null) {
                rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.reserve.travel", command);
            } else rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.reserve.activity", command);

        } else if (newPeopleCount - oldPeopleCount < 0){
            log.info("Invio evento RabbitMQ per Booking ID: {}. Rimozione posti: {}", booking.getId(), difference);
            if (requestDto.getTravelId() != null) {
                rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.release.travel", command);
            } else rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.release.activity", command);
        }
    }

    @Override
    @Transactional
    public BookingStep2Response insertMembers(BookingMemberRequest requestDto, String userId) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", requestDto.getBookingId())));

        if (!booking.getUserId().equals(userId)){
            throw new UnauthorizedActionException(messageLang.getMessage("error.access.denied"));
        }

        if (booking.getStatus() != BookingStatus.RESERVE_CONFIRMED) {
            throw new StatusException(messageLang.getMessage("error.status.insert.member"));
        }

        if (requestDto.getMembers().size() != booking.getPeopleCount()) {
            throw new StatusException(messageLang.getMessage("error.status.number.member"));
        }

        List<BookingMember> entities = new ArrayList<>();

        for (BookingMemberDTO memberDto : requestDto.getMembers()) {
            BookingMember member = bookingMemberMapper.toEntity(memberDto);
            member.setBooking(booking);

            entities.add(member);
        }

        booking.setMembers(entities);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingStep2Response.builder()
                .bookingId(savedBooking.getId())
                .members(savedBooking.getMembers().stream()
                        .map(m -> MemberIdResponse.builder()
                                .memberId(m.getId())
                                .firstName(m.getFirstName())
                                .lastName(m.getLastName())
                                .documentIds(m.getDocuments() != null ? m.getDocuments().stream()
                                        .map(MemberDocument::getId)
                                        .toList() : java.util.Collections.emptyList())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public BookingStatusResponse getBookingStatus(UUID bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (!booking.getUserId().equals(userId)){
            throw new UnauthorizedActionException(messageLang.getMessage("error.access.denied"));
        }
        return new BookingStatusResponse(booking.getStatus());
    }

    @Transactional
    @Override
    public void confirmBookingAfterPayment(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (booking.getStatus() == BookingStatus.CONFIRMED) return;
        if (booking.getStatus() != BookingStatus.READY_FOR_PAYMENT) {
            return;
        }
        log.info("Booking {} confermato con successo", bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        rabbitTemplate.convertAndSend("booking.exchange", "booking.gamification.points.add", new GamificationEvent(booking.getUserId(), booking.getTotalPrice()));
        rabbitTemplate.convertAndSend("notification.exchange", "notification.mail.send", new NotificationEvent(booking.getUserId(), "Conferma Prenotazione", "Ciao,\n\nti confermiamo che la tua prenotazione è stata completata con successo.\n\nUn saluto,\nIl Team"));
    }

    //metodi caso d'insuccesso

    @Transactional
    @Override
    public void deleteBooking(UUID bookingId, String userId, String mailTo) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (!booking.getUserId().equals(userId)){
            throw new UnauthorizedActionException(messageLang.getMessage("error.access.denied"));
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new StatusException(messageLang.getMessage("error.status.delete.booking"));
        }

        log.info("Booking {} cancellata con successo", bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        deleteMinioDocument(booking);
        bookingRepository.save(booking);
        rabbitTemplate.convertAndSend("notification.exchange", "notification.mail.send", new NotificationEvent(mailTo, "Eliminazione Prenotazione", "Ciao,\n\nti confermiamo che la tua prenotazione è stata cancellata con successo.\n\nUn saluto,\nIl Team"));
        ReserveSeatCommand command = new ReserveSeatCommand(
                booking.getId(),
                booking.getTravelId(),
                booking.getActivityId(),
                booking.getPeopleCount()
        );
        if (booking.getTravelId() != null) {
            rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.release.travel", command);
        } else rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.release.activity", command);
        rabbitTemplate.convertAndSend("booking.exchange", "booking.gamification.points.remove", new GamificationEvent(booking.getUserId(), booking.getTotalPrice()));
    }

    public void deleteMinioDocument(Booking booking){
        if (booking.getMembers() != null) {
            for (BookingMember member : booking.getMembers()) {
                if (member.getDocuments() != null) {
                    for (MemberDocument doc : member.getDocuments()) {
                        minioService.deleteFileByUrl(doc.getFileUrl());
                        doc.setFileUrl(null);
                    }
                }
            }
        }
    }

    @Override
    public Page<BookingHomeResponse> getPastBookingsFromUser(String userJwt, Pageable pageable) {
        return getBookingsByTimeStatus(userJwt, true, pageable);
    }

    @Override
    public Page<BookingHomeResponse> getActiveBookingsFromUser(String userJwt, Pageable pageable) {
        return getBookingsByTimeStatus(userJwt, false, pageable);
    }

    private Page<BookingHomeResponse> getBookingsByTimeStatus(String userJwt, boolean fetchPast, Pageable pageable) {
        // Fetch di tutte le prenotazioni confermate dell'utente
        List<Booking> bookings = bookingRepository.findAllByUserIdAndStatus(userJwt, BookingStatus.CONFIRMED, Pageable.unpaged());

        List<UUID> travelIds = bookings.stream()
                .map(Booking::getTravelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<UUID> activityIds = bookings.stream()
                .map(Booking::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, TravelBatchResponse> travelMap = new HashMap<>();
        if (!travelIds.isEmpty()) {
            List<TravelBatchResponse> travels = travelService.getTravelsBatch(travelIds);
            if (travels != null) {
                for (TravelBatchResponse t : travels) {
                    travelMap.put(t.getDepartureId(), t);
                }
            }
        }

        Map<UUID, ActivityBatchResponse> activityMap = new HashMap<>();
        if (!activityIds.isEmpty()) {
            List<ActivityBatchResponse> activities = travelService.getActivitiesBatch(activityIds);
            if (activities != null) {
                for (ActivityBatchResponse a : activities) {
                    activityMap.put(a.getDepartureId(), a);
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<BookingHomeResponse> filteredBookings = new ArrayList<>();

        for (Booking booking : bookings) {
            UUID principalId = null;
            String title = null;
            boolean includeBooking = false;
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            DepartureType departureType = DepartureType.TRAVEL;

            if (booking.getTravelId() != null) {
                TravelBatchResponse travel = travelMap.get(booking.getTravelId());
                if (travel != null) {
                    title = travel.getTitle();
                    principalId = travel.getPricipalTravelId();
                    startDate = travel.getStartDate().atStartOfDay();
                    endDate = travel.getEndDate().atStartOfDay();
                    if (travel.getEndDate() != null) {
                        boolean isBeforeNow = travel.getEndDate().isBefore(now.toLocalDate());
                        includeBooking = (fetchPast && isBeforeNow) || (!fetchPast && !isBeforeNow);
                    }
                }
            } else if (booking.getActivityId() != null) {
                ActivityBatchResponse activity = activityMap.get(booking.getActivityId());
                if (activity != null) {
                    departureType = DepartureType.ACTIVITY;
                    title = activity.getTitle();
                    principalId = activity.getPricipalActivityId();
                    startDate = activity.getStartDate();
                    endDate = activity.getEndDate();
                    if (activity.getEndDate() != null) {
                        boolean isBeforeNow = activity.getEndDate().isBefore(now);
                        includeBooking = (fetchPast && isBeforeNow) || (!fetchPast && !isBeforeNow);
                    }
                }
            }

            if (includeBooking) {
                BookingHomeResponse dto = new BookingHomeResponse();
                dto.setBookingId(booking.getId());
                dto.setPrincipalId(principalId);
                dto.setTravelName(title);
                dto.setPeopleCount(booking.getPeopleCount());
                dto.setTotalPrice(booking.getTotalPrice());
                dto.setStartDate(startDate);
                dto.setEndDate(endDate);
                dto.setDepartureType(departureType);
                filteredBookings.add(dto);
            }
        }

        // paginazione
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredBookings.size());
        List<BookingHomeResponse> pagedList = new ArrayList<>();
        if (start <= filteredBookings.size()) {
            pagedList = filteredBookings.subList(start, end);
        }

        return new PageImpl<>(pagedList, pageable, filteredBookings.size());
    }

    @Transactional
    @Override
    public void updateBookingIfAllDocumentsUploaded(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (booking.getMembers() == null || booking.getStatus() == BookingStatus.CONFIRMED) return;
        boolean allDocumentsUploaded = booking.getMembers()
                .stream()
                .flatMap(member -> member.getDocuments().stream())
                .allMatch(document -> document.getFileUrl() != null && !document.getFileUrl().isBlank());

        if (allDocumentsUploaded) {
            booking.setStatus(BookingStatus.READY_FOR_PAYMENT);
            bookingRepository.save(booking);
        }
    }

    @Override
    public List<UUID> getUserBookings(String userId) {
        List<Booking> bookings = bookingRepository.findAllByUserIdAndStatus(userId,BookingStatus.CONFIRMED, Pageable.ofSize(100));
        return bookings.stream().map(Booking::getId).toList();
    }
}