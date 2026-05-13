package it.roadies.booking_service.exceptions;

import it.roadies.booking_service.data.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Field validation failed", message, request.getRequestURI());
    }

    @ExceptionHandler({BookingNotFoundException.class, DocumentNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Risorsa non trovata: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("Rotta non trovata: {}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Endpoint Not Found", "La rotta richiesta non esiste", request.getRequestURI());
    }

    @ExceptionHandler({StatusException.class, SeatsNotAvailableException.class})
    public ResponseEntity<ErrorResponse> handleBusinessExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Errore di logica di business: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Business Logic Error", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Accesso negato: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", "Non hai i permessi per accedere a questa risorsa.", request.getRequestURI());
    }

    @ExceptionHandler(com.stripe.exception.StripeException.class)
    public ResponseEntity<ErrorResponse> handleStripeException(com.stripe.exception.StripeException ex, HttpServletRequest request) {
        log.error("Errore di comunicazione con Stripe: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "Payment Gateway Error", "Impossibile elaborare il pagamento al momento.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Errore critico imprevisto sulla rotta {}: ", request.getRequestURI(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Si è verificato un errore imprevisto.", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
        return new ResponseEntity<>(response, status);
    }
}