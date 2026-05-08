package it.roadies.booking_service.exceptions;

public class StatusException extends RuntimeException {
    public StatusException(String message) {
        super(message);
    }
}
