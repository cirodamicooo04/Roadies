package it.roadies.review_service.exceptions;

public class ReplyNotFoundException extends RuntimeException {
    public ReplyNotFoundException(String message) {
        super(message);
    }
}
