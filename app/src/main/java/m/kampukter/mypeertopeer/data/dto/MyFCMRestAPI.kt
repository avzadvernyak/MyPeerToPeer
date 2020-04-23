package m.kampukter.mypeertopeer.data.dto

import android.util.Log
import m.kampukter.mypeertopeer.myId
import m.kampukter.mypeertopeer.myName
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class MyFCMRestAPI : FCMRestAPI {

    private val titleMessage = "MyPeerToPeer"
    private val keyAPI = "AAAANbTFWL0:APA91bFa92BwBEOIQWvIaJUszoZKzZ5ZGidbHH_-0dV77cyMT-LWnlXwnZDyKPpWx6HpDHAfRJ3JMmA7prjb3t6A8eiNzjqhCZm6R6zPi8iBTTAmKBNpYl20RdnOHfUWXDFSmQqjbYGv"

    private val mClient = OkHttpClient()
    private val myJSON = MediaType.parse("application/json")

    override fun send(respondentToken: String) {
        val notification = JSONObject()
        val data = JSONObject()
        val root = JSONObject()
        notification.put("title", titleMessage)
        notification.put("from_name", myName)
        notification.put("from_id", myId)
        data.put("body", notification)
        root.put("data", data)
        root.put("to", respondentToken)
        val body = RequestBody.create(myJSON, root.toString())
        /*

         root.put("token", respondentToken)
        val message = JSONObject()
        message.put("message",root)
        val body = RequestBody.create(myJSON, message.toString())

        .url("https://fcm.googleapis.com/fcm/send")
        .addHeader("Authorization", "key=$keyAPI")

        .url("https://fcm.googleapis.com/v1/projects/sublime-amp-262411/messages:send")
            .addHeader("Content-Type", "application/json; UTF-8")
            .addHeader("Authorization", "Bearer $keyAPI")
         */
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .addHeader("Authorization", "key=$keyAPI")
            .post(body)
            .build()
        mClient.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("blablabla", "Failure")
            }
            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) Log.d("blablabla", "FCM -> Send")
                else Log.d("blablabla", "FCM -> NO Send")
            }
        })
    }
}