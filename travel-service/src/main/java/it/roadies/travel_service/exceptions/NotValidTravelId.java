package it.roadies.travel_service.exceptions;

public class NotValidTravelId extends RuntimeException {
    public NotValidTravelId(String message) {
        super(message);
    }
}
