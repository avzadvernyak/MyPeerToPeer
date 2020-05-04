package m.kampukter.mypeertopeer.data.dto

import android.util.Log
import androidx.lifecycle.LifecycleService
import com.google.gson.Gson
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.NegotiationMessage
import m.kampukter.mypeertopeer.data.UserData
import m.kampukter.mypeertopeer.myId
import okhttp3.*
import java.util.concurrent.TimeUnit


class WebSocketNegotiationAPI : NegotiationAPI, LifecycleService() {

    private var webSocket: WebSocket? = null

    override var onNegotiationEvent: ((NegotiationEvent) -> Unit)? = null

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val webSocketListener = object : WebSocketListener() {

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            Log.d("blablabla", "WS  Disconnected")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.d("blablabla", "WS  Failure ${t.message}")
            onNegotiationEvent?.invoke(NegotiationEvent.HangUp)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val message = gson.fromJson(text, NegotiationMessage::class.java)
            //Log.d("blablabla", "WS  ${message.type} ${message.to}")
            when (message.type) {
                NegotiationMessage.TYPE_DISCOVERY -> message.ids?.let {
                    onNegotiationEvent?.invoke(
                        NegotiationEvent.Discovery(it)
                    )
                }
                NegotiationMessage.TYPE_OFFER -> {
                    val from = message.from
                    val sdp = message.sdp
                    if (from == null || sdp == null) return
                    onNegotiationEvent?.invoke(NegotiationEvent.Offer(from, "", sdp))
                }
                NegotiationMessage.TYPE_ANSWER -> message.sdp?.let {
                    onNegotiationEvent?.invoke(
                        NegotiationEvent.Answer("", "", it)
                    )
                }
                NegotiationMessage.TYPE_CANDIDATE -> {
                    val sdp = message.sdp
                    val sdpMid = message.sdpMid
                    val sdpMLineIndex = message.sdpMLineIndex
                    if (sdp == null || sdpMid == null||sdpMLineIndex==null) return
                    onNegotiationEvent?.invoke(
                        NegotiationEvent.IceCandidate(
                            "",
                            "",
                            sdp,
                            sdpMid,
                            sdpMLineIndex
                        )
                    )
                }
                NegotiationMessage.TYPE_USERS ->  message.users?.let {
                    onNegotiationEvent?.invoke(
                        NegotiationEvent.Users(it)
                    )
                }
            }

        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("blablabla", "WS  Connected")
        }
    }

    override fun connect() {
        Log.d("blablabla", "WS  Connecting...")
        //.url("ws://176.37.84.130:8080/$myId")
        //.url("ws://192.168.0.69:8080/$myId")
        //.url("ws://109.254.66.131:8080/$myId")
        webSocket = okHttpClient.newWebSocket(
            Request.Builder()
                //.url("ws://176.37.84.130:9517/$myId")
                .url("ws://192.168.0.100:8080/$myId")
                //.url("ws://109.254.66.131:8080/$myId")
                .build(),
            webSocketListener
        )

    }

    override fun disconnect() {
        webSocket?.close(1000, null)
    }

    override fun sendOffer(to: String, sdp: String) {
        webSocket?.send(
            gson.toJson(
                NegotiationMessage(
                    to = to,
                    from = myId,
                    type = NegotiationMessage.TYPE_OFFER,
                    sdp = sdp
                )
            )
        )
    }

    override fun sendAnswer(to: String, sdp: String) {
        webSocket?.send(
            gson.toJson(
                NegotiationMessage(
                    to = to,
                    type = NegotiationMessage.TYPE_ANSWER,
                    sdp = sdp
                )
            )
        )
    }

    override fun sendCandidate(to: String, sdp: String, sdpMid: String, sdpMLineIndex: Int) {
        webSocket?.send(
            gson.toJson(
                NegotiationMessage(
                    to = to,
                    type = NegotiationMessage.TYPE_CANDIDATE,
                    sdp = sdp,
                    sdpMid = sdpMid,
                    sdpMLineIndex = sdpMLineIndex
                )
            )
        )
    }

    override fun sendNewUser(user: UserData) {
        webSocket?.send(
            gson.toJson(
                NegotiationMessage(
                    type = NegotiationMessage.TYPE_NEW_USER,
                    user = user
                )
            )
        )
    }

    override fun sendUpdateUser(user: UserData) {
        webSocket?.send(
            gson.toJson(
                NegotiationMessage(
                    type = NegotiationMessage.TYPE_UPDATE_USER,
                    user = user
                )
            )
        )
    }

    override fun sendInvitation(to: String) {
        webSocket?.send(
            gson.toJson(
                NegotiationMessage(
                    type = NegotiationMessage.TYPE_INVITATION,
                    to = to
                )
            )
        )
    }
}