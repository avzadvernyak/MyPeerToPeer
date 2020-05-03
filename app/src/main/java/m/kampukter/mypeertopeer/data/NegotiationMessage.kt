package m.kampukter.mypeertopeer.data

data class NegotiationMessage(
    val to: String? = null,
    val from: String? = null,
    val type: String,
    val sdp: String? = null,
    val ids: List<String>? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int = 0,
    val users: List<UserShort>? = null,
    val user: UserShort? = null
){
    companion object {
        const val TYPE_DISCOVERY = "discovery"
        const val TYPE_OFFER = "offer"
        const val TYPE_ANSWER = "answer"
        const val TYPE_CANDIDATE = "candidate"
        const val TYPE_USERS = "users"
        const val TYPE_INVITATION="invitation"
        const val TYPE_NEWUSER="newuser"
        const val TYPE_UPDATEUSER="updateuser"
    }

}