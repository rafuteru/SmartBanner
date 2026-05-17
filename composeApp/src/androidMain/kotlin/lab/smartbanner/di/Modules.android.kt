package lab.smartbanner.di

import android.content.pm.ApplicationInfo
import lab.smartbanner.utils.createDataStore
import lab.smartbanner.utils.createPosterExporter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { createDataStore(androidContext()) }
    single { createPosterExporter(androidContext()) }
    single(named("isDebug")) {
        (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
