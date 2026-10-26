package com.skyhightechnology.restaurantengine.di

import com.skyhightechnology.restaurantengine.getApplicationId
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val TenantId = named("tenantId")

val engineModule = module {
    single(TenantId) { getApplicationId() }
}

/** Starts Koin with the engine's modules. Each platform entry point calls this once at startup. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(engineModule)
}
