package m.kampukter.mypeertopeer.data.dto

import androidx.lifecycle.LiveData
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.NegotiationMessage
import m.kampukter.mypeertopeer.data.UserShort
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URL

interface NegotiationAPI {

    var onNegotiationEvent: ((NegotiationEvent) -> Unit)?

    fun connect()

    fun disconnect()

    fun sendOffer(to: String, sdp: String)
    fun sendAnswer(to: String, sdp: String)
    fun sendCandidate(to: String, sdp: String, sdpMid: String, sdpMLineIndex: Int)
    fun sendNewUser(user: UserShort)
    fun sendUpdateUser(user: UserShort)
    fun sendInvitation(to: String)

}