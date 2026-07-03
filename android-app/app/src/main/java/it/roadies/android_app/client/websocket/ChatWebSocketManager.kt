package it.roadies.android_app.client.websocket

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import it.roadies.android_app.client.models.chat.MessageRequestDTO
import it.roadies.android_app.client.models.chat.MessageResponseDTO
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketManager @Inject constructor() {

    private var stompClient: StompClient? = null
    private val gson = Gson()
    private val compositeDisposable = CompositeDisposable()

    @SuppressLint("CheckResult")
    fun connect(
        token: String,
        conversationId: String,
        onMessageReceived: (MessageResponseDTO) -> Unit
    ) {
        // Usa l'IP dell'emulatore e l'endpoint STOMP corretto configurato nel backend
        val url = "ws://10.0.2.2:8086/ws/websocket"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        // Passiamo il JWT token per superare l'AuthChannelInterceptor del tuo backend
        val headers = listOf(StompHeader("Authorization", "Bearer $token"))

        // Ascoltiamo gli eventi di connessione
        val lifecycleDisp = stompClient!!.lifecycle().subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> Log.d("WebSocket", "Connessione STOMP aperta")
                LifecycleEvent.Type.ERROR -> Log.e("WebSocket", "Errore STOMP", lifecycleEvent.exception)
                LifecycleEvent.Type.CLOSED -> Log.d("WebSocket", "Connessione STOMP chiusa")
                else -> {}
            }
        }
        compositeDisposable.add(lifecycleDisp)

        // Iscriviamoci al topic specifico della conversazione
        val topicDisp = stompClient!!.topic("/topic/conversation/$conversationId")
            .subscribe({ topicMessage ->
                // Quando il backend invia un messaggio su questo topic, lo deserializziamo
                val message = gson.fromJson(topicMessage.payload, MessageResponseDTO::class.java)
                onMessageReceived(message)
            }, { error ->
                Log.e("WebSocket", "Errore nella sottoscrizione al topic", error)
            })
        compositeDisposable.add(topicDisp)

        // Avvia la connessione
        stompClient!!.connect(headers)
    }

    fun sendMessage(conversationId: String, content: String) {
        val request = MessageRequestDTO(
            conversationId = conversationId,
            content = content
        )
        val jsonPayload = gson.toJson(request)

        val sendDisp = stompClient?.send("/app/chat.sendMessage", jsonPayload)?.subscribe({
            Log.d("WebSocket", "Messaggio STOMP inviato con successo")
        }, { error ->
            Log.e("WebSocket", "Errore invio messaggio STOMP", error)
        })

        if (sendDisp != null) compositeDisposable.add(sendDisp)
    }

    fun disconnect() {
        stompClient?.disconnect()
        compositeDisposable.clear()
    }
}