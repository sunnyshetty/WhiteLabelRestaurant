package com.skyhightechnology.restaurantengine.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.skyhightechnology.restaurantengine.presentation.component.AppIcons
import com.skyhightechnology.restaurantengine.presentation.component.FullScreenError
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.menu.MenuScreen
import com.skyhightechnology.restaurantengine.presentation.onboarding.OnboardingScreen
import com.skyhightechnology.restaurantengine.presentation.reservation.ReservationScreen
import com.skyhightechnology.restaurantengine.presentation.restaurant.RestaurantScreen
import com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigAction
import com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigViewModel
import com.skyhightechnology.restaurantengine.presentation.theme.RestaurantEngineTheme
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_back
import com.skyhightechnology.restaurantengine.resources.nav_menu
import com.skyhightechnology.restaurantengine.resources.restaurant_book_table
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Root composable: brand theme, then the navigation back stack once the brand config has loaded. */
@Composable
fun App() {
    val globalViewModel = koinViewModel<GlobalConfigViewModel>()
    val state by globalViewModel.state.collectAsStateWithLifecycle()

    RestaurantEngineTheme(state.brandConfig) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val config = state.brandConfig
            when {
                config == null && state.isLoading -> LoadingView()
                config == null -> FullScreenError(
                    message = stringResource((state.error ?: LoadError.Unknown).messageRes),
                    onRetry = { globalViewModel.onAction(GlobalConfigAction.Retry) },
                )
                else -> AppNavigation(brandName = config.name)
            }
        }
    }
}

@Serializable
private data object BranchesRoute : NavKey

@Serializable
private data class RestaurantRoute(val branchId: String) : NavKey

/** [branchName] is the app bar subtitle; the Restaurant screen already has it loaded. */
@Serializable
private data class MenuRoute(val branchId: String, val branchName: String) : NavKey

/** [branchName] is the app bar subtitle, as for [MenuRoute]. */
@Serializable
private data class ReservationRoute(val branchId: String, val branchName: String) : NavKey

/** Every route, so the back stack can be saved and restored on all platforms (no reflection on iOS). */
private val navConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(BranchesRoute::class, BranchesRoute.serializer())
            subclass(RestaurantRoute::class, RestaurantRoute.serializer())
            subclass(MenuRoute::class, MenuRoute.serializer())
            subclass(ReservationRoute::class, ReservationRoute.serializer())
        }
    }
}

/**
 * Branch list → restaurant → menu or book a table. The back stack is a plain list: opening a screen adds
 * its route, and back (app bar arrow or system back) removes the last one. Back on the branch list leaves the app.
 */
@Composable
private fun AppNavigation(brandName: String) {
    val backStack = rememberNavBackStack(navConfiguration, BranchesRoute)
    val goBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        // Each entry keeps its own saved UI state and ViewModels, cleared when it leaves the back stack.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<BranchesRoute> {
                OnboardingScreen(
                    brandName = brandName,
                    onBranchSelected = { branchId -> backStack.add(RestaurantRoute(branchId)) },
                )
            }
            entry<RestaurantRoute> { route ->
                RestaurantScreen(
                    brandName = brandName,
                    branchId = route.branchId,
                    onBack = goBack,
                    onBookTable = { branchName -> backStack.add(ReservationRoute(route.branchId, branchName)) },
                    onViewMenu = { branchName -> backStack.add(MenuRoute(route.branchId, branchName)) },
                )
            }
            entry<MenuRoute> { route ->
                BackScaffold(title = Res.string.nav_menu, subtitle = route.branchName, onBack = goBack) {
                    MenuScreen(branchId = route.branchId)
                }
            }
            entry<ReservationRoute> { route ->
                BackScaffold(title = Res.string.restaurant_book_table, subtitle = route.branchName, onBack = goBack) {
                    ReservationScreen(branchId = route.branchId)
                }
            }
        },
    )
}

/** Small app bar with back arrow. Content draws behind the navigation bar and pads for it itself. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackScaffold(
    title: StringResource,
    onBack: () -> Unit,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(title))
                        if (!subtitle.isNullOrBlank()) {
                            Text(subtitle, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding)) { content() }
    }
}
