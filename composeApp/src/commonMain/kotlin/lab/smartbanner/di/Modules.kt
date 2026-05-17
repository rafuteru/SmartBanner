package lab.smartbanner.di

import lab.smartbanner.data.AccessCodeRepositoryImpl
import lab.smartbanner.data.DataStoreDraftRepository
import lab.smartbanner.data.FirebaseConfigRepository
import lab.smartbanner.data.LocalTemplateRepository
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.ConfigRepository
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.ui.auth.AuthViewModel
import lab.smartbanner.ui.home.HomeViewModel
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val commonModule = module {
    single<TemplateRepository> { LocalTemplateRepository(get(), get()) }
    single<DraftRepository> { DataStoreDraftRepository(get()) }
    
    single<AccessCodeRepository> { AccessCodeRepositoryImpl(get()) }
    
    // Inject isDebug from platformModule
    single<ConfigRepository> { FirebaseConfigRepository(get(named("isDebug"))) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::TemplatePreviewViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }
