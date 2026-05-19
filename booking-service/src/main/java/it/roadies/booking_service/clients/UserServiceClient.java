package it.roadies.booking_service.clients;

import it.roadies.booking_service.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "travelService", url = "${user-service.url}", configuration = FeignConfiguration.class)
public interface UserServiceClient {
}
