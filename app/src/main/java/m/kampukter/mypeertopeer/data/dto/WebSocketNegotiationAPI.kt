package m.kampukter.mypeertopeer.data.dto

import android.util.Log
import com.google.gson.Gson
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.NegotiationMessage
import m.kampukter.mypeertopeer.ui.myName
import okhttp3.*
import java.util.concurrent.TimeUnit


class WebSocketNegotiationAPI : NegotiationAPI {

    private var webSocket: WebSocket? = null

    override var onNegotiationEvent: ((NegotiationEvent) -> Unit)? = null
    override var onNegotiationEventMain: ((NegotiationEvent) -> Unit)? = null

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
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val message = gson.fromJson(text, NegotiationMessage::class.java)
            //Log.d("blablabla", "WS  ${message.type} ${message.to}")
            when (message.type) {
                "discovery" -> message.ids?.let {
                    /*onNegotiationEvent?.invoke(
                        NegotiationEvent.Discovery(it)
                    )*/
                    onNegotiationEventMain?.invoke(
                        NegotiationEvent.Discovery(it)
                    )
                }
                "offer" -> {
                    val from = message.from
                    val sdp = message.sdp
                    if (from == null || sdp == null) return
                    onNegotiationEvent?.invoke(NegotiationEvent.Offer(from, "", sdp))
                    onNegotiationEventMain?.invoke(NegotiationEvent.Offer(from, "", sdp))
                }
                "answer" -> message.sdp?.let {
                    onNegotiationEvent?.invoke(
                        NegotiationEvent.Answer("", "", it)
                    )
                }
                "serverUrl" -> {
                    val sdp = message.sdp
                    val sdpMid = message.sdpMid
                    if (sdp == null || sdpMid == null) return
                    onNegotiationEvent?.invoke(
                        NegotiationEvent.IceCandidate(
                            "",
                            "",
                            sdp,
                            sdpMid,
                            message.sdpMLineIndex
                        )
                    )
                }
            }

        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("blablabla", "WS  Connected")
        }
    }

    override fun connect() {
        webSocket = okHttpClient.newWebSocket(
                        Request.Builder()
                        .url("ws://176.37.84.130:8080/$myName")
                        .build(),
                        webSocketListener
                    )

    }

    override fun disconnect() {
        webSocket?.close(1000, null)
    }


    override fun send( dataObject: NegotiationMessage) {
        webSocket?.send(gson.toJson(dataObject))
    }

    override fun sendOffer(to: String, sdp: String) {
        webSocket?.send(gson.toJson(NegotiationMessage(to = to, from = myName, type = NegotiationMessage.TYPE_OFFER, sdp = sdp)))
    }

    override fun sendAnswer(to: String, sdp: String) {
        webSocket?.send(gson.toJson(NegotiationMessage(to = to, type = NegotiationMessage.TYPE_ANSWER, sdp = sdp)))
    }

    override fun sendCandidate(to: String, sdp: String, sdpMid: String, sdpMLineIndex: Int) {
        webSocket?.send(gson.toJson(NegotiationMessage(to = to, type = NegotiationMessage.TYPE_CANDIDATE, sdp = sdp, sdpMid = sdpMid, sdpMLineIndex = sdpMLineIndex)))
    }

}