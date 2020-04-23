package m.kampukter.mypeertopeer.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.mypeertopeer.data.dto.FCMRestAPI
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import m.kampukter.mypeertopeer.data.dto.UsersInfoAPI
import retrofit2.Callback
import m.kampukter.mypeertopeer.rtc.CallSession
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class RTCRepository(
    private val context: Application,
    private val negotiationAPI: NegotiationAPI,
    private val myFCMRestAPI: FCMRestAPI
) {

    private val apiInfoSensor = UsersInfoAPI.create()

    // Заглушки пока нет базы
    private val listCalledUser = listOf(
        UserData(
            "C2yw9XLb-4-R1zlo31STJyblbB5IIVPBJLO",
            "Nexus",
            "ewkqSpOySimG0nmIjX909E:APA91bElyhhhmdrwi51DBa0cWx2bB4DIQkNHe8wLRDsfnnQnJeXBA2zKlm2_tVyQ0Odf0Fbe-Or6Q8wVKTY0Lv7urs9uF4pK20MTNRrX6_KKIOqCitvflK9tmw8t2jrlAyKaOm6ptqRW"

        )
        , UserData(
            "R4OI8E3i-3-5gWHZX35rqsq6qJcYHD1VMdq",
            "Anatoly",
            "fEmnM55RRlaJo1ms7L8lOK:APA91bEiYzl2VyPnmUCd0FGe6S33fujM5fEQ_uh6H3YrLy9BFd8cvA7rZ20ghXWTQxqPV8XNrfpWzKrjGaIY4LrBiinvEoNyu0UBDPbrOe5h2KQEzncsY_Mf8-EEofE_ZHtjQ83DXM75"
        )
    )
    val mapCalledUser = listCalledUser.associateBy({ it.id }, { listOf(it.userName, it.tokenFCM) })
    // Конец заглушки

    private val _listCalledUserLiveData = MutableLiveData<List<UserData>>()
    val listCalledUserLiveData: LiveData<List<UserData>>
        get() = _listCalledUserLiveData

    data class IceCandidateInfo(val sdp: String, val sdpMid: String, val sdpMLineIndex: Int)

    private val listIceCandidateInfo = mutableListOf<IceCandidateInfo>()
    private var receivedOffer: String? = null
    private var lastFrom: String? = null

    private val _userIdsLiveData = MutableLiveData<List<String>>()
    val userIdsLiveData: LiveData<List<String>>
        get() = _userIdsLiveData

    private val _negotiationEvent = MutableLiveData<NegotiationEvent>()
    val negotiationEvent: LiveData<NegotiationEvent>
        get() = _negotiationEvent

    private var callSession: CallSession? = null

    private val onOutgoingNegotiationEvent: (NegotiationEvent) -> Unit = { event ->
        when (event) {
            is NegotiationEvent.Offer -> negotiationAPI.sendOffer(event.to, event.sdp)
            is NegotiationEvent.Answer -> negotiationAPI.sendAnswer(event.to, event.sdp)
            is NegotiationEvent.IceCandidate -> negotiationAPI.sendCandidate(
                event.to,
                event.sdp,
                event.sdpMid,
                event.sdpMLineIndex
            )
            NegotiationEvent.HangUp -> _negotiationEvent.postValue(NegotiationEvent.HangUp)
            NegotiationEvent.Connected -> _negotiationEvent.postValue(NegotiationEvent.Connected)
        }
    }

    init {
        //Получаем данные всех зарегистрированных в приложении пользователей
        getUsersData()

        negotiationAPI.onNegotiationEvent = { event ->
            when (event) {
                is NegotiationEvent.Discovery -> _userIdsLiveData.postValue(event.userIds)
                is NegotiationEvent.Offer -> {
                    receivedOffer = event.sdp
                    lastFrom = event.from
                    _negotiationEvent.postValue(NegotiationEvent.IncomingCall(event.from))
                    /* val intent = Intent(context, MainActivity::class.java)
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     context.startActivity(intent)*/
                }
                is NegotiationEvent.Answer -> {
                    callSession?.handleAnswer(event.sdp)
                }
                is NegotiationEvent.IceCandidate -> {
                    if (callSession != null) callSession?.handleIceCandidate(
                        event.sdp,
                        event.sdpMid,
                        event.sdpMLineIndex
                    )
                    else listIceCandidateInfo.add(
                        IceCandidateInfo(
                            event.sdp,
                            event.sdpMid,
                            event.sdpMLineIndex
                        )
                    )
                }
            }
        }
    }

    fun connect() {
        negotiationAPI.connect()
    }

    fun disconnect() {
        negotiationAPI.disconnect()
    }

    fun startCall(infoLocalVideoCapture: InfoLocalVideoCapture) {
        if (callSession == null) callSession =
            CallSession(
                context,
                infoLocalVideoCapture.userId,
                onOutgoingNegotiationEvent
            ).apply {
                initSurfaceView(infoLocalVideoCapture.localView, infoLocalVideoCapture.remoteView)
                startNewSession(infoLocalVideoCapture.localView)
            }
    }

    fun answerCall(infoLocalVideoCapture: InfoLocalVideoCapture) {
        lastFrom?.let { from ->
            if (callSession == null) callSession =
                CallSession(
                    context,
                    from,
                    onOutgoingNegotiationEvent
                ).apply {
                    initSurfaceView(
                        infoLocalVideoCapture.localView,
                        infoLocalVideoCapture.remoteView
                    )
                    startLocalVideoCapture(infoLocalVideoCapture.localView)
                    receivedOffer?.let { sdp -> handleOffer(sdp) }
                    listIceCandidateInfo.forEach { ice ->
                        handleIceCandidate(
                            ice.sdp,
                            ice.sdpMid,
                            ice.sdpMLineIndex
                        )
                    }
                }
        }
    }

    fun disposeRTC() {
        _negotiationEvent.postValue(NegotiationEvent.Waiting)
        callSession?.disposeAll()
        callSession = null
        receivedOffer = null
        lastFrom = null
        listIceCandidateInfo.clear()
    }

    fun sendFCMMessage(token: String) {
        myFCMRestAPI.send(token)
    }

    private fun getUsersData() {
        val call = apiInfoSensor.getUsersData()
        call.enqueue(object : Callback<List<UserData>> {
            override fun onResponse(
                call: Call<List<UserData>>,
                response: Response<List<UserData>>
            ) {
                response.body().let { _listCalledUserLiveData.postValue(it) }
            }

            override fun onFailure(call: Call<List<UserData>>, t: Throwable) {
                t.message?.let {
                    Log.d("blablabla", "Retrofit2 onFailure GET")
                }
            }
        })
    }

    fun saveUsersData(userData: UserData) {
        val call = apiInfoSensor.saveUserInfo(userData.id, userData.userName, userData.tokenFCM)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("blablabla", "Retrofit2 onFailure POST")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            }
        })
    }

    fun getCalledUserData(userId: String): LiveData<UserData> {

        val retValue = MutableLiveData<UserData>()


        val rtewq = mapCalledUser[userId]?.toList()
        rtewq?.let { retValue.postValue(UserData(id = userId, userName = it[0], tokenFCM = it[1])) }
        return retValue
    }
}

