package m.kampukter.mypeertopeer.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.mypeertopeer.R

/*
1 важное - наследник PeerConnection.Observer
2 важное - инициализация, тут она в init
3 важное - конфигурация стримов, тут она в setupMediaDevices
и последнее важное - диспозить это все ибо оно нативное и коллектор его не выгребет
 */

class MainActivity : AppCompatActivity() {

   /* private var videoSession: VideoCallSession? = null
    private var localVideoView: SurfaceViewRenderer? = null
    private var remoteVideoView: SurfaceViewRenderer? = null

    private var audioManager: AudioManager? = null
    private var savedAudioMode: Int? = null
    private var savedMicrophoneState: Boolean? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        /*audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        savedAudioMode = audioManager?.mode
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

        savedMicrophoneState = audioManager?.isMicrophoneMute
        audioManager?.isMicrophoneMute = false

        handlePermissions()*/
        viewCamera1Button.setOnClickListener {
            startActivity(Intent(this, CameraToScreenActivity::class.java))
        }
        viewCamera2Button.setOnClickListener {
            startActivity(Intent(this, SecondVerActivity::class.java))
        }
    }
/*
    private fun onStatusChanged(newStatus: VideoCallStatus) {
        Log.d(TAG, "New call status: $newStatus")
        runOnUiThread {
            when (newStatus) {
                VideoCallStatus.FINISHED -> finish()
                else -> {
                    statusTextView?.text = resources.getString(newStatus.label)
                    statusTextView?.setTextColor(ContextCompat.getColor(this, newStatus.color))
                }
            }
        }
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

    private fun startVideoSession() {

        videoSession = VideoCallSession.connect(
            this,
            BACKEND_URL,
            VideoRenderers(localVideoView, remoteVideoView),
            this::onStatusChanged
        )

        localVideoView?.init(videoSession?.renderContext, null)
        localVideoView?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        localVideoView?.setZOrderMediaOverlay(true)
        localVideoView?.setEnableHardwareScaler(true)

        remoteVideoView?.init(videoSession?.renderContext, null)
        remoteVideoView?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        remoteVideoView?.setEnableHardwareScaler(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.w(TAG, "onRequestPermissionsResult: $requestCode $permissions $grantResults")
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

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST = 1
        private const val TAG = "MyPeerToPeer"
        private const val BACKEND_URL = "ws://192.168.0.83:81/"
    }*/
}