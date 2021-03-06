package m.kampukter.mypeertopeer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import m.kampukter.mypeertopeer.data.RTCRepository
import m.kampukter.mypeertopeer.data.UserData
import m.kampukter.mypeertopeer.ui.CallActivity
import m.kampukter.mypeertopeer.ui.MainActivity
import org.koin.android.ext.android.inject

data class FcmDataMessage(val from_id: String, val from_name: String, val title: String)
class MyFCMService : FirebaseMessagingService() {

    private val repository by inject<RTCRepository>()
    override fun onNewToken(token: String) {
        Log.d("blablabla", "Refreshed token: $token")
        myId?.let {
            repository.saveUsersData(UserData(id = it, userName = "$myName", token = token))
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let {
            val message = Gson().fromJson(it["body"], FcmDataMessage::class.java)
            sendNotification(message)
        }
    }

    private fun sendNotification(messageBody: FcmDataMessage) {
        val intent = Intent(this, MainActivity::class.java)
        /*val intent = Intent(this, CallActivity::class.java).apply {
            Log.d("blablabla", "Принимаем звонок - сервис")
            putExtra(
                CallActivity.EXTRA_MESSAGE_CANDIDATE,
                messageBody.from_id
            )
        }*/
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(messageBody.title)
            .setContentText(messageBody.from_name)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}