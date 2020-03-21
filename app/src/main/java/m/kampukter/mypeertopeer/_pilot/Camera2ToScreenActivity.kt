package m.kampukter.mypeertopeer._pilot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.camera_activity.*
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.data.VideoRenderers
import org.webrtc.*

class Camera2ToScreenActivity : AppCompatActivity() {

    private var remoteVideoView: SurfaceViewRenderer? = null

    private var videoRenderers: VideoRenderers? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.camera_activity)
        videoRenderers = VideoRenderers(
            surfaceViewRenderer,
            remoteVideoView
        )
        handlePermissions()
    }

    override fun onDestroy() {
        super.onDestroy()

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
            startVideoSession()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_AUDIO_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    startVideoSession()
                } else {
                    finish()
                }
                return
            }
        }
    }

    private fun startVideoSession() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true)

        //Create a new PeerConnectionFactory instance.
        val optionPCF = PeerConnectionFactory.Options()
        val peerConnectionFactory = PeerConnectionFactory(optionPCF)
        var videoCapturer: VideoCapturer? = null
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("blablabla", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        } else {
            Log.d("blablabla", "))))")
            val camera2 = Camera2Enumerator(this)
            if (camera2.deviceNames.isNotEmpty()) {
                val selectedDevice = camera2.deviceNames.firstOrNull(camera2::isFrontFacing)
                    ?: camera2.deviceNames.first()
                videoCapturer = camera2.createCapturer(selectedDevice, null)
            }

            //Create a VideoSource instance
            val videoSource = peerConnectionFactory.createVideoSource(videoCapturer)

            val localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)

            val mediaConstraints = MediaConstraints()

            //create an AudioSource instance
            val audioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
            val localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)

            //we will start capturing the video from the camera
            //params are width,height and fps
            videoCapturer?.startCapture(1000, 1000, 30)

            //create surface renderer, init it and add the renderer to the track
            //surfaceViewRenderer.setMirror(true)

            val rootEglBase = EglBase.create()
            surfaceViewRenderer.init(rootEglBase.eglBaseContext, null)
            val videoRenderer = VideoRenderer(videoRenderers?.localRenderer)
            localVideoTrack.addRenderer(videoRenderer)
        }
    }

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST = 1
    }

}