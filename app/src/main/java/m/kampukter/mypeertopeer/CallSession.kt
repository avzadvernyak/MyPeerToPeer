package m.kampukter.mypeertopeer

import android.content.Context
import android.media.AudioManager
import m.kampukter.mypeertopeer.data.NegotiationEvent
import org.webrtc.*
import java.util.*

class CallSession(
    private val context: Context,
    private val userId: String,
    private val onOutgoingNegotiationEvent: ((NegotiationEvent) -> Unit)
) {

   /* private val onOutgoingNegotiationEvent1: (NegotiationEvent) -> Unit = { event ->
        when (event) {
            is NegotiationEvent.Offer -> negotiationDao.sendOffer(event.to, event.sdp)
            is NegotiationEvent.Answer -> negotiationDao.sendAnswer(event.to, event.sdp)
            is NegotiationEvent.IceCandidate -> negotiationDao.sendCandidate(event.to, event.sdp, event.sdpMid, event.sdpMLineIndex)
        }
    }*/
    fun start() {
        createPeerConnection()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onSetFailure(p0: String?) {}

            override fun onSetSuccess() {
                peerConnection?.localDescription?.description?.let {
                    onOutgoingNegotiationEvent.invoke(
                        NegotiationEvent.Offer("", userId, it)
                    )
                }
            }

            override fun onCreateSuccess(sdp: SessionDescription?) {
                peerConnection?.setLocalDescription(this, sdp)
            }

            override fun onCreateFailure(p0: String?) {}
        }, mediaConstraints)
    }

    fun handleOffer(sdp: String) {
        createPeerConnection()
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(reason: String?) {
            }

            override fun onSetSuccess() {
                peerConnection?.createAnswer(object : SdpObserver {
                    override fun onSetFailure(reason: String?) {}

                    override fun onSetSuccess() {
                        peerConnection?.localDescription?.description?.let { sdp ->
                            onOutgoingNegotiationEvent.invoke(
                                NegotiationEvent.Answer(
                                    "",
                                    userId,
                                    sdp
                                )
                            )
                        }
                    }

                    override fun onCreateSuccess(sdp: SessionDescription?) {
                        peerConnection?.setLocalDescription(this, sdp)
                    }

                    override fun onCreateFailure(reason: String?) {}
                }, mediaConstraints)
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}

            override fun onCreateFailure(p0: String?) {}
        }, SessionDescription(SessionDescription.Type.OFFER, sdp))
    }

    fun handleAnswer(sdp: String) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {}

            override fun onSetSuccess() {}

            override fun onCreateSuccess(p0: SessionDescription?) {}

            override fun onCreateFailure(p0: String?) {}
        }, SessionDescription(SessionDescription.Type.ANSWER, sdp))
    }

    fun handleIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {
        peerConnection?.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, sdp))
    }

    private var peerConnection: PeerConnection? = null
    private val mediaConstraints = MediaConstraints()

    private fun createPeerConnection() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        val initializeOptions = PeerConnectionFactory.InitializationOptions
            .builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializeOptions)
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE)

        val peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        val audioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
        val localAudioTrack =
            peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), audioSource)

        val localStream = peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString())
        localStream.addTrack(localAudioTrack)

        val iceServers = listOf(
            PeerConnection.IceServer("stun:stun.l.google.com:19302"),
            PeerConnection.IceServer("turn:numb.viagenie.ca", "muazkh", "webrtc@live.com")
        )
        peerConnection =
            peerConnectionFactory.createPeerConnection(iceServers, peerConnectionObserver)
        peerConnection?.addStream(localStream)
    }

    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate?) {
            if (candidate == null) return
            onOutgoingNegotiationEvent.invoke(
                NegotiationEvent.IceCandidate(
                    "",
                    userId,
                    candidate.sdp,
                    candidate.sdpMid,
                    candidate.sdpMLineIndex
                )
            )
        }

        override fun onDataChannel(p0: DataChannel?) {}

        override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {}

        override fun onIceConnectionReceivingChange(p0: Boolean) {}

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}

        override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}

        override fun onTrack(transceiver: RtpTransceiver?) {}

        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

        override fun onAddStream(stream: MediaStream?) {}

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

        override fun onRemoveStream(p0: MediaStream?) {}

        override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {}

        override fun onRenegotiationNeeded() {}

        override fun onAddTrack(p0: RtpReceiver?, remoteStreams: Array<out MediaStream>?) {}
    }


}