package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.review.ReviewApi
import it.roadies.android_app.client.apis.review.ReviewReplyApi
import it.roadies.android_app.client.models.review.*
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
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
    suspend fun createReview(travelId: UUID, request: ReviewRequest): ApiResponse<Void> =
        safeApiCall { reviewApi.createReview(travelId, request) }

    // Get all reviews for a travel
    suspend fun getReviews(travelId: UUID): ApiResponse<List<ReviewResponse>> =
        safeApiCall { reviewApi.getReviewsByTravel(travelId) }

    // Get average rating for a travel
    suspend fun getAverageRating(travelId: UUID): ApiResponse<Double> =
        safeApiCall { reviewApi.getAverageRating(travelId) }

    // Update an existing review
    suspend fun updateReview(reviewId: UUID, request: ReviewUpdateRequest): ApiResponse<Void> =
        safeApiCall { reviewApi.updateReview(reviewId, request) }

    // Delete a review
    suspend fun deleteReview(reviewId: UUID): ApiResponse<Void> =
        safeApiCall { reviewApi.deleteReview(reviewId) }

    // --- Reply operations ---

    // Create a reply to a review
    suspend fun createReply(reviewId: UUID, request: ReplyRequest): ApiResponse<Void> =
        safeApiCall { reviewReplyApi.createReply(reviewId, request) }

    // Get reply for a specific review
    suspend fun getReply(reviewId: UUID): ApiResponse<ReplyResponse> =
        safeApiCall { reviewReplyApi.getReply(reviewId) }

    // Update a reply
    suspend fun updateReply(replyId: UUID, request: ReplyRequest): ApiResponse<ReviewReply> =
        safeApiCall { reviewReplyApi.updateReply(replyId, request) }

    // Delete a reply
    suspend fun deleteReply(replyId: UUID): ApiResponse<Void> =
        safeApiCall { reviewReplyApi.deleteReply(replyId) }
}