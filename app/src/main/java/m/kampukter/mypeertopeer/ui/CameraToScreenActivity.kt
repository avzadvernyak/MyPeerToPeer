package m.kampukter.mypeertopeer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.camera_activity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.data.SimpleRTCEventHandler
import m.kampukter.mypeertopeer.data.VideoRenderers
import m.kampukter.mypeertopeer.data.dto.SignalingWebSocketAPI
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.webrtc.*
import java.net.URL


class CameraToScreenActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    private var videoCapturerAndroid: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var rootEglBase: EglBase? = null


    private var localPeer: PeerConnection? = null
    private var remotePeer: PeerConnection? = null

    private var remoteVideoView: SurfaceViewRenderer? = null

    private var videoRenderers: VideoRenderers? = null

    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.camera_activity)
        videoRenderers = VideoRenderers(
            surfaceViewRenderer,
            remoteVideoView
        )
        handlePermissions()


        viewModel.connectToWS()
        viewModel.connectionStatusLiveData.observe(this, Observer {
            when (it) {
                is SignalingWebSocketAPI.ConnectionStatus.Connected -> {
                    statusWSTextView.text = getString(R.string.status_connected)
                }
                is SignalingWebSocketAPI.ConnectionStatus.Failed -> {
                    statusWSTextView.text = getString(R.string.status_failed)
                }
                is SignalingWebSocketAPI.ConnectionStatus.Disconnected -> {
                    statusWSTextView.text = getString(R.string.status_disconnected)
                }
                is SignalingWebSocketAPI.ConnectionStatus.Connecting -> {
                    statusWSTextView.text = getString(R.string.status_connecting)
                }
                is SignalingWebSocketAPI.ConnectionStatus.Closing -> {
                    statusWSTextView.text = getString(R.string.status_closing)
                }

            }
        })
        viewModel.webSocketMessageLiveData.observe(this, Observer { message ->
            when (message) {
                is SignalingWebSocketAPI.Message.AnswerReceived -> {
                    messageTextView.text = message.sessionDescription.type.toString()
                }
            }
        })
        viewModel.setWsServerURL(URL("http://192.168.0.69:8080/"))
    }

    override fun onDestroy() {
        super.onDestroy()

        videoCapturerAndroid?.stopCapture()
        videoCapturerAndroid?.dispose()
        videoSource?.dispose()
        audioSource?.dispose()
        localPeer?.dispose()
        remotePeer?.dispose()
        peerConnectionFactory?.dispose()
        rootEglBase?.release()
        surfaceViewRenderer.release()

    }

    private fun handlePermissions() {
        val canAccessCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val canRecordAudio = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!canAccessCamera || !canRecordAudio) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                CAMERA_AUDIO_PERMISSION_REQUEST
            )
        } else {
            start()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("blablabla", "onRequestPermissionsResult: $requestCode $permissions $grantResults")
        when (requestCode) {
            CAMERA_AUDIO_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    //startVideoSession()
                    start()
                } else {
                    finish()
                }
                return
            }
        }
    }


    private fun start() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true)

        //Create a new PeerConnectionFactory instance.
        val optionPCF = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory(optionPCF)
        val iceServers = arrayListOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"))
        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"))
        val rtcCfg = PeerConnection.RTCConfiguration(iceServers)
        rtcCfg.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        val rtcEvents = SimpleRTCEventHandler(
            this::handleLocalIceCandidate,
            this::addRemoteStream,
            this::removeRemoteStream
        )
        peerConnectionFactory?.let { factory ->
            localPeer = factory.createPeerConnection(iceServers, sdpConstraints, rtcEvents)
            // или
            // localPeer = factory.createPeerConnection(rtcCfg, sdpConstraints, rtcEvents)
        }
        // Инициализируем камеру
        val camera1 = Camera1Enumerator(false)
        val selectedDevice =
            camera1.deviceNames.firstOrNull(camera1::isFrontFacing)
                ?: camera1.deviceNames.first()

        videoCapturerAndroid = camera1.createCapturer(selectedDevice, null)
        //Create a VideoSource instance
        videoSource = peerConnectionFactory?.createVideoSource(videoCapturerAndroid)
        localVideoTrack = peerConnectionFactory?.createVideoTrack("100", videoSource)

        val mediaConstraints = MediaConstraints()

        //create an AudioSource instance
        audioSource = peerConnectionFactory?.createAudioSource(mediaConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("101", audioSource)

        //we will start capturing the video from the camera
        //params are width,height and fps
        videoCapturerAndroid?.startCapture(1000, 1000, 30)

        //create surface renderer, init it and add the renderer to the track
        surfaceViewRenderer.setMirror(true)

        rootEglBase = EglBase.create()
        surfaceViewRenderer.init(rootEglBase?.eglBaseContext, null)
        val videoRenderer = VideoRenderer(surfaceViewRenderer)
        localVideoTrack?.addRenderer(videoRenderer)

        val localStream = peerConnectionFactory?.createLocalMediaStream("102")
        localStream?.addTrack(localAudioTrack)
        localStream?.addTrack(localVideoTrack)
        localPeer?.addStream(localStream)

        val sdpObserver = object : AppSdpObserver() {
            override fun onCreateSuccess(p0: SessionDescription?) {
                super.onCreateSuccess(p0)
                //viewModel.send(p0)
            }
        }
        localPeer?.createOffer(sdpObserver,sdpConstraints)

    }

    private fun removeRemoteStream(stream: MediaStream) {
        Log.d("blablabla", "removeRemoteStream: $stream")
    }

    private fun addRemoteStream(stream: MediaStream) {
        Log.d("blablabla", "addRemoteStream: $stream")
    }

    private fun handleLocalIceCandidate(candidate: IceCandidate) {
        Log.d("blablabla", "handleLocalIceCandidate: $candidate")
    }

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST = 1
    }

}