package lab.smartbanner

import android.app.Application
import lab.smartbanner.di.initKoin
import org.koin.android.ext.koin.androidContext

class SmartBannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SmartBannerApp)
        }
    }
}
