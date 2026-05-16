package it.roadies.booking_service.services.implementations;

import it.roadies.booking_service.config.i8n.MessageLang;
import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.BookingMemberDTO;
import it.roadies.booking_service.data.dto.response.MemberIdResponse;
import it.roadies.booking_service.data.dto.event.ReserveSeatCommand;
import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.request.MemberDocumentRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import it.roadies.booking_service.data.dto.response.BookingStep2Response;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.BookingMember;
import it.roadies.booking_service.data.entities.MemberDocument;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.data.entities.enumeration.DocumentStatus;
import it.roadies.booking_service.exceptions.AccessDeniedException;
import it.roadies.booking_service.exceptions.BookingNotFoundException;
import it.roadies.booking_service.data.mapper.BookingMapper;
import it.roadies.booking_service.exceptions.StatusException;
import it.roadies.booking_service.services.BookingService;
import it.roadies.booking_service.services.clients.TravelService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RabbitTemplate rabbitTemplate;
    private final TravelService travelService;
    private final MessageLang messageLang;

    //flusso caso d'uso di successo
    @Transactional
    public BookingDraftResponse createDraft(BookingDraftRequest requestDto, String userId) {
        travelService.verifyTravelExists(requestDto.getTravelId(), requestDto.getActivityId());

        log.info("Iniziata creazione draft per userId");
        Booking booking = bookingMapper.toEntity(requestDto, userId);
        booking.setTotalPrice(BigDecimal.ZERO);
        booking.setPeopleCount(0);
        Booking saved = bookingRepository.save(booking);
        log.info("Draft creato con successo con Booking ID: {}", saved.getId());
        return bookingMapper.toDto(saved);
    }

    @Transactional
    public void createBookingStep1(BookingCreateRequest requestDto, String userId) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", requestDto.getBookingId())));

        if (!booking.getUserId().equals(userId)){
            throw new AccessDeniedException(messageLang.getMessage("error.access.denied"));
        }

        int oldPeopleCount = booking.getPeopleCount();
        int newPeopleCount = requestDto.getPeopleCount();

        int difference = Math.abs(newPeopleCount - oldPeopleCount);

        if (difference != 0) {
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

    @Override
    @Transactional
    public BookingStep2Response createBookingStep2(BookingMemberRequest requestDto, String userId) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", requestDto.getBookingId())));

        if (!booking.getUserId().equals(userId)){
            throw new AccessDeniedException(messageLang.getMessage("error.access.denied"));
        }

        if (booking.getStatus() != BookingStatus.RESERVE_CONFIRMED) {
            throw new StatusException(messageLang.getMessage("error.status.insert.member"));
        }

        if (requestDto.getMembers().size() != booking.getPeopleCount()) {
            throw new StatusException(messageLang.getMessage("error.status.number.member"));
        }

        List<BookingMember> entities = new ArrayList<>();

        for (BookingMemberDTO memberDto : requestDto.getMembers()) {
            BookingMember member = new BookingMember();
            member.setFirstName(memberDto.getFirstName());
            member.setLastName(memberDto.getLastName());
            member.setBirthDate(memberDto.getBirthDate());
            member.setNotes(memberDto.getNotes());
            member.setPhoneNumber(memberDto.getPhoneNumber());
            member.setBooking(booking);

            List<MemberDocument> documents = new ArrayList<>();

            if (memberDto.getDocuments() != null) {
                for (MemberDocumentRequest docDto : memberDto.getDocuments()) {
                    MemberDocument doc = new MemberDocument();
                    doc.setType(docDto.getType());
                    doc.setStatus(DocumentStatus.PENDING);
                    doc.setMember(member);
                    documents.add(doc);
                }
            }

            member.setDocuments(documents);
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
                                .documentIds(m.getDocuments().stream()
                                        .map(MemberDocument::getId)
                                        .toList())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public BookingStatusResponse getBookingStatus(UUID bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (!booking.getUserId().equals(userId)){
            throw new AccessDeniedException(messageLang.getMessage("error.access.denied"));
        }
        return new BookingStatusResponse(booking.getStatus());
    }

    @Transactional
    @Override
    public void confirmBookingAfterPayment(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (booking.getStatus() == BookingStatus.CONFIRMED) return;
        if (booking.getStatus() != BookingStatus.READY_FOR_PAYMENT) {
            throw new StatusException(messageLang.getMessage("error.status.reserve"));
        }
        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new StatusException(messageLang.getMessage("error.status.time"));
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        //qui aggiungerò un qualche evento
    }

    //metodi caso d'insuccesso

    @Transactional
    @Override
    public void deleteBooking(UUID bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", bookingId)));
        if (!booking.getUserId().equals(userId)){
            throw new AccessDeniedException(messageLang.getMessage("error.access.denied"));
        }
        if (booking.getStatus() == BookingStatus.RESERVE_CONFIRMED || booking.getStatus() == BookingStatus.READY_FOR_PAYMENT) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            ReserveSeatCommand command = new ReserveSeatCommand(
                    booking.getId(),
                    booking.getTravelId(),
                    booking.getActivityId(),
                    booking.getPeopleCount()
            );
            if (booking.getTravelId() != null && booking.getActivityId() == null) {
                rabbitTemplate.convertAndSend("travel.release.queue", command);
            } else if (booking.getTravelId() == null && booking.getActivityId() != null)
                rabbitTemplate.convertAndSend("activity.release.queue", command);
        }
        //qui aggiungerò un qualche evento
    }

    @Override
    public void updateBookingIfAllDocumentsUploaded(Booking booking) {
        if (booking.getMembers()==null) return;
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