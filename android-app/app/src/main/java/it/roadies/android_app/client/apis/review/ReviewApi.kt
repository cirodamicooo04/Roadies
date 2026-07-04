package it.roadies.android_app.client.apis.review

import it.roadies.android_app.client.models.review.ReviewRequest
import it.roadies.android_app.client.models.review.ReviewResponse
import it.roadies.android_app.client.models.review.ReviewUpdateRequest
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface ReviewApi {

    // Create a new review for a specific travel or activity
    @POST("/api/v1/reviews/{travelId}")
    suspend fun createReview(
        @Path("travelId") travelId: UUID,
        @Body request: ReviewRequest
    ): Response<Void>

    // Get all reviews for a specific travel
    @GET("/api/v1/reviews/public/{travelId}")
    suspend fun getReviewsByTravel(@Path("travelId") travelId: UUID): Response<List<ReviewResponse>>

    // Get average rating
    @GET("/api/v1/reviews/{travelId}/average")
    suspend fun getAverageRating(@Path("travelId") travelId: UUID): Response<Double>

    // Update an existing review
    @PUT("/api/v1/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: UUID,
        @Body request: ReviewUpdateRequest
    ): Response<ReviewUpdateRequest>

    // Delete a review
    @DELETE("/api/v1/reviews/{id}")
    suspend fun deleteReview(@Path("id") reviewId: UUID): Response<Void>
}