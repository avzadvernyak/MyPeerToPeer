package m.kampukter.mypeertopeer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import m.kampukter.mypeertopeer.data.NegotiationMessage
import m.kampukter.mypeertopeer.data.WebSocketRepository
import m.kampukter.mypeertopeer.data.dto.SignalingWebSocketAPI
import java.net.URL

class MyViewModel(private val webSocketRepository: WebSocketRepository) : ViewModel() {

    fun send( dataObject: NegotiationMessage ){ webSocketRepository.send( dataObject)}
    fun connectToWS() {
        webSocketRepository.connectToDevices()
    }
    fun disconnect() {
        webSocketRepository.disconnect()
    }

    private val _urlWsServer = MutableLiveData<URL>()
    fun setWsServerURL(url: URL) {
        _urlWsServer.postValue(url)
    }

    val connectionStatusLiveData: LiveData<SignalingWebSocketAPI.ConnectionStatus> =
        Transformations.switchMap(_urlWsServer) { url -> webSocketRepository.webSocketStatus(url) }
    val webSocketMessageLiveData: LiveData<SignalingWebSocketAPI.Message> =
        Transformations.switchMap(_urlWsServer) { url ->
            webSocketRepository.getMessage(url)
        }
    /*val connectionStatusLiveData: LiveData<SignalingWebSocketAPI.ConnectionStatus> =
       webSocketRepository.webSocketStatus()*/
}