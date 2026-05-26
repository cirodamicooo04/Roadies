package it.roadies.review_service.service.impl;

import it.roadies.review_service.conf.i8n.MessageLang;
import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.entity.ReviewReply;
import it.roadies.review_service.data.dao.ReviewRepository;
import it.roadies.review_service.data.dao.ReviewReplyRepository;
import it.roadies.review_service.data.mapper.ReplyMapper;
import it.roadies.review_service.exceptions.AccessDeniedException;
import it.roadies.review_service.exceptions.ReplyNotFoundException;
import it.roadies.review_service.exceptions.ReviewBusinessException;
import it.roadies.review_service.exceptions.ReviewNotFoundException;
import it.roadies.review_service.service.ReviewReplyService;
import it.roadies.review_service.service.client.TravelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private final ReplyMapper replyMapper;
    private final ReviewReplyRepository replyRepository;
    private final ReviewRepository reviewRepository;
    private final MessageLang messageLang;
    private final TravelService travelServiceClient;

    // Create a new reply for a specific review
    @Transactional
    @Override
    public void createReply(ReplyRequest request, UUID reviewId, String userId) {
        log.info("provo a creare una risposta ad una recensione - reviewId: {} userId: {}", reviewId, userId);

        // First validation: Ensure the original review exists before allowing a reply to be created
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->new ReviewNotFoundException(messageLang.getMessage("error.resource.not.found")));

        travelServiceClient.verifyTravelExists(review.getTravelId(), review.getReviewType());

        // Second validation: Ensure that a reply does not already exist for this review (enforcing the one-to-one relationship)
        if (replyRepository.existsByReviewId(reviewId)) {
            log.error("tentativo di creare una risposta ad una recensione che ha già una risposta - reviewId: {}", reviewId);
            throw new ReviewBusinessException(messageLang.getMessage("review.reply.already.exists"));
        }

        // If both validations pass, proceed to create and save the new reply
        ReviewReply reply = replyMapper.toEntity(request, userId);
        reply.setReview(review);
        replyRepository.save(reply);
            log.info("risposta ad una recensione creata con successo - reviewId: {} userId: {}", reviewId, userId);
    }

    // Get the reply for a specific review by the review's ID
    @Override
    public ReplyResponse getReplyByReviewId(UUID reviewId) {
        log.info("provo a recuperare la risposta ad una recensione - reviewId: {}", reviewId);
        return replyRepository.findByReviewId(reviewId)
                .map(replyMapper::toReplyResponse)
                .orElseThrow(() -> new ReplyNotFoundException(messageLang.getMessage("review.not.found")));
    }

    // Edit the content of an existing reply by its ID
    @Transactional
    @Override
    public ReviewReply updateReply(UUID replyId, ReplyRequest request, String userId) {
        log.info("provo ad aggiornare una risposta di una recensione - replyId: {} userId: {}", replyId, userId);
        ReviewReply existingReply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyNotFoundException(messageLang.getMessage("review.reply.not.found",replyId)));
        // Ensure that only the user who created the reply can edit it
        if (!existingReply.getUserId().equals(userId)) {
            log.error("tentativo di aggiornare una risposta ad una recensione da parte di un utente non autorizzato - replyId: {} userId: {}", replyId, userId);
            throw new AccessDeniedException("l'utente" + userId + "ha tentato di accedere ad una risorsa non autorizzato");
        }
        existingReply.setContent(request.getContent());
        log.info("risposta ad una recensione aggiornata con successo - replyId: {} userId: {}", replyId, userId);
        return replyRepository.save(existingReply);
    }

    // Delete a reply by its ID
    @Transactional
    @Override
    public void deleteReply(UUID replyId, String userId) {
        log.info("provo a cancellare una risposta di una recensione - replyId: {} userId: {}", replyId, userId);
        if (!replyRepository.existsById(replyId)) {
            log.error("tentativo di cancellare una risposta ad una recensione che non esiste - replyId: {}", replyId);
            throw new ReplyNotFoundException(messageLang.getMessage("review.reply.not.found", replyId));
        }
        // Ensure that only the user who created the reply can edit it
        if (!replyRepository.existsByUserId(userId)) {
            log.error("tentativo di cancellare una risposta ad una recensione da parte di un utente non autorizzato - replyId: {} userId: {}", replyId, userId);
            throw new AccessDeniedException("l'utente" + userId + "ha tentato di accedere ad una risorsa non autorizzato");
        }
        replyRepository.deleteById(replyId);
        log.info("risposta ad una recensione cancellata con successo - replyId: {} userId: {}", replyId, userId);
    }
}