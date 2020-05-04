package m.kampukter.mypeertopeer.data.dto

import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.UserData

interface NegotiationAPI {

    var onNegotiationEvent: ((NegotiationEvent) -> Unit)?

    fun connect()

    fun disconnect()

    fun sendOffer(to: String, sdp: String)
    fun sendAnswer(to: String, sdp: String)
    fun sendCandidate(to: String, sdp: String, sdpMid: String, sdpMLineIndex: Int)
    fun sendNewUser(user: UserData)
    fun sendUpdateUser(user: UserData)
    fun sendInvitation(to: String)

}