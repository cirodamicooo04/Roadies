package it.roadies.api_gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    @GetMapping("/travel")
    public Mono<ResponseEntity<String>> travel_fallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Travels / activities unavailable"));
    }

    @GetMapping("/booking")
    public Mono<ResponseEntity<String>> booking_fallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Bookings unavailable"));
    }

    @GetMapping("/chat")
    public Mono<ResponseEntity<String>> chat_fallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Chat unavailable"));
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<String>> user_fallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("User details unavailable"));
    }

    @GetMapping("/review")
    public Mono<ResponseEntity<String>> review_fallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Reviews unavailable"));
    }
}
