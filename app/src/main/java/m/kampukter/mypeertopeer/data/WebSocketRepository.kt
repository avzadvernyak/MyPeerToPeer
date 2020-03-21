package m.kampukter.mypeertopeer.data

import m.kampukter.mypeertopeer.data.dto.SignalingWebSocketAPI
import m.kampukter.mypeertopeer.ui.myName
import java.net.URL

class WebSocketRepository(
    private val signalingWebSocketAPI: SignalingWebSocketAPI
) {
    private val urlWebSocketServer: URL = URL("http://176.37.84.130:8080/$myName")
    fun connectToDevices() {
        signalingWebSocketAPI.connect(urlWebSocketServer)
    }
    fun disconnect() {
        signalingWebSocketAPI.disconnect(urlWebSocketServer)
    }

    fun webSocketStatus(url: URL) = signalingWebSocketAPI.getConnectStatusLiveData(url)
    fun getMessage(url: URL) = signalingWebSocketAPI.getMessageLiveData(url)
    fun send(dataObject: NegotiationMessage) {
        signalingWebSocketAPI.send(urlWebSocketServer, dataObject)
    }
}