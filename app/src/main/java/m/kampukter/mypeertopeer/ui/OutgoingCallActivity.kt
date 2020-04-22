package m.kampukter.mypeertopeer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.outgoing_call_aactivity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class OutgoingCallActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var calledUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calledUserId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_CANDIDATE)
        setContentView(R.layout.outgoing_call_aactivity)
        hangUpFAB.visibility = View.INVISIBLE

        viewModel.setUserDataId(calledUserId)
        viewModel.currentUserStatus.observe(this, Observer { userStatus ->
            when (userStatus) {
                is MyViewModel.UserStatusEvent.UserConnected -> {
                    Log.d("blablabla", "UserConnected ${userStatus.user.userName}")
                    startActivity(
                        Intent(this, CallActivity::class.java).putExtra(
                            MainActivity.EXTRA_MESSAGE_CANDIDATE,
                            userStatus.user.id
                        )
                    )
                }
                is MyViewModel.UserStatusEvent.UserDisconnected -> {
                    Log.d("blablabla", "UserDisconnected ${userStatus.user.userName}")
                    calledUserTextView.text = userStatus.user.userName
                    callFCMFAB.setOnClickListener {
                        viewModel.sendFCM(userStatus.user.tokenFCM)
                        callFCMFAB.visibility = View.INVISIBLE
                        hangUpFAB.visibility = View.VISIBLE
                    }
                    hangUpFAB.setOnClickListener {
                        finish()
                    }
                }
            }
            //callFCMFAB.setOnClickListener { viewModel.sendFCM(userData.tokenFCM) }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.d("blablabla", "OutgoingCallActivity Finish")
        finish()
    }
}