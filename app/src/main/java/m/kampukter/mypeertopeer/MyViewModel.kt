package m.kampukter.mypeertopeer

import androidx.lifecycle.ViewModel
import m.kampukter.mypeertopeer.data.RTCRepository
import org.webrtc.SurfaceViewRenderer

class MyViewModel(private val repository: RTCRepository) : ViewModel() {
    val userIdsLiveData = repository.userIdsLiveData
    val negotiationEvent = repository.negotiationEvent
    fun connect() {
        repository.connect()
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun startCall(userId: String, localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        repository.startCall(userId, localView, remoteView)
    }

    fun answerCall(localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        repository.answerCall(localView, remoteView)
    }

    fun dispose() {
        repository.disposeRTC()
    }
}