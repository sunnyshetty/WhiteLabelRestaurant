package com.skyhightechnology.restaurantengine.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_retry
import com.skyhightechnology.restaurantengine.resources.onboarding_choose_branch
import com.skyhightechnology.restaurantengine.resources.onboarding_no_branches
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Branch picker. [onBranchSelected] should dispatch `GlobalConfigAction.SelectBranch`. */
@Composable
fun OnboardingScreen(
    brandName: String,
    onBranchSelected: (branchId: String) -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        BrandHeader(brandName)
        val error = state.error
        when {
            state.isLoading -> LoadingView()
            error != null -> ErrorBanner(
                message = stringResource(error.messageRes),
                actionLabel = stringResource(Res.string.action_retry),
                onAction = { viewModel.onAction(OnboardingAction.Retry) },
                modifier = Modifier.padding(16.dp),
            )
            state.branches.isEmpty() -> Text(
                stringResource(Res.string.onboarding_no_branches),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(24.dp),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.branches, key = { it.id }) { branch ->
                    BranchCard(branch, onClick = { onBranchSelected(branch.id) })
                }
            }
        }
    }
}

/** Logo placeholder: the brand initial on the primary color, until tenants supply a logo asset. */
@Composable
private fun BrandHeader(brandName: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                brandName.firstOrNull()?.uppercase().orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(brandName, style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(Res.string.onboarding_choose_branch),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BranchCard(branch: RestaurantBranch, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(branch.name, style = MaterialTheme.typography.titleMedium)
            Text(branch.address, style = MaterialTheme.typography.bodyMedium)
            Text(
                branch.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
