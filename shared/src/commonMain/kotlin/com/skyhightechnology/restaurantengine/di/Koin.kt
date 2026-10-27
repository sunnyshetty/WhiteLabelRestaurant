package com.skyhightechnology.restaurantengine.di

import com.skyhightechnology.restaurantengine.data.repository.FirestoreRestaurantRepository
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import com.skyhightechnology.restaurantengine.getApplicationId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val TenantId = named("tenantId")

val engineModule = module {
    single(TenantId) { getApplicationId() }
    // Resolved lazily: Firebase must be initialized (per-brand config files) before first use.
    single<FirebaseFirestore> { Firebase.firestore }
    single<RestaurantRepository> { FirestoreRestaurantRepository(get()) }
}

/** Starts Koin with the engine's modules. Each platform entry point calls this once at startup. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(engineModule)
}
