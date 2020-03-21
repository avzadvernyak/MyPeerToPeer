package m.kampukter.mypeertopeer.data.dto

import androidx.lifecycle.LiveData
import m.kampukter.mypeertopeer.data.NegotiationMessage
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URL

interface SignalingWebSocketAPI {
    fun connect(url: URL)

    fun disconnect(url: URL)

    fun send(url: URL, dataObject: NegotiationMessage)

    fun getConnectStatusLiveData(url: URL): LiveData<ConnectionStatus>?

    fun getMessageLiveData(url: URL): LiveData<Message>?

    sealed class ConnectionStatus {
        object Connecting : ConnectionStatus()
        object Connected : ConnectionStatus()
        object Closing : ConnectionStatus()
        object Disconnected : ConnectionStatus()
        data class Failed(val reason: String?) : ConnectionStatus()
    }

    sealed class Message {
        data class IceCandidateReceived (val iceCandidate: IceCandidate) : Message()
        data class OfferReceived(val sessionDescription: SessionDescription) : Message()
        data class AnswerReceived(val sessionDescription: SessionDescription) : Message()
        data class SDPSend(val sessionDescription: SessionDescription) : Message()
        data class DiscoveryReceived(val content: List<String>) : Message()
        data class Text(val content: String) : Message()

    }
}