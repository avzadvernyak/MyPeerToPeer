package m.kampukter.mypeertopeer.rtc

import android.app.Application
import android.content.Context
import android.media.AudioManager
import m.kampukter.mypeertopeer.data.NegotiationEvent
import org.webrtc.*
import java.util.*

class CallSession(
    private val context: Application,
    private val userId: String,
    private val onOutgoingNegotiationEvent: ((NegotiationEvent) -> Unit)
) {

    private var peerConnectionFactory: PeerConnectionFactory
    private val rootEglBase: EglBase = EglBase.create()
    private var videoCapturer: VideoCapturer
    private var localVideoSource: VideoSource
    private var peerConnection: PeerConnection?
    private var audioSource: AudioSource? = null

    private var remoteVideoView: SurfaceViewRenderer? = null

    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer(),
        PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
            .setUsername("muazkh")
            .setPassword("webrtc@live.com")
            .createIceServer()
    )
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

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            when (p0) {
                PeerConnection.IceConnectionState.DISCONNECTED,
                PeerConnection.IceConnectionState.CLOSED,
                PeerConnection.IceConnectionState.FAILED -> {
                    onOutgoingNegotiationEvent.invoke(NegotiationEvent.HangUp)
                }
                PeerConnection.IceConnectionState.CONNECTED -> {
                    onOutgoingNegotiationEvent.invoke(NegotiationEvent.Connected)
                }
                else -> {
                }
            }

        }

        override fun onAddStream(stream: MediaStream?) {
            remoteVideoView?.let { stream?.videoTracks?.get(0)?.addSink(it) }
        }

        override fun onDataChannel(p0: DataChannel?) {}

        override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {}

        override fun onIceConnectionReceivingChange(p0: Boolean) {}

        override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}

        override fun onTrack(transceiver: RtpTransceiver?) {}

        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

        override fun onRemoveStream(p0: MediaStream?) {}

        override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {}

        override fun onRenegotiationNeeded() {}

        override fun onAddTrack(p0: RtpReceiver?, remoteStreams: Array<out MediaStream>?) {}
    }

    init {
        initPeerConnectionFactory(context)
        peerConnectionFactory = buildPeerConnectionFactory()
        videoCapturer = getVideoCapturer()
        localVideoSource = peerConnectionFactory.createVideoSource(false)
        peerConnection = buildPeerConnection(peerConnectionObserver)
    }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory =
        PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )

    fun startNewSession(localView: SurfaceViewRenderer) {
        startLocalVideoCapture(localView)
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

    fun initSurfaceView(localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        remoteVideoView = remoteView
        localView.run {
            setMirror(true)
            setEnableHardwareScaler(true)
            init(rootEglBase.eglBaseContext, null)
        }
        remoteView.run {
            //setMirror(true)
            setEnableHardwareScaler(true)
            init(rootEglBase.eglBaseContext, null)
        }
    }

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        audioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
        val localAudioTrack =
            peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), audioSource)

        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        videoCapturer.initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )

        videoCapturer.startCapture(320, 240, 30)

        val localVideoTrack =
            peerConnectionFactory.createVideoTrack(UUID.randomUUID().toString(), localVideoSource)
        localVideoTrack.addSink(localVideoOutput)

        val localStream = peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString())

        localStream.addTrack(localAudioTrack)
        localStream.addTrack(localVideoTrack)

        peerConnection?.addStream(localStream)
    }

    private fun getVideoCapturer(): VideoCapturer {
        val camera1 = Camera1Enumerator(false)
        val selectedDevice =
            camera1.deviceNames.firstOrNull(camera1::isFrontFacing)
                ?: camera1.deviceNames.first()

        return camera1.createCapturer(selectedDevice, null)
    }

    fun disposeAll() {
        videoCapturer.stopCapture()
        videoCapturer.dispose()
        localVideoSource.dispose()
        audioSource?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory.dispose()
        rootEglBase.release()
    }
}