package m.kampukter.mypeertopeer

import androidx.lifecycle.ViewModel
import m.kampukter.mypeertopeer.data.RTCRepository
import org.webrtc.SurfaceViewRenderer

class MyViewModel(private val repository: RTCRepository) : ViewModel() {
    val userIdsLiveData = repository.userIdsLiveData
    val isOffer = repository.isOffer
    fun connect() {
        repository.connect()
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun startCall(userId: String, localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        repository.startCall(userId, localView, remoteView)
    }

    fun setSurfaceView(localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        repository.setSurfaceView(localView, remoteView)
    }

    fun dispose() {
        repository.disposeRTC()
    }
}