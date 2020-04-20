package m.kampukter.mypeertopeer.data

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.mypeertopeer.data.dto.FCMRestAPI
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import m.kampukter.mypeertopeer.myName
import m.kampukter.mypeertopeer.rtc.CallSession
import m.kampukter.mypeertopeer.ui.MainActivity

class RTCRepository(
    private val context: Application,
    private val negotiationAPI: NegotiationAPI,
    private val myFCMRestAPI: FCMRestAPI
) {

    data class IceCandidateInfo(val sdp: String, val sdpMid: String, val sdpMLineIndex: Int)

    private val listIceCandidateInfo = mutableListOf<IceCandidateInfo>()
    private var receivedOffer: String? = null
    private var lastFrom: String? = null

    private val _userIdsLiveData = MutableLiveData<List<String>>()
    val userIdsLiveData: LiveData<List<String>>
        get() = _userIdsLiveData

    private val _negotiationEvent = MutableLiveData<NegotiationEvent>()
    val negotiationEvent: LiveData<NegotiationEvent>
        get() = _negotiationEvent

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
            NegotiationEvent.HangUp -> _negotiationEvent.postValue(NegotiationEvent.HangUp)
            NegotiationEvent.Connected -> _negotiationEvent.postValue(NegotiationEvent.Connected)
        }
    }

    init {
        negotiationAPI.onNegotiationEvent = { event ->
            when (event) {
                is NegotiationEvent.Discovery -> _userIdsLiveData.postValue(event.userIds)
                is NegotiationEvent.Offer -> {
                    receivedOffer = event.sdp
                    lastFrom = event.from
                    _negotiationEvent.postValue(NegotiationEvent.IncomingCall(event.from))
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                is NegotiationEvent.Answer -> {
                    callSession?.handleAnswer(event.sdp)
                }
                is NegotiationEvent.IceCandidate -> {
                    if (callSession != null) callSession?.handleIceCandidate(
                        event.sdp,
                        event.sdpMid,
                        event.sdpMLineIndex
                    )
                    else listIceCandidateInfo.add(
                        IceCandidateInfo(
                            event.sdp,
                            event.sdpMid,
                            event.sdpMLineIndex
                        )
                    )
                }
            }
        }
    }

    fun connect() {
        negotiationAPI.connect()
    }

    fun disconnect() {
        negotiationAPI.disconnect()
    }

    fun startCall(infoLocalVideoCapture: InfoLocalVideoCapture) {
        if (callSession == null) callSession =
            CallSession(
                context,
                infoLocalVideoCapture.userId,
                onOutgoingNegotiationEvent
            ).apply {
                initSurfaceView(infoLocalVideoCapture.localView, infoLocalVideoCapture.remoteView)
                startNewSession(infoLocalVideoCapture.localView)
            }
    }

    fun answerCall(infoLocalVideoCapture: InfoLocalVideoCapture) {
        lastFrom?.let { from ->
            if (callSession == null) callSession =
                CallSession(
                    context,
                    from,
                    onOutgoingNegotiationEvent
                ).apply {
                    initSurfaceView(
                        infoLocalVideoCapture.localView,
                        infoLocalVideoCapture.remoteView
                    )
                    startLocalVideoCapture(infoLocalVideoCapture.localView)
                    receivedOffer?.let { sdp -> handleOffer(sdp) }
                    listIceCandidateInfo.forEach { ice ->
                        handleIceCandidate(
                            ice.sdp,
                            ice.sdpMid,
                            ice.sdpMLineIndex
                        )
                    }
                }
        }
    }

    fun disposeRTC() {
        _negotiationEvent.postValue(NegotiationEvent.Waiting)
        callSession?.disposeAll()
        callSession = null
        receivedOffer = null
        lastFrom = null
        listIceCandidateInfo.clear()
    }

    fun sendFCMMessage() {
        //val currentToken = "e3lhY0r1RkOhPI1swPMdvX:APA91bHWVxklBRJhNpdPN-UxmVqMWDQ-BFC6oFnGElqVYXvt8hhRd7mI33PU98ao6G2kLA0QwAtxEs1eVNNu7ocyVgxusSPbGgn4T2Jl6nnQliNj9Ad5xAsMDFlZDDZH5q_EdQsQKVGH"
        val currentToken =
            "d51kzI0FSjybtDI7KkLDJd:APA91bFfFo_FzfkiD3MkR9Kv1Y_3Go414nWslRutcDjmOjdgCCIMI4731PskymKtdQLrWGKX_atyjj3vdl_mzd8EdRlHiwf711wJD_liXIp2i8AQzsSrwXoQocu2HZSrn8xvUz3PeORc"
        myFCMRestAPI.send(currentToken, myName)
    }
}

