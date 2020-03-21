package m.kampukter.mypeertopeer

import android.app.Application
import android.content.Context
import org.webrtc.*

class RTCClient(
    context: Application,
    observer: PeerConnection.Observer
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    private var peerConnectionFactory : PeerConnectionFactory
    private var videoCapturer: VideoCapturer
    private var localVideoSource: VideoSource
    private var peerConnection : PeerConnection

    private val rootEglBase: EglBase = EglBase.create()
    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    init {
        initPeerConnectionFactory(context)
        peerConnectionFactory = buildPeerConnectionFactory()
        videoCapturer = getVideoCapturer(context)
        localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer)
        peerConnection = buildPeerConnection(observer)
    }

    private fun initPeerConnectionFactory(context: Application) {

        PeerConnectionFactory.initializeAndroidGlobals(context, true)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory =
        PeerConnectionFactory(PeerConnectionFactory.Options())

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(iceServer, constraints, observer)

    private fun getVideoCapturer(context: Context) : VideoCapturer {
        val camera1 = Camera1Enumerator(false)
        val selectedDevice =
            camera1.deviceNames.firstOrNull(camera1::isFrontFacing)
                ?: camera1.deviceNames.first()

        return camera1.createCapturer(selectedDevice, null)
    }

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        videoCapturer.startCapture(320, 240, 60)

        val localVideoTrack =
            peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack.addRenderer(VideoRenderer(localVideoOutput))
        localVideoTrack.addSink(localVideoOutput)

        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        peerConnection.addStream(localStream)
    }

    private fun PeerConnection.call(sdpObserver: SdpObserver) {
        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {

                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }

                    override fun onCreateFailure(p0: String?) {
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver) {


        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }

                    override fun onCreateFailure(p0: String?) {
                    }
                }, p0)
                sdpObserver.onCreateSuccess(p0)
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver) = peerConnection.call(sdpObserver)

    fun answer(sdpObserver: SdpObserver) = peerConnection.answer(sdpObserver)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onCreateFailure(p0: String?) {
            }
        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection.addIceCandidate(iceCandidate)
    }
    fun disposeAll(){
        videoCapturer.stopCapture()
        videoCapturer.dispose()
        localVideoSource.dispose()
        peerConnection.dispose()
        peerConnectionFactory.dispose()
        rootEglBase.release()
    }
}