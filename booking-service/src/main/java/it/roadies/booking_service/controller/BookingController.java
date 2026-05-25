package it.roadies.booking_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import it.roadies.booking_service.data.dto.response.BookingStep2Response;
import it.roadies.booking_service.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Gestione Prenotazioni", description = "API per la creazione, la visualizzazione e la gestione delle prenotazioni dei viaggi")
public class BookingController {
    private final BookingService bookingService;

    //TODO: devo eliminarlo prima della consegna
    @GetMapping("/debug")
    public Map<String, Object> debugToken(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }

    @Operation(summary = "Crea una bozza di prenotazione (passo 1)", description = "Inizializza una nuova prenotazione in stato di bozza")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bozza creata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati della richiesta non validi"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/draft")
    public ResponseEntity<BookingDraftResponse> createDraft(@Valid @RequestBody BookingDraftRequest request, @AuthenticationPrincipal Jwt userJwt) {
        BookingDraftResponse response = bookingService.createDraft(request, userJwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(summary = "Richiedi riserva posti per un tempo pre-stabilito (passo 2)", description = "Permette di riservare i posti, se ancora disponibili, e iniziare il processo di prenotazione dopo la creazione della draft")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati della richiesta non validi"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @PreAuthorize("hasRole('TRAVELER')")
    @PutMapping("/pending")
    public ResponseEntity<Void> createPendingAndReserveSeats(@Valid @RequestBody BookingCreateRequest request, @AuthenticationPrincipal Jwt userJwt) {
        bookingService.createPendingAndReserveSeats(request, userJwt.getSubject());
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Inizializza membri", description = "Permette di inizializzare la lista dei membri di una prenotazione")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati della richiesta non validi"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
            @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/members")
    public ResponseEntity<BookingStep2Response> insertMembers(@Valid @RequestBody BookingMemberRequest request, @AuthenticationPrincipal Jwt userJwt) {
        BookingStep2Response response = bookingService.insertMembers(request, userJwt.getSubject());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Ottieni lo stato della prenotazione", description = "Restituisce lo stato attuale di una prenotazione specifica tramite il suo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stato recuperato con successo"),
            @ApiResponse(responseCode = "404", description = "Prenotazione non trovata"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatusResponse> getStatus(@PathVariable UUID bookingId, @AuthenticationPrincipal Jwt userJwt) {
        return ResponseEntity.ok(bookingService.getBookingStatus(bookingId, userJwt.getSubject()));
    }


    //@Operation(summary = "Conferma una prenotazione", description = "Permette di confermare una prenotazione dopo il pagamento")
//    @PreAuthorize("hasRole('TRAVELER')")
//    @PatchMapping("/{bookingId}/confirm")
//    public ResponseEntity<Void> confirmBooking(@PathVariable UUID bookingId) {
//        bookingService.confirmBookingAfterPayment(bookingId);
//        return ResponseEntity.noContent().build();
//    }

    @Operation(summary = "Elimina una prenotazione", description = "Permette l'eleminazione una prenotazione precedenetemente creata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prenotazione eliminata con successo"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId, @AuthenticationPrincipal Jwt userJwt) {
        bookingService.deleteBooking(bookingId, userJwt.getSubject(), userJwt.getClaimAsString("email"));
        return ResponseEntity.noContent().build();
    }

    //TRAVEL SERVICE RECOMMENDATION
    @Operation(summary = "Ottieni tutte le prenotazioni", description = "Permette di ottenere tutte le prenotazioni dell'utente che ne fa richiesta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prenotazioni restituite con successo"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/users/me")
    public ResponseEntity<List<UUID>> getBookingsFromUser(@AuthenticationPrincipal Jwt jwt){
        List<UUID> travelsIds = bookingService.getUserBookings(jwt.getClaim("sub"));
        return ResponseEntity.ok(travelsIds);
    }
}
