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
import m.kampukter.mypeertopeer.data.dto.WebSocketNegotiationAPI
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import m.kampukter.mypeertopeer.ui.UserActivity
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

private const val APP_PREFERENCES = "appSettings"
private const val APP_PREFERENCES_USER = "user"
var myName: String? = null
lateinit var privateNotesApplication: MainApplication

@Suppress("unused")
class MainApplication : Application() {
    private var appSharedPreferences: SharedPreferences? = null
    private val module = module {
        single<NegotiationAPI> { WebSocketNegotiationAPI() }
        single { RTCRepository(this@MainApplication, get()) }
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
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onForeground() {
                if (myName.isNullOrEmpty()) {
                    startActivity(
                        Intent(baseContext, UserActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                }
            }
        })
    }

    fun saveMyName(userName: String) {
        if (userName.isNotBlank()) {
            myName = userName

            appSharedPreferences?.edit()?.putString(APP_PREFERENCES_USER, userName)?.apply()

        } else Log.e("blablabla", "Login is bad")
    }

}