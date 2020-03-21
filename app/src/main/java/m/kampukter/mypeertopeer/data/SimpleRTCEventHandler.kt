package m.kampukter.mypeertopeer.data

import android.util.Log
import org.webrtc.*

class SimpleRTCEventHandler (

    private val onIceCandidateCb: (IceCandidate) -> Unit,
    private val onAddStreamCb: (MediaStream) -> Unit,
    private val onRemoveStreamCb: (MediaStream) -> Unit) : PeerConnection.Observer {

    override fun onIceCandidate(candidate: IceCandidate?) {
        if(candidate != null) onIceCandidateCb(candidate)
    }

    override fun onAddStream(stream: MediaStream?) {
        if (stream != null) onAddStreamCb(stream)
    }

    override fun onRemoveStream(stream: MediaStream?) {
        if(stream != null) onRemoveStreamCb(stream)
    }

    override fun onDataChannel(chan: DataChannel?) { Log.w(TAG, "onDataChannel: $chan") }

    override fun onIceConnectionReceivingChange(p0: Boolean) { Log.w(TAG, "onIceConnectionReceivingChange: $p0") }

    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) { Log.w(
        TAG, "onIceConnectionChange: $newState") }

    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) { Log.w(
        TAG, "onIceGatheringChange: $newState") }

    override fun onSignalingChange(newState: PeerConnection.SignalingState?) { Log.w(
        TAG, "onSignalingChange: $newState") }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) { Log.w(
        TAG, "onIceCandidatesRemoved: $candidates") }

    override fun onRenegotiationNeeded() { Log.w(TAG, "onRenegotiationNeeded") }

    override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) { }
    companion object {
        //const val TAG = "SimpleRTCEventHandler"
        const val TAG = "blablabla"
    }
}