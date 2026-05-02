package it.roadies.travel_service.controller.fallback;

import it.roadies.travel_service.controller.client.BookingClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class BookingClientFallback implements BookingClient {
    @Override
    public List<UUID> getUserBookings() {
        return Collections.emptyList();
    }
}
