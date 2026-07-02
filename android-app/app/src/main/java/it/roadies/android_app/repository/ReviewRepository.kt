package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.review.ReviewApi
import it.roadies.android_app.client.apis.review.ReviewReplyApi
import it.roadies.android_app.client.models.review.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewApi: ReviewApi,
    private val reviewReplyApi: ReviewReplyApi
) {

    // --- Review operations ---

    // Create a new review
    suspend fun createReview(travelId: UUID, request: ReviewRequest) =
        reviewApi.createReview(travelId, request)

    // Get all reviews for a travel
    suspend fun getReviews(travelId: UUID) = reviewApi.getReviewsByTravel(travelId)

    // Get average rating for a travel
    suspend fun getAverageRating(travelId: UUID) = reviewApi.getAverageRating(travelId)

    // Update an existing review
    suspend fun updateReview(reviewId: UUID, request: ReviewUpdateRequest) =
        reviewApi.updateReview(reviewId, request)

    // Delete a review
    suspend fun deleteReview(reviewId: UUID) = reviewApi.deleteReview(reviewId)

    // --- Reply operations ---

    // Create a reply to a review
    suspend fun createReply(reviewId: UUID, request: ReplyRequest) =
        reviewReplyApi.createReply(reviewId, request)

    // Get reply for a specific review
    suspend fun getReply(reviewId: UUID) = reviewReplyApi.getReply(reviewId)

    // Update a reply
    suspend fun updateReply(replyId: UUID, request: ReplyRequest) =
        reviewReplyApi.updateReply(replyId, request)

    // Delete a reply
    suspend fun deleteReply(replyId: UUID) = reviewReplyApi.deleteReply(replyId)
}