package m.kampukter.mypeertopeer

import android.app.Application
import m.kampukter.mypeertopeer.data.DefaultMainRepository
import m.kampukter.mypeertopeer.data.MainRepository
import m.kampukter.mypeertopeer.data.dto.WebSocketNegotiationAPI
import m.kampukter.mypeertopeer.data.dto.NegotiationAPI
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("unused")
class MainApplication : Application(){
    private val module = module {
        single<NegotiationAPI> { WebSocketNegotiationAPI() }
        single<MainRepository> { DefaultMainRepository(androidContext(),get()) }
        viewModel { MainViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }

}