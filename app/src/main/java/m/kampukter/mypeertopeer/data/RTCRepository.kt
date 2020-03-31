package m.kampukter.mypeertopeer.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import m.kampukter.mypeertopeer.CallSession
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import org.webrtc.SurfaceViewRenderer

class RTCRepository(
    private val context: Application,
    private val negotiationAPI: NegotiationAPI
) {

    private val _userIdsLiveData = MutableLiveData<List<String>>()

    val userIdsLiveData: LiveData<List<String>>
        get() = _userIdsLiveData

    private val _isOffer = MutableLiveData<Boolean>()

    val isOffer: LiveData<Boolean>
        get() = _isOffer

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
                    ).apply {
                        _isOffer.postValue(true)
                        handleOffer(event.sdp)
                    }
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

    fun connect() {
        negotiationAPI.connect()
    }

    fun disconnect() {
        negotiationAPI.disconnect()
    }

    fun startCall(userId: String, localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        if (callSession == null) callSession =
            CallSession(context, userId, onOutgoingNegotiationEvent).apply {
                initSurfaceView(localView)
                initSurfaceView(remoteView)
                start(localView)
            }
    }

    data class MyViewRenderer(
        val localSVR: SurfaceViewRenderer,
        val remoteSVR: SurfaceViewRenderer
    )

    private var myViewRenderer: MyViewRenderer? = null
        set(value) {
            Log.d("blablabla", "set view ")
            if (callSession != null) {
                value?.let {
                    callSession?.initSurfaceView(it.localSVR)
                    callSession?.initSurfaceView(it.remoteSVR)
                    callSession?.startLocalVideoCapture(it.localSVR)

                }
            }
            field = value
        }

    fun setSurfaceView(localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        myViewRenderer = MyViewRenderer(localView, remoteView)
    }

    fun disposeRTC() {
        callSession?.disposeAll()
        callSession = null
    }
}

