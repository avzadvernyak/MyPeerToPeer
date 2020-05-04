package m.kampukter.mypeertopeer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.myId
import m.kampukter.mypeertopeer.myName
import m.kampukter.mypeertopeer.ui.CallActivity.Companion.EXTRA_MESSAGE_CANDIDATE
import org.koin.androidx.viewmodel.ext.android.viewModel


/*
1 важное - наследник PeerConnection.Observer
2 важное - инициализация, тут она в init
3 важное - конфигурация стримов, тут она в setupMediaDevices
и последнее важное - диспозить это все ибо оно нативное и коллектор его не выгребет
 */

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var usersAdapter: UsersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        setSupportActionBar(mainToolbar).apply {
            title = getString(R.string.main_toolbar_title, myName)
        }

        viewModel.negotiationEvent.observe(
            this,
            Observer {
                when (it) {
                    is NegotiationEvent.IncomingCall -> {
                        AlertDialog.Builder(this).setTitle(getString(R.string.incoming_call))
                            .setMessage(getString(R.string.message_title, it.from))
                            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                                startActivity(
                                    Intent(this, CallActivity::class.java).putExtra(
                                        EXTRA_MESSAGE_CANDIDATE,
                                        it.from
                                    )
                                )
                            }
                            .setNegativeButton(getString(R.string.reject)) { _, _ -> }
                            .create().show()
                    }
                }
            })
        viewModel.listCalledUserLiveData.observe(this, Observer { listCalledUser ->
            usersAdapter?.setList(listCalledUser.filter { it.id != myId })
        })
        usersAdapter = UsersAdapter { item ->
            startActivity(
                Intent(this, OutgoingCallActivity::class.java).putExtra(
                    EXTRA_MESSAGE_CANDIDATE,
                    item.id
                )
            )
        }

        with(usersRecyclerView) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = usersAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("blablabla", "Destroy MainActivity")
    }
}