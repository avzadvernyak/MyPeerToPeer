package m.kampukter.mypeertopeer.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.mypeertopeer.CallSession
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI

class DefaultMainRepository(
    private val context: Context,
    private val negotiationAPI: NegotiationAPI
) : MainRepository {

    private val _userIdsLiveData = MutableLiveData<List<String>>()

    override val userIdsLiveData: LiveData<List<String>>
        get() = _userIdsLiveData

    private var callSession: CallSession? = null

    private val onOutgoingNegotiationEvent: (NegotiationEvent) -> Unit = { event ->
        when (event) {
            is NegotiationEvent.Offer -> negotiationAPI.sendOffer(event.to, event.sdp)
            is NegotiationEvent.Answer -> negotiationAPI.sendAnswer(event.to, event.sdp)
            is NegotiationEvent.IceCandidate -> negotiationAPI.sendCandidate(
                event.to,
                event.sdp,
                event.sdpMid,
                event.sdpMLineIndex
            )
        }
    }

    init {
        negotiationAPI.onNegotiationEvent = { event ->
            when (event) {
                is NegotiationEvent.Discovery -> _userIdsLiveData.postValue(event.userIds)
                is NegotiationEvent.Offer -> {
                    if (callSession == null) callSession = CallSession(
                        context,
                        event.from,
                        onOutgoingNegotiationEvent
                    ).apply { handleOffer(event.sdp) }
                }
                is NegotiationEvent.Answer -> {
                    callSession?.handleAnswer(event.sdp)
                }
                is NegotiationEvent.IceCandidate -> {
                    callSession?.handleIceCandidate(event.sdp, event.sdpMid, event.sdpMLineIndex)
                }
            }
        }
    }

    override fun connect() {
        negotiationAPI.connect()
    }

    override fun disconnect() {
        negotiationAPI.disconnect()
    }

    override fun startCall(userId: String) {
        if (callSession == null) callSession =
            CallSession(context, userId, onOutgoingNegotiationEvent).apply { start() }
    }
}

