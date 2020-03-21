package m.kampukter.mypeertopeer.data.dto

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import m.kampukter.mypeertopeer.data.NegotiationMessage
import okhttp3.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URL
import java.util.concurrent.TimeUnit

fun WebSocket.getUrl(): URL = request().url().url()

class MySignalingWebSocket : SignalingWebSocketAPI {

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSockets = mutableMapOf<URL, WebSocket>()
    private val connectionStatusLiveDatas =
        mutableMapOf<URL, MutableLiveData<SignalingWebSocketAPI.ConnectionStatus>>()
    private val messageLiveDatas =
        mutableMapOf<URL, MutableLiveData<SignalingWebSocketAPI.Message>>()

    val gson = Gson()

    private val webSocketListener = object : WebSocketListener() {

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(SignalingWebSocketAPI.ConnectionStatus.Disconnected)
            webSockets.remove(webSocket.getUrl())

            /*isDisconnect[webSocket.getUrl()]?.let {
                if (!it) disconnect(webSocket.getUrl())
            }*/
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {

        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(
                SignalingWebSocketAPI.ConnectionStatus.Failed(
                    t.message
                )
            )
            webSockets.remove(webSocket.getUrl())
        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(SignalingWebSocketAPI.ConnectionStatus.Connected)
            val message = gson.fromJson(text, NegotiationMessage::class.java)
            //Log.d("blablabla", "Receive ${message.type}")
            //Log.d("blablabla", "Receive ${message.type} == ${messageLiveDatas[webSocket.getUrl()]}")
            //Log.d("blablabla", "Receive ${message.type} == ${messageLiveDatas[webSocket.getUrl()]?.hasActiveObservers()}")
            Log.d("blablabla", "Receive ${message.type} == ${messageLiveDatas[webSocket.getUrl()]}")
            messageLiveDatas[webSocket.getUrl()]?.postValue(
                when (message.type) {
                    "offer" -> {
                        Log.d("blablabla", "Receive ${message.type} Поняли ")
                        SignalingWebSocketAPI.Message.OfferReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                message.sdp
                            )
                        )
                    }
                    "answer" -> {
                        SignalingWebSocketAPI.Message.AnswerReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                message.sdp
                            )
                        )
                    }
                    "serverUrl" -> {
                        SignalingWebSocketAPI.Message.IceCandidateReceived(
                            IceCandidate(
                                message.sdpMid,
                                message.sdpMLineIndex,
                                message.sdp
                            )
                        )
                    }
                    "discovery" -> {
                        SignalingWebSocketAPI.Message.DiscoveryReceived(message.ids ?: listOf())
                    }
                    else -> SignalingWebSocketAPI.Message.Text(message.toString())
                }
            )

        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(SignalingWebSocketAPI.ConnectionStatus.Connected)
        }
    }

    override fun connect(url: URL) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectionStatusLiveDatas.putIfAbsent(url,
                MutableLiveData<SignalingWebSocketAPI.ConnectionStatus>()
                    .apply { postValue(SignalingWebSocketAPI.ConnectionStatus.Connecting) })
            messageLiveDatas.putIfAbsent(url, MutableLiveData())
        } else {
            if (connectionStatusLiveDatas.containsKey(url)) {
                if (connectionStatusLiveDatas[url] == null) connectionStatusLiveDatas[url]?.postValue(
                    SignalingWebSocketAPI.ConnectionStatus.Connecting
                )
            } else {
                connectionStatusLiveDatas[url] =
                    MutableLiveData<SignalingWebSocketAPI.ConnectionStatus>()
                        .apply {
                            postValue(SignalingWebSocketAPI.ConnectionStatus.Connecting)
                        }
            }

            if (messageLiveDatas.containsKey(url)) {
                if (messageLiveDatas[url] == null) messageLiveDatas[url] = MutableLiveData()
            } else messageLiveDatas[url] = MutableLiveData()
        }
        if (!webSockets.containsKey(url)) {
            connectionStatusLiveDatas[url]?.postValue(
                SignalingWebSocketAPI.ConnectionStatus.Connecting
            )
            webSockets[url] = okHttpClient.newWebSocket(
                Request.Builder().url(url).build(),
                webSocketListener
            )
        }
    }

    override fun disconnect(url: URL) {
        webSockets[url]?.close(1000, null)
        webSockets.remove(url)
        connectionStatusLiveDatas[url]?.postValue(SignalingWebSocketAPI.ConnectionStatus.Closing)
    }


    override fun send(url: URL, dataObject: NegotiationMessage) {
        Log.d("blablabla", "Send ${dataObject.type}")
        webSockets[url]?.send(gson.toJson(dataObject))
    }

    override fun getConnectStatusLiveData(url: URL): LiveData<SignalingWebSocketAPI.ConnectionStatus>? =
        connectionStatusLiveDatas[url]

    override fun getMessageLiveData(url: URL): LiveData<SignalingWebSocketAPI.Message>? =
        messageLiveDatas[url]

}