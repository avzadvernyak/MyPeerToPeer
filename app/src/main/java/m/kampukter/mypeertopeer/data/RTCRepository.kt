package m.kampukter.mypeertopeer.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.mypeertopeer.data.dao.UsersInfoDao
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import m.kampukter.mypeertopeer.rtc.CallSession

class RTCRepository(
    private val context: Application,
    private val negotiationAPI: NegotiationAPI,
    private val myUsersDao: UsersInfoDao
) {

    private val _listCalledUserLiveData = MutableLiveData<List<UserData>>()
    val listCalledUserLiveData: LiveData<List<UserData>>
        get() = _listCalledUserLiveData

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
                is NegotiationEvent.Users -> _listCalledUserLiveData.postValue(event.users)
                is NegotiationEvent.Discovery -> _userIdsLiveData.postValue(event.userIds)
                is NegotiationEvent.Offer -> {
                    receivedOffer = event.sdp
                    lastFrom = event.from
                    _negotiationEvent.postValue(NegotiationEvent.IncomingCall(event.from))
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

    fun sendFCMMessage(id: String) {
        negotiationAPI.sendInvitation(id)
    }

    fun saveUsersData(userData: UserData) {
        negotiationAPI.sendUpdateUser(userData)
    }
    fun addUsersData(userData: UserData) {
        negotiationAPI.sendNewUser(userData)
    }
}

