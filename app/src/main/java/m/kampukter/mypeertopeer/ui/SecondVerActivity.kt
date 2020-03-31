package m.kampukter.mypeertopeer.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.second_version_activity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.ui.MainActivity.Companion.EXTRA_MESSAGE_CANDIDATE
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecondVerActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var lastUser: String? = null

    private var audioManager: AudioManager? = null
    private var savedAudioMode: Int? = null
    private var savedMicrophoneState: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_version_activity)

        lastUser = intent.getStringExtra(EXTRA_MESSAGE_CANDIDATE)

        checkCameraPermission()

        //service.onNegotiationEvent = this::negotiationMessageListener

        //
        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        savedAudioMode = audioManager?.mode
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        savedMicrophoneState = audioManager?.isMicrophoneMute
        audioManager?.isMicrophoneMute = false
        //

        hangUpFAB.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        local_view.release()
        remote_view.release()
        viewModel.dispose()
        savedAudioMode?.let { audioManager?.mode = it }
        savedMicrophoneState?.let { audioManager?.isMicrophoneMute = it }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
            onCameraPermissionGranted()
        }
    }

    private fun onCameraPermissionGranted() {
        lastUser?.let { viewModel.startCall(it, local_view, remote_view) }
        /*rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    lastUser?.let { user ->
                        p0?.let {
                            service.send(
                                NegotiationMessage(
                                    type = "serverUrl",
                                    to = user,
                                    from = myName,
                                    sdp = it.sdp,
                                    sdpMid = it.sdpMid,
                                    sdpMLineIndex = it.sdpMLineIndex
                                )
                            )
                        }
                    }
                    rtcClient.addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.d("blablabla", "onAddStream -> ${p0?.videoTracks?.get(0)}")

                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }

                override fun onRemoveStream(p0: MediaStream?) {
                    super.onRemoveStream(p0)
                    Log.d("blablabla", "onRemoveStream")

                }

                override fun onDataChannel(p0: DataChannel?) {
                    Log.d("blablabla", "onDataChannel: $p0")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {
                    Log.d("blablabla", "onIceConnectionReceivingChange: $p0")
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    when (p0) {
                        PeerConnection.IceConnectionState.DISCONNECTED,
                        PeerConnection.IceConnectionState.CLOSED,
                        PeerConnection.IceConnectionState.FAILED -> {
                            finish()
                        }
                        PeerConnection.IceConnectionState.CONNECTED -> {
                            runOnUiThread {
                                remote_view_loading.visibility = View.GONE
                            }
                        }
                        else -> {
                        }
                    }
                }

                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                    Log.d("blablabla", "onIceGatheringChange: $p0")
                }

                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                    Log.d("blablabla", "onSignalingChange: $p0")
                }

                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                    Log.d("blablabla", "onIceCandidatesRemoved: $p0")
                }

                override fun onRenegotiationNeeded() {
                    Log.d("blablabla", "onRenegotiationNeeded")
                }

                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}

            }
        )

        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)
        if (lastUser != "") {
            rtcClient.call(sdpObserver)
        }

         */
    }

    private fun requestCameraPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                CAMERA_PERMISSION
            ) && !dialogShown
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA_PERMISSION),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("This app need the camera to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Log.d("blablabla", "Camera Permission Denied")
    }


    /*private fun negotiationMessageListener(message: NegotiationEvent) {
        runOnUiThread {
            when (message) {
                is NegotiationEvent.Answer -> {
                    Log.d("blablabla", "AnswerReceived in UI")
                    rtcClient.onRemoteSessionReceived(
                        SessionDescription(
                            SessionDescription.Type.ANSWER,
                            message.sdp
                        )
                    )
                    remote_view_loading.visibility = View.GONE
                }
                is NegotiationEvent.Offer -> {
                    Log.d("blablabla", "OfferReceived in UI")
                    lastUser = message.from
                    rtcClient.onRemoteSessionReceived(
                        SessionDescription(
                            SessionDescription.Type.OFFER,
                            message.sdp
                        )
                    )
                    rtcClient.answer(sdpObserver)
                    remote_view_loading.visibility = View.GONE
                }
                is NegotiationEvent.IceCandidate -> {
                    rtcClient.addIceCandidate(
                        IceCandidate(
                            message.sdpMid,
                            message.sdpMLineIndex,
                            message.sdp
                        )
                    )
                }
                is NegotiationEvent.Discovery -> {
                    //usersAdapter?.setList(message.userIds)
                }
            }
        }
    }*/

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}