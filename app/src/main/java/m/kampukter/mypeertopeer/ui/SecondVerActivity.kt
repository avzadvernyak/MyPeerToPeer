package m.kampukter.mypeertopeer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.second_version_activity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.RTCClient
import m.kampukter.mypeertopeer.data.NegotiationMessage
import m.kampukter.mypeertopeer.data.dto.SignalingWebSocketAPI
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import java.net.URL

//= "user_${Build.MODEL}"

val myName: String
    get() = "user_${Build.BOOTLOADER}"

class SecondVerActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    private lateinit var rtcClient: RTCClient

    private var usersAdapter: UsersAdapter? = null
    private var lastUser: String? = null



    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            //Log.d("blablabla", "Send offer $p0 + ${p0?.type.toString()} + user-$lastUser")
            lastUser?.let { user ->

                p0?.let {
                    viewModel.send(
                        NegotiationMessage(
                            to = user,
                            from = myName,
                            type = it.type.canonicalForm(),
                            sdp = it.description
                        )
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_version_activity)

        userTextView.text = myName
        lastUser = "user_and_2"
        checkCameraPermission()

        usersAdapter = UsersAdapter { item ->
            //Log.d("blablabla", "Выбран  -> $item")
            callFAB.isExpanded = false
            lastUser = item
            rtcClient.call(sdpObserver)
        }
        with(usersRecyclerView) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = usersAdapter
        }

        viewModel.setWsServerURL(URL("http://176.37.84.130:8080/$myName"))
        viewModel.connectToWS()
        viewModel.connectionStatusLiveData.observe(this, Observer {
            when (it) {
                is SignalingWebSocketAPI.ConnectionStatus.Connected -> {
                    statusWSTextView.text = getString(R.string.status_connected)
                    callFAB.visibility = View.VISIBLE
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
            //Log.d("blablabla", "Что то in UI $message")
            Log.d("blablabla", "Что то in UI $message ${viewModel.webSocketMessageLiveData}")
            when (message) {
                is SignalingWebSocketAPI.Message.AnswerReceived -> {
                    //Log.d("blablabla", "AnswerReceived in UI")
                    rtcClient.onRemoteSessionReceived(message.sessionDescription)
                    remote_view_loading.visibility = View.GONE
                }
                is SignalingWebSocketAPI.Message.OfferReceived -> {
                    Log.d("blablabla", "OfferReceived in UI")
                    rtcClient.onRemoteSessionReceived(message.sessionDescription)
                    rtcClient.answer(sdpObserver)
                    remote_view_loading.visibility = View.GONE
                }
                is SignalingWebSocketAPI.Message.IceCandidateReceived -> {
                    Log.d("blablabla", "IceCandidateReceived in UI")
                    rtcClient.addIceCandidate(message.iceCandidate)
                }
                is SignalingWebSocketAPI.Message.Text -> {
                    Log.d("blablabla", "Text in UI -> ${message.content}")
                }
                is SignalingWebSocketAPI.Message.DiscoveryReceived -> {
                    Log.d("blablabla", "Discovery in UI -> ${message.content}")
                    usersAdapter?.setList(message.content)
                }
                else -> {Log.d("blablabla", "Фигня in UI ")}
            }
        })


        sheet.setOnClickListener {
            callFAB.isExpanded = false
        }
        callFAB.setOnClickListener {
            callFAB.isExpanded = true
        }
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
        rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)

                    rtcClient.addIceCandidate(p0)
                    //Log.d("blablabla", "Send onIceCandidate $p0 to $lastUser")
                    lastUser?.let { user ->
                        p0?.let {
                            viewModel.send(
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


                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }
            }
        )

        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)
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

    override fun onDestroy() {
        super.onDestroy()
        local_view.release()
        remote_view.release()
        rtcClient.disposeAll()
        viewModel.disconnect()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}