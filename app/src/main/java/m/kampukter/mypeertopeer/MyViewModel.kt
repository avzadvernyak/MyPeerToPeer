package m.kampukter.mypeertopeer

import android.util.Log
import androidx.lifecycle.*
import m.kampukter.mypeertopeer.data.InfoLocalVideoCapture
import m.kampukter.mypeertopeer.data.NegotiationEvent
import m.kampukter.mypeertopeer.data.RTCRepository
import m.kampukter.mypeertopeer.data.UserData

class MyViewModel(private val repository: RTCRepository) : ViewModel() {

    val listCalledUserLiveData = repository.listCalledUserLiveData

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
        addSource(_negotiationEvent) {
            postValue(it)
            isIncomingCall = when (it) {
                is NegotiationEvent.IncomingCall -> true
                else -> false
            }
        }
        addSource(infoLocalVideoCaptureLiveData) {
            if (isIncomingCall) repository.answerCall(it)
            else repository.startCall(it)
        }
    }

    fun sendFCM(token: String) {
        repository.sendFCMMessage(token)
    }

    private val calledUserId = MutableLiveData<String>()
    val calledUserDataLiveData: LiveData<UserData> =
        Transformations.switchMap(calledUserId) { userId -> repository.getCalledUserData(userId) }

    fun setUserDataId(userId: String?) {
        calledUserId.postValue(userId)
        Log.d("blablabla", "setUserDataId $userId")
    }

    sealed class UserStatusEvent {
        data class UserConnected(val user: UserData) : UserStatusEvent()
        data class UserDisconnected(val user: UserData) : UserStatusEvent()
    }

    val currentUserStatus: LiveData<UserStatusEvent> = MediatorLiveData<UserStatusEvent>().apply {
        var candidateUserData: UserData? = null
        var currentIds = listOf<String>()

        addSource(calledUserDataLiveData) { user ->
            candidateUserData = user
            if (currentIds.contains(user.id)) postValue(UserStatusEvent.UserConnected(user))
            else postValue(UserStatusEvent.UserDisconnected(user))
        }
        addSource(userIdsLiveData) { ids ->
            currentIds = ids
            candidateUserData?.let {
                if (ids.contains(it.id)) postValue(UserStatusEvent.UserConnected(it))
                else postValue(UserStatusEvent.UserDisconnected(it))
            }
        }
    }

    // Код работы с базой пользователей
    fun saveUserData(userData: UserData) {repository.saveUsersData(userData)}
}