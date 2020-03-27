package m.kampukter.mypeertopeer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OlegMainActivity : AppCompatActivity() {
/*

    private val mainViewModel by viewModel<MainViewModel>()

    private val userAdapter = UserAdapter()
*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*        setContentView(R.layout.activity_main)

        with(usersView) {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        userAdapter.onClick = { userId ->
            mainViewModel.startCall(userId)
        }

        mainViewModel.userIdsLiveData.observe(this, Observer { userIds ->
            userAdapter.update(userIds)
        })

        mainViewModel.userIdsLiveData.hasActiveObservers()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }*/
    }

}