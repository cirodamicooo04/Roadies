package it.roadies.travel_service.controller.client;

import io.github.resilience4j.retry.annotation.Retry;
import it.roadies.travel_service.conf.FeignConfiguration;
import it.roadies.travel_service.controller.fallback.BookingClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "booking-service", configuration = FeignConfiguration.class, fallback = BookingClientFallback.class)
public interface BookingClient {
    @Retry(name = "booking-service")
    @GetMapping("/api/v1/bookings/users/me")
    List<UUID> getUserBookings();
}
