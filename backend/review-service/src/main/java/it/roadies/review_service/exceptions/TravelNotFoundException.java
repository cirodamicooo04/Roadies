package it.roadies.review_service.exceptions;

public class TravelNotFoundException extends RuntimeException {
    public TravelNotFoundException(String message) {
        super(message);
    }
}
