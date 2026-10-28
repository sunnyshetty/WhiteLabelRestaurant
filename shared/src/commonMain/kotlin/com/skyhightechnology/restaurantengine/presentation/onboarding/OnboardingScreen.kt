package com.skyhightechnology.restaurantengine.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.presentation.component.AppIcons
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_retry
import com.skyhightechnology.restaurantengine.resources.onboarding_headline
import com.skyhightechnology.restaurantengine.resources.onboarding_no_branches
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Branch picker (Figma "2 · Home – Choose location"): brand-coloured header with a rounded
 * surface sheet listing the branches. [onBranchSelected] opens the branch.
 */
@Composable
fun OnboardingScreen(
    brandName: String,
    onBranchSelected: (branchId: String) -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnboardingContent(
        brandName = brandName,
        state = state,
        onBranchSelected = onBranchSelected,
        onRetry = { viewModel.onAction(OnboardingAction.Retry) },
    )
}

/** Stateless body of [OnboardingScreen], also used by the previews. */
@Composable
internal fun OnboardingContent(
    brandName: String,
    state: OnboardingState,
    onBranchSelected: (branchId: String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
        BrandHeader(brandName)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            // In a Box: Surface stretches its only child, which would blow the error banner up to fill the sheet.
            Box(Modifier.fillMaxSize()) {
                val error = state.error
                when {
                    state.isLoading -> LoadingView()
                    error != null -> ErrorBanner(
                        message = stringResource(error.messageRes),
                        actionLabel = stringResource(Res.string.action_retry),
                        onAction = onRetry,
                        modifier = Modifier.padding(16.dp),
                    )
                    state.branches.isEmpty() -> Text(
                        stringResource(Res.string.onboarding_no_branches),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp),
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 24.dp,
                            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.branches, key = { it.id }) { branch ->
                            LocationCard(branch, onClick = { onBranchSelected(branch.id) })
                        }
                    }
                }
            }
        }
    }
}

/** Initials mark until tenants supply an in-app logo; the header runs behind the status bar. */
@Composable
private fun BrandHeader(brandName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    brandInitials(brandName),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(brandName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
        Text(
            stringResource(Res.string.onboarding_headline),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun LocationCard(branch: RestaurantBranch, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 80.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    branch.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    branch.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    branch.phoneNumber,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(AppIcons.ChevronForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** First letter of each word, keeping symbols such as "&": "Olive & Ember" → "O&E", "Burger Joint" → "BJ". */
internal fun brandInitials(name: String): String =
    name.split(Regex("\\s+")).filter { it.isNotEmpty() }.take(3).joinToString("") { it.first().uppercase() }
