package m.kampukter.mypeertopeer

import android.app.Application
import m.kampukter.mypeertopeer.data.WebSocketRepository
import m.kampukter.mypeertopeer.data.dto.MySignalingWebSocket
import m.kampukter.mypeertopeer.data.dto.SignalingWebSocketAPI
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("unused")
class MainApplication : Application(){
    private val module = module {
        single<SignalingWebSocketAPI> { MySignalingWebSocket() }
        single { WebSocketRepository(get()) }
        viewModel { MyViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }

}