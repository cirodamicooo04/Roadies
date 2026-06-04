package it.roadies.booking_service.jobs;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingCleanup {

    private final BookingRepository bookingRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanDraft(){
        LocalDateTime time = LocalDateTime.now().minusHours(24);
        bookingRepository.deleteByStatusAndCreatedAtBefore(BookingStatus.DRAFT, time);
    }

}
