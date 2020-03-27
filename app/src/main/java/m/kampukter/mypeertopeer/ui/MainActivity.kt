package m.kampukter.mypeertopeer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.ParcelObjectOffer
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import org.koin.android.ext.android.inject

/*
1 важное - наследник PeerConnection.Observer
2 важное - инициализация, тут она в init
3 важное - конфигурация стримов, тут она в setupMediaDevices
и последнее важное - диспозить это все ибо оно нативное и коллектор его не выгребет
 */

class MainActivity : AppCompatActivity() {

    private val service: NegotiationAPI by inject()
    private var usersAdapter: UsersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Log.d("blablabla", "onCreate MainActivity ${Thread.currentThread().name}")
        service.connect()

        usersAdapter = UsersAdapter { item ->
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_MESSAGE_CANDIDATE, ParcelObjectOffer(item, null))
            startActivity(Intent(this, SecondVerActivity::class.java).putExtra("Bundle", bundle))
        }

        with(usersRecyclerView) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = usersAdapter
        }
        callFAB.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_MESSAGE_CANDIDATE, ParcelObjectOffer(null, null))
            startActivity(Intent(this, SecondVerActivity::class.java).putExtra("Bundle", bundle))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        service.disconnect()
    }

    override fun onResume() {
        super.onResume()

        service.onNegotiationEventMain = this::negotiationMessageListener
    }


    private fun negotiationMessageListener(message: NegotiationEvent) {
        runOnUiThread {
            when (message) {
                is NegotiationEvent.Discovery -> {
                    usersAdapter?.setList(message.userIds)
                }
                /*is NegotiationEvent.Offer -> {
                    val bundle = Bundle()
                    bundle.putParcelable(
                        EXTRA_MESSAGE_CANDIDATE,
                        ParcelObjectOffer(message.from, message.sdp)
                    )
                    startActivity(
                        Intent(this, SecondVerActivity::class.java).putExtra(
                            "Bundle",
                            bundle
                        )
                    )
                }*/
            }
        }
    }

    companion object {
        const val EXTRA_MESSAGE_CANDIDATE = "EXTRA_MESSAGE_CANDIDATE"
    }
}