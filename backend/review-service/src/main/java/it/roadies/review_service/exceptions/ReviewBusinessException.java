package it.roadies.review_service.exceptions;

public class ReviewBusinessException extends RuntimeException {
    public ReviewBusinessException(String message) {
        super(message);
    }
}
