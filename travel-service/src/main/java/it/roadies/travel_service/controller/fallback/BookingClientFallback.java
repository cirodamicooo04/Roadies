package it.roadies.travel_service.controller.fallback;

import it.roadies.travel_service.controller.client.BookingClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class BookingClientFallback implements BookingClient {
    @Override
    public List<UUID> getUserBookings() {
        log.info("Fallback method called for getUserBookings");
        return Collections.emptyList();
    }
}
