package it.roadies.travel_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/travels")
public class TestController {
    @GetMapping("/test")
    public String test() throws InterruptedException {
        Thread.sleep(100000);
        return "Hello World!";
    }
}
