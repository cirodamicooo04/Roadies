package it.roadies.booking_service.exceptions;

public class MinioException extends RuntimeException {
    public MinioException(String message) {
        super(message);
    }
}
