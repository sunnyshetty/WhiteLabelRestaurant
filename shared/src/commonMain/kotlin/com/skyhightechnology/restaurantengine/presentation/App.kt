package com.skyhightechnology.restaurantengine.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Root composable: brand theme, then onboarding until a branch is chosen, then the branch flow. */
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
                else -> BranchFlow(
                    brandName = config.name,
                    branchId = branchId,
                    onChangeBranch = { globalViewModel.onAction(GlobalConfigAction.ClearBranch) },
                )
            }
        }
    }
}

@Serializable
private data object RestaurantRoute

@Serializable
private data object MenuRoute

@Serializable
private data object ReservationRoute

/** Restaurant home, with Menu and Book a table pushed on top; leaving the home returns to the location list. */
@Composable
private fun BranchFlow(brandName: String, branchId: String, onChangeBranch: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = RestaurantRoute) {
        composable<RestaurantRoute> {
            RestaurantScreen(
                brandName = brandName,
                branchId = branchId,
                onBack = onChangeBranch,
                onBookTable = { navController.navigate(ReservationRoute) },
                onViewMenu = { navController.navigate(MenuRoute) },
            )
        }
        composable<MenuRoute> {
            BackScaffold(Res.string.nav_menu, onBack = { navController.navigateUp() }) { MenuScreen(branchId = branchId) }
        }
        composable<ReservationRoute> {
            BackScaffold(Res.string.restaurant_book_table, onBack = { navController.navigateUp() }) {
                ReservationScreen(branchId = branchId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackScaffold(title: StringResource, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
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
