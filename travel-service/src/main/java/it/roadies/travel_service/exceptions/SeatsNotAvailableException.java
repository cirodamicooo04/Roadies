package it.roadies.travel_service.exceptions;

public class SeatsNotAvailableException extends RuntimeException {
  public SeatsNotAvailableException(String message) {
    super(message);
  }
}
