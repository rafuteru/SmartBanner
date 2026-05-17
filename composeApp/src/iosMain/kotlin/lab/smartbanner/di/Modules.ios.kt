package lab.smartbanner.di

import lab.smartbanner.utils.createDataStore
import lab.smartbanner.utils.createPosterExporter
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val platformModule: Module = module {
    single { createDataStore(null) }
    single { createPosterExporter(null) }
    single(named("isDebug")) { kotlin.native.Platform.isDebugBinary }
}
