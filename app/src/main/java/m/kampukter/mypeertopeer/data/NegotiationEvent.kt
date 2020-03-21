package m.kampukter.mypeertopeer.data

sealed class NegotiationEvent {

    data class Discovery(
        val userIds: List<String>
    ) : NegotiationEvent()

    data class Offer(
        val from: String,
        val to: String,
        val sdp: String
    ) : NegotiationEvent()

    data class Answer(
        val from: String,
        val to: String,
        val sdp: String
    ) : NegotiationEvent()

    data class IceCandidate(
        val from: String,
        val to: String,
        val sdp: String,
        val sdpMid: String,
        val sdpMLineIndex: Int
    ) : NegotiationEvent()

}