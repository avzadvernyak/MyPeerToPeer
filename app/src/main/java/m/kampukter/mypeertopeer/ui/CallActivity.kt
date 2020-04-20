package m.kampukter.mypeertopeer.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.second_version_activity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.data.InfoLocalVideoCapture
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.ui.MainActivity.Companion.EXTRA_MESSAGE_CANDIDATE
import org.koin.androidx.viewmodel.ext.android.viewModel

class CallActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var lastUser: String? = null

    private var audioManager: AudioManager? = null
    private var savedAudioMode: Int? = null
    private var savedMicrophoneState: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_version_activity)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lastUser = intent.getStringExtra(EXTRA_MESSAGE_CANDIDATE)
        connectTextView.text = getString(R.string.conversation_title, lastUser)
        remote_view.visibility = View.INVISIBLE

        checkCameraPermission()

        //
        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        savedAudioMode = audioManager?.mode
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        savedMicrophoneState = audioManager?.isMicrophoneMute
        audioManager?.isMicrophoneMute = false
        //


        hangUpFAB.setOnClickListener {
            viewModel.dispose()
            finish()
        }
        viewModel.negotiationEvent.observe(this, Observer {
            when (it) {
                is NegotiationEvent.HangUp -> {
                    viewModel.dispose()
                    finish()
                }
                is NegotiationEvent.Connected -> {
                    remote_view_loading.visibility = View.GONE
                    remote_view.visibility = View.VISIBLE
                }
            }
        })

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
        lastUser?.let { viewModel.startCall(InfoLocalVideoCapture(it, local_view, remote_view)) }

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


    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}