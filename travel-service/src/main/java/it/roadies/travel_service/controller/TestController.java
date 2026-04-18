package it.roadies.travel_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/travels")
public class TestController {
    @GetMapping("/test")
    public String test() throws InterruptedException {
        return "Hello World!";
    }

    @GetMapping("/public/test")
    public String publicTest() {
        return "Public Hello World!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/private/test")
    public String privateTest(@AuthenticationPrincipal Jwt jwt) {
        return "Private Hello World from " + jwt.getClaim("preferred_username");
    }
}
