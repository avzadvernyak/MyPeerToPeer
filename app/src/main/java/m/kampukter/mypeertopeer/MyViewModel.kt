package m.kampukter.mypeertopeer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import m.kampukter.mypeertopeer.data.InfoLocalVideoCapture
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.RTCRepository

class MyViewModel(private val repository: RTCRepository) : ViewModel() {
    val userIdsLiveData = repository.userIdsLiveData
    private val _negotiationEvent = repository.negotiationEvent

    private val infoLocalVideoCaptureLiveData = MutableLiveData<InfoLocalVideoCapture>()

    fun startCall(infoLocalVideoCapture: InfoLocalVideoCapture) {
        infoLocalVideoCaptureLiveData.postValue(infoLocalVideoCapture)
    }

    fun dispose() {
        repository.disposeRTC()
    }

    val negotiationEvent: LiveData<NegotiationEvent> = MediatorLiveData<NegotiationEvent>().apply {
        var isIncomingCall = false
        addSource(_negotiationEvent){
            postValue(it)
            isIncomingCall = when(it){
                is NegotiationEvent.IncomingCall -> true
                else -> false
            }
        }
        addSource(infoLocalVideoCaptureLiveData){
            if (isIncomingCall) repository.answerCall(it)
            else repository.startCall(it)
        }
    }
    fun sendFCM() {
        repository.sendFCMMessage()
    }
}