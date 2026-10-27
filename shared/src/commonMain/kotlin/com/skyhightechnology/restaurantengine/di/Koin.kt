package com.skyhightechnology.restaurantengine.di

import com.skyhightechnology.restaurantengine.data.repository.FirestoreRestaurantRepository
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import com.skyhightechnology.restaurantengine.getApplicationId
import com.skyhightechnology.restaurantengine.presentation.menu.MenuViewModel
import com.skyhightechnology.restaurantengine.presentation.onboarding.OnboardingViewModel
import com.skyhightechnology.restaurantengine.presentation.reservation.ReservationViewModel
import com.skyhightechnology.restaurantengine.presentation.restaurant.RestaurantViewModel
import com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val TenantId = named("tenantId")

val engineModule = module {
    single(TenantId) { getApplicationId() }
    // Resolved lazily: Firebase must be initialized (per-brand config files) before first use.
    single<FirebaseFirestore> { Firebase.firestore }
    single<RestaurantRepository> { FirestoreRestaurantRepository(get()) }

    // ViewModels take the repository directly. The tenant ID is the restaurantId: each brand's data lives at restaurants/{applicationId}.
    viewModel { GlobalConfigViewModel(get(TenantId), get()) }
    viewModel { OnboardingViewModel(get(TenantId), get()) }
    viewModel { RestaurantViewModel(get(TenantId), get()) }
    viewModel { MenuViewModel(get(TenantId), get()) }
    viewModel { ReservationViewModel(get(TenantId), get()) }
}

/** Starts Koin with the engine's modules. Each platform entry point calls this once at startup. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(engineModule)
}
