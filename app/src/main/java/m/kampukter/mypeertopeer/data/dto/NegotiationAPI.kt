package m.kampukter.mypeertopeer.data.dto

import m.kampukter.mypeertopeer.data.NegotiationEvent

interface NegotiationAPI {
    fun connect()
    fun disconnect()
    fun sendOffer(to: String, sdp: String)
    fun sendAnswer(to: String, sdp: String)
    fun sendCandidate(to: String, sdp: String, sdpMid: String, sdpMLineIndex: Int)

    var onNegotiationEvent: ((NegotiationEvent) -> Unit)?

}