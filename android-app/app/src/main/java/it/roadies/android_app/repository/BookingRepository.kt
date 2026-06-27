package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.booking.GestioneDocumentiApi
import it.roadies.android_app.client.apis.booking.GestionePrenotazioniApi
import it.roadies.android_app.client.models.booking.BookingCreateRequest
import it.roadies.android_app.client.models.booking.BookingDraftResponse
import it.roadies.android_app.client.models.booking.BookingHomeResponse
import it.roadies.android_app.client.models.booking.BookingMemberRequest
import it.roadies.android_app.client.models.booking.BookingStatusResponse
import it.roadies.android_app.client.models.booking.BookingStep2Response
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import it.roadies.android_app.client.apis.booking.GestionePagamentoApi
import it.roadies.android_app.client.models.booking.PaymentRequest
import it.roadies.android_app.client.models.booking.PaymentResponse
import okhttp3.MultipartBody
import androidx.core.net.toUri
import it.roadies.android_app.client.models.booking.BookingDraftRequest
import it.roadies.android_app.model.dao.BookingDao
import it.roadies.android_app.model.mappers.toEntity

@Singleton
class BookingRepository @Inject constructor (private val prenotazioniApi: GestionePrenotazioniApi, private val documentiApi: GestioneDocumentiApi, private val pagamentoApi: GestionePagamentoApi, private val bookingDao: BookingDao, @ApplicationContext private val context: Context) {
    fun activeBookingsFlow(now:LocalDateTime= LocalDateTime.now()) = bookingDao.getActiveBookingsFlow(now)
    fun pastBookingsFlow(now:LocalDateTime= LocalDateTime.now()) = bookingDao.getPastBookingsFlow(now)

    suspend fun createDraft(request: BookingDraftRequest): ApiResponse<BookingDraftResponse> {
        return safeApiCall { prenotazioniApi.createDraft(request) }
    }

    suspend fun createPendingAndReserveSeats(request: BookingCreateRequest): ApiResponse<Unit> {
        return safeApiCall { prenotazioniApi.createPendingAndReserveSeats(request) }
    }

    suspend fun getStatus(bookingId: UUID): ApiResponse<BookingStatusResponse> {
        return safeApiCall { prenotazioniApi.getStatus(bookingId) }
    }

    suspend fun insertMembers(request: BookingMemberRequest): ApiResponse<BookingStep2Response> {
        return safeApiCall { prenotazioniApi.insertMembers(request) }
    }

    suspend fun deleteBooking(bookingId: UUID): ApiResponse<Unit> {
        val response = safeApiCall { prenotazioniApi.deleteBooking(bookingId) }

        if (response.success) {
            bookingDao.deleteBookingById(bookingId)
        }
        return response
    }

    suspend fun getBookingsFromUser(): ApiResponse<List<UUID>> {
        return safeApiCall { prenotazioniApi.getBookingsFromUser() }
    }

    suspend fun getPastBookings(page: Int? = null, size: Int? = null): ApiResponse<PageResponse<BookingHomeResponse>> {
        val response = safeApiCall { prenotazioniApi.getPastBookingsFromUser(page, size) }

        if (response.success) {
            response.data?.content?.let { networkList ->
                val entities = networkList.map { it.toEntity() }
                bookingDao.insertAllBookings(entities)
            }
        }
        return response
    }

    suspend fun uploadDocumentPhoto(documentId: UUID, uriString: String): ApiResponse<String> {
        return safeApiCall {
            val uri = uriString.toUri()
            val inputStream = context.contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open URI: $uriString")
            val bytes = inputStream.use { it.readBytes() }
            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "document.jpg", requestBody)
            documentiApi.uploadDocumentPhoto(documentId, part)
        }
    }

    suspend fun createPaymentIntent(request: PaymentRequest): ApiResponse<PaymentResponse> {
        return safeApiCall { pagamentoApi.createPaymentIntent(request) }
    }

    suspend fun getActiveBookings(page: Int? = null, size: Int? = null): ApiResponse<PageResponse<BookingHomeResponse>> {
        val response = safeApiCall { prenotazioniApi.getActiveBookingsFromUser(page, size) }

        if (response.success) {
            response.data?.content?.let { networkList ->
                val entities = networkList.map { it.toEntity() }
                bookingDao.insertAllBookings(entities)
            }
        }
        return response
    }
}