package m.kampukter.mypeertopeer

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import m.kampukter.mypeertopeer.data.RTCRepository
import m.kampukter.mypeertopeer.data.dto.FCMRestAPI
import m.kampukter.mypeertopeer.data.dto.MyFCMRestAPI
import m.kampukter.mypeertopeer.data.dto.WebSocketNegotiationAPI
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import m.kampukter.mypeertopeer.ui.UserActivity
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

private const val APP_PREFERENCES = "appSettings"
private const val APP_PREFERENCES_USER = "user"
private const val APP_PREFERENCES_USER_ID = "userId"
var myName: String? = null
var myId: String? = null
lateinit var privateNotesApplication: MainApplication

@Suppress("unused")
class MainApplication : Application() {
    private var appSharedPreferences: SharedPreferences? = null
    private val module = module {
        single<NegotiationAPI> { WebSocketNegotiationAPI() }
        single<FCMRestAPI> { MyFCMRestAPI() }
        single { RTCRepository(this@MainApplication, get(), get()) }
        viewModel { MyViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()

        privateNotesApplication = this@MainApplication
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
        appSharedPreferences = getSharedPreferences(APP_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        appSharedPreferences?.let { appShare ->
            if (appShare.contains(APP_PREFERENCES_USER)) {
                appShare.getString(APP_PREFERENCES_USER, null)?.let {
                    myName = it
                }
                appShare.getString(APP_PREFERENCES_USER_ID, null)?.let {
                    myId = it
                }
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            private val serviceIntent = Intent(this@MainApplication, WebSocketService::class.java)

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onForeground() {
                if (myName.isNullOrEmpty() || myId.isNullOrEmpty() ) {
                    startActivity(
                        Intent(baseContext, UserActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                } else startService(serviceIntent)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onBackground() {
                stopService(serviceIntent)
            }
        })
    }

    fun saveMyName(userName: String, userId: String) {
        if (userName.isNotBlank()) {
            myName = userName
            myId = userId
            appSharedPreferences?.edit()
                ?.putString(APP_PREFERENCES_USER, userName)
                ?.putString(APP_PREFERENCES_USER_ID, userId)
                ?.apply()
            Log.e("blablabla", "myName $myName myId $myId")
        } else Log.e("blablabla", "Login is bad")
    }

}