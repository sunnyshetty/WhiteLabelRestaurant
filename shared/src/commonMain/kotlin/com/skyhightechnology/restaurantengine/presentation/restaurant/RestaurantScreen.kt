package com.skyhightechnology.restaurantengine.presentation.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.presentation.component.AppIcons
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_back
import com.skyhightechnology.restaurantengine.resources.restaurant_address
import com.skyhightechnology.restaurantengine.resources.restaurant_book_table
import com.skyhightechnology.restaurantengine.resources.restaurant_opening_hours
import com.skyhightechnology.restaurantengine.resources.restaurant_phone
import com.skyhightechnology.restaurantengine.resources.restaurant_tables_available
import com.skyhightechnology.restaurantengine.resources.restaurant_tables_full
import com.skyhightechnology.restaurantengine.resources.restaurant_tables_today
import com.skyhightechnology.restaurantengine.resources.restaurant_view_menu
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Branch home (Figma "3 · Restaurant"): hero, brand name, the two primary actions and a details card.
 * [onBack] returns to the location list.
 */
@Composable
fun RestaurantScreen(
    brandName: String,
    branchId: String,
    onBack: () -> Unit,
    onBookTable: (branchName: String) -> Unit,
    onViewMenu: (branchName: String) -> Unit,
    viewModel: RestaurantViewModel = koinViewModel(),
) {
    LaunchedEffect(branchId) { viewModel.onAction(RestaurantAction.SelectBranch(branchId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    RestaurantContent(brandName, state, onBack, onBookTable, onViewMenu)
}

/** Stateless body of [RestaurantScreen], also used by the previews. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantContent(
    brandName: String,
    state: RestaurantState,
    onBack: () -> Unit,
    onBookTable: (branchName: String) -> Unit,
    onViewMenu: (branchName: String) -> Unit,
) {
    val branch = state.branch

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(branch?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        val error = state.error
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            error != null -> ErrorBanner(
                message = stringResource(error.messageRes),
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            branch != null -> BranchDetails(
                brandName = brandName,
                branch = branch,
                onBookTable = { onBookTable(branch.name) },
                onViewMenu = { onViewMenu(branch.name) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun BranchDetails(
    brandName: String,
    branch: RestaurantBranch,
    onBookTable: () -> Unit,
    onViewMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Placeholder until branches carry a photo: the design's hero slot in the brand container colour.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.RestaurantMenu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            brandName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onBookTable, modifier = Modifier.heightIn(min = 56.dp), contentPadding = MediumButtonPadding) {
            Text(stringResource(Res.string.restaurant_book_table), style = MaterialTheme.typography.titleMedium)
        }
        FilledTonalButton(onClick = onViewMenu, modifier = Modifier.heightIn(min = 56.dp), contentPadding = MediumButtonPadding) {
            Text(stringResource(Res.string.restaurant_view_menu), style = MaterialTheme.typography.titleMedium)
        }
        InfoCard(branch)
    }
}

@Composable
private fun InfoCard(branch: RestaurantBranch) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (branch.address.isNotBlank()) {
                InfoRow(AppIcons.LocationOn, stringResource(Res.string.restaurant_address), branch.address)
            }
            if (branch.openingHours.isNotBlank()) {
                InfoRow(AppIcons.Schedule, stringResource(Res.string.restaurant_opening_hours), branch.openingHours)
            }
            branch.hasTablesToday?.let { hasTables ->
                InfoRow(
                    AppIcons.Groups,
                    stringResource(Res.string.restaurant_tables_today),
                    stringResource(if (hasTables) Res.string.restaurant_tables_available else Res.string.restaurant_tables_full),
                )
            }
            if (branch.phoneNumber.isNotBlank()) {
                InfoRow(AppIcons.Phone, stringResource(Res.string.restaurant_phone), branch.phoneNumber)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(icon, contentDescription = null)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** M3 "medium" button: 56dp tall with 24dp side padding. */
private val MediumButtonPadding = PaddingValues(horizontal = 24.dp)
