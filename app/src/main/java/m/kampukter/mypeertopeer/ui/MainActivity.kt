package m.kampukter.mypeertopeer.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.mypeertopeer.MyViewModel
import m.kampukter.mypeertopeer.R
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
1 важное - наследник PeerConnection.Observer
2 важное - инициализация, тут она в init
3 важное - конфигурация стримов, тут она в setupMediaDevices
и последнее важное - диспозить это все ибо оно нативное и коллектор его не выгребет
 */

val myName: String
    get() = "user_${Build.BOOTLOADER}"

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var usersAdapter: UsersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewModel.connect()
        viewModel.isOffer.observe(
            this,
            Observer {
                if (it) startActivity(
                    Intent(this, AnswerActivity::class.java)
                )
            })
        viewModel.userIdsLiveData.observe(this, Observer { usersAdapter?.setList(it) })
        usersAdapter = UsersAdapter { item ->
            startActivity(
                Intent(this, SecondVerActivity::class.java).putExtra(
                    EXTRA_MESSAGE_CANDIDATE,
                    item
                )
            )
        }

        with(usersRecyclerView) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = usersAdapter
        }
        callFAB.setOnClickListener {
            startActivity(
                Intent(this, SecondVerActivity::class.java).putExtra(
                    EXTRA_MESSAGE_CANDIDATE,
                    ""
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.disconnect()
    }

    companion object {
        const val EXTRA_MESSAGE_CANDIDATE = "EXTRA_MESSAGE_CANDIDATE"
    }
}