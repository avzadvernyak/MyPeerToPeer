package m.kampukter.mypeertopeer

import android.content.Intent
import androidx.lifecycle.LifecycleService
import m.kampukter.mypeertopeer.data.RTCRepository
import org.koin.android.ext.android.inject

class WebSocketService : LifecycleService() {
    private val repository by inject<RTCRepository>()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        repository.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.disconnect()
    }
}