package m.kampukter.mypeertopeer

import android.app.Application
import m.kampukter.mypeertopeer.data.dto.WebSocketNegotiationAPI
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("unused")
class MainApplication : Application(){
    private val module = module {
        single<NegotiationAPI> { WebSocketNegotiationAPI() }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }

}