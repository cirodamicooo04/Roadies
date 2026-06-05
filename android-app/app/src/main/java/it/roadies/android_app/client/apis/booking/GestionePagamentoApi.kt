package it.roadies.android_app.client.apis

import it.roadies.android_app.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.PaymentRequest
import it.roadies.android_app.client.models.PaymentResponse

interface GestionePagamentoApi {
    /**
     * POST api/v1/payments/create-payment-intent
     * Crea richiesta pagamento
     * Permette di creare una richiesta di pagamento
     * Responses:
     *  - 200: Richiesta di pagamento effettuata con successo
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param paymentRequest 
     * @return [PaymentResponse]
     */
    @POST("api/v1/payments/create-payment-intent")
    suspend fun createPaymentIntent(@Body paymentRequest: PaymentRequest): Response<PaymentResponse>

    /**
     * POST api/v1/payments/public/webhook
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param stripeSignature 
     * @param body 
     * @return [kotlin.String]
     */
    @POST("api/v1/payments/public/webhook")
    suspend fun handleStripeWebhook(@Header("Stripe-Signature") stripeSignature: kotlin.String, @Body body: kotlin.String): Response<kotlin.String>

}
