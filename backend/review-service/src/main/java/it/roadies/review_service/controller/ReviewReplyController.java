package it.roadies.review_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.dto.ReplyUpdateRequest;
import it.roadies.review_service.data.entity.ReviewReply;
import it.roadies.review_service.service.impl.ReviewReplyServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews/replies")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyServiceImpl replyService;

    @PostMapping("/{reviewId}")
    @Operation(
            summary = "Crea una nuova risposta a una recensione",
            description = "Permette a un utente autenticato di creare una risposta (commento) a una recensione specifica, identificata dal suo ID."
    )
    public ResponseEntity<Void> createReply(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReplyRequest request) {

        replyService.createReply(request, reviewId, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get the reply for a specific review based on the review's id
    // GET /reviews/replies/{reviewId}
    @GetMapping("/{reviewId}")
    @Operation(
            summary = "Recupera la risposta a una recensione",
            description = "Restituisce la risposta (commento) associata a una recensione specifica, identificata dal suo ID."
    )
    public ResponseEntity<ReplyResponse> getReplyByReviewId(@PathVariable UUID reviewId) {
        ReplyResponse reply = replyService.getReplyByReviewId(reviewId);
        return ResponseEntity.ok(reply);
    }

    // Edit the content of a reply based on the reply's id
    // PUT /reviews/replies/id/{replyId}
    @PutMapping("/replyId/{replyId}")
    @Operation(
            summary = "Modifica una risposta a una recensione",
            description = "Permette a un utente autenticato di modificare il contenuto di una risposta (commento) esistente, identificata dal suo ID."
    )
    public ResponseEntity<ReplyUpdateRequest> updateReply(
            @PathVariable UUID replyId,
            @Valid @RequestBody ReplyUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        replyService.updateReply(replyId, request, jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    // delete the reply based on the reply's id
    // DELETE /reviews/replies/id/{replyId}
    @Operation(
            summary = "Elimina una risposta a una recensione",
            description = "Permette a un utente autenticato di eliminare una risposta (commento) esistente, identificata dal suo ID."
    )
    @DeleteMapping("/replyId/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable UUID replyId, @AuthenticationPrincipal Jwt jwt) {
        replyService.deleteReply(replyId, jwt.getSubject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}