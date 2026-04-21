package it.roadies.booking_service.services.implementations;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.request.BookingRequestDTO;
import it.roadies.booking_service.data.dto.response.BookingResponseDTO;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.mapper.BookingMapper;
import it.roadies.booking_service.services.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO requestDto) {
        //devo riscrivere il metodo appena inizializziamo gli
//        try {
//            travelServiceClient.reserveSpots(requestDto.getTravelId(), requestDto.getPeopleCount());
//        } catch (Exception e) {
//            throw new RuntimeException("Impossibile riservare i posti: " + e.getMessage());
//        }

        Booking booking = bookingMapper.toEntity(requestDto);
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }
}
