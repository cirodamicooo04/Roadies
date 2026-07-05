package it.roadies.android_app.client.apis.review

import it.roadies.android_app.client.models.review.ReplyRequest
import it.roadies.android_app.client.models.review.ReplyResponse
import it.roadies.android_app.client.models.review.ReviewReply
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface ReviewReplyApi {

    // Create a reply to a review
    @POST("/api/v1/reviews/replies/{reviewId}")
    suspend fun createReply(
        @Path("reviewId") reviewId: UUID,
        @Body request: ReplyRequest
    ): Response<Void>

    // Get reply by review id
    @GET("/api/v1/reviews/replies/{reviewId}")
    suspend fun getReply(@Path("reviewId") reviewId: UUID): Response<ReplyResponse>

    // Update a reply
    @PUT("/api/v1/reviews/replies/replyId/{replyId}")
    suspend fun updateReply(
        @Path("replyId") replyId: UUID,
        @Body request: ReplyRequest
    ): Response<ReviewReply>

    // Delete a reply
    @DELETE("/api/v1/reviews/replies/replyId/{replyId}")
    suspend fun deleteReply(@Path("replyId") replyId: UUID): Response<Void>
}