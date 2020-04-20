package m.kampukter.mypeertopeer.data.dto

interface FCMRestAPI {

    fun send(respondentToken: String, from: String?)

}