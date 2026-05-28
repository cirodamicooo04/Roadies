package it.roadies.booking_service.exceptions;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
}
