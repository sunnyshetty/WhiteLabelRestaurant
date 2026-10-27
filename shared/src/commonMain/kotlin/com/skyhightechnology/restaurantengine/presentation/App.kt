package com.skyhightechnology.restaurantengine.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skyhightechnology.restaurantengine.presentation.component.AppIcons
import com.skyhightechnology.restaurantengine.presentation.component.FullScreenError
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.menu.MenuScreen
import com.skyhightechnology.restaurantengine.presentation.onboarding.OnboardingScreen
import com.skyhightechnology.restaurantengine.presentation.reservation.ReservationScreen
import com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigAction
import com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigViewModel
import com.skyhightechnology.restaurantengine.presentation.theme.RestaurantEngineTheme
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_change_branch
import com.skyhightechnology.restaurantengine.resources.nav_menu
import com.skyhightechnology.restaurantengine.resources.nav_reserve
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.reflect.KClass

/** Root composable: brand theme, then onboarding until a branch is chosen, then the tabbed shell. */
@Composable
fun App() {
    val globalViewModel = koinViewModel<GlobalConfigViewModel>()
    val state by globalViewModel.state.collectAsStateWithLifecycle()

    RestaurantEngineTheme(state.brandConfig) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val config = state.brandConfig
            val branchId = state.selectedBranchId
            when {
                config == null && state.isLoading -> LoadingView()
                config == null -> FullScreenError(
                    message = stringResource((state.error ?: LoadError.Unknown).messageRes),
                    onRetry = { globalViewModel.onAction(GlobalConfigAction.Retry) },
                )
                branchId == null -> OnboardingScreen(
                    brandName = config.name,
                    onBranchSelected = { globalViewModel.onAction(GlobalConfigAction.SelectBranch(it)) },
                )
                else -> MainShell(
                    brandName = config.name,
                    branchId = branchId,
                    onChangeBranch = { globalViewModel.onAction(GlobalConfigAction.ClearBranch) },
                )
            }
        }
    }
}

@Serializable
private data object MenuRoute

@Serializable
private data object ReservationRoute

private enum class Tab(val label: StringResource, val icon: ImageVector, val route: Any, val routeClass: KClass<*>) {
    Menu(Res.string.nav_menu, AppIcons.RestaurantMenu, MenuRoute, MenuRoute::class),
    Reserve(Res.string.nav_reserve, AppIcons.Event, ReservationRoute, ReservationRoute::class),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(brandName: String, branchId: String, onChangeBranch: () -> Unit) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(brandName) },
                actions = { TextButton(onClick = onChangeBranch) { Text(stringResource(Res.string.action_change_branch)) } },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(tab.routeClass) == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = MenuRoute, modifier = Modifier.padding(padding)) {
            composable<MenuRoute> { MenuScreen(branchId = branchId) }
            composable<ReservationRoute> { ReservationScreen(branchId = branchId) }
        }
    }
}
