package m.kampukter.mypeertopeer._pilot

import android.content.Context
import android.util.Log
import m.kampukter.mypeertopeer.data.VideoRenderers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.webrtc.*
import java.util.concurrent.Executors

class VideoCallSession(
    private val context: Context,
    private val onStatusChangedListener: (VideoCallStatus) -> Unit,
    private val signaler: SignalingWebSocket,
    private val videoRenderers: VideoRenderers
) {


    private val eglBase = EglBase.create()

    val renderContext: EglBase.Context
        get() = eglBase.eglBaseContext

    private var isOfferingPeer = false

    private var factory : PeerConnectionFactory? = null

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
    }

    init {
        signaler.messageHandler = this::onMessage
        this.onStatusChangedListener(VideoCallStatus.MATCHING)
        executor.execute(this::init)
    }
    private fun init() {
        PeerConnectionFactory.initializeAndroidGlobals(context, true)
        val options = PeerConnectionFactory.Options()
        options.networkIgnoreMask = 0

        factory = PeerConnectionFactory(options)
        factory?.setVideoHwAccelerationOptions(eglBase.eglBaseContext, eglBase.eglBaseContext)

        val iceServers = arrayListOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        /*val rtcEvents = SimpleRTCEventHandler(this::handleLocalIceCandidate, this::addRemoteStream, this::removeRemoteStream)
        peerConnection = factory?.createPeerConnection(rtcConfig, constraints, rtcEvents)
        setupMediaDevices()*/
    }
    private fun onMessage(message: ClientMessage) {
        when(message) {
            is MatchMessage -> {
                onStatusChangedListener(VideoCallStatus.CONNECTING)
                isOfferingPeer = message.offer
                start()
            }
            is SDPMessage -> {
                //handleRemoteDescriptor(message.sdp)
            }
            is ICEMessage -> {
                //handleRemoteCandidate(message.label, message.id, message.candidate)
            }
            is PeerLeft -> {
                onStatusChangedListener(VideoCallStatus.FINISHED)
            }
        }
    }
    private fun start() {
        //executor.execute(this::maybeCreateOffer)
    }
    companion object {

        fun connect(context: Context, url: String, videoRenderers: VideoRenderers, callback: (VideoCallStatus) -> Unit) : VideoCallSession {
            val websocketHandler =
                SignalingWebSocket()
            val session = VideoCallSession(
                context,
                callback,
                websocketHandler,
                videoRenderers
            )
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            Log.i(TAG, "Connecting to $url")
            client.newWebSocket(request, websocketHandler)
            client.dispatcher().executorService().shutdown()
            return session
        }

        private val STREAM_LABEL = "remoteStream"
        private val VIDEO_TRACK_LABEL = "remoteVideoTrack"
        private val AUDIO_TRACK_LABEL = "remoteAudioTrack"
        private val TAG = "VideoCallSession"
        private val executor = Executors.newSingleThreadExecutor()
    }
}