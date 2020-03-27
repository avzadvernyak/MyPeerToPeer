package m.kampukter.mypeertopeer.data

import androidx.lifecycle.LiveData

interface MainRepository {
    val userIdsLiveData: LiveData<List<String>>
    fun connect()
    fun disconnect()
    fun startCall(userId: String)
}
