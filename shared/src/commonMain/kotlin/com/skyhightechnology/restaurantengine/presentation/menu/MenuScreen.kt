package com.skyhightechnology.restaurantengine.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.menu_empty
import com.skyhightechnology.restaurantengine.resources.menu_in_stock
import com.skyhightechnology.restaurantengine.resources.menu_price
import com.skyhightechnology.restaurantengine.resources.menu_sold_out
import com.skyhightechnology.restaurantengine.resources.menu_tab_all
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MenuScreen(branchId: String, viewModel: MenuViewModel = koinViewModel()) {
    LaunchedEffect(branchId) { viewModel.onAction(MenuAction.SelectBranch(branchId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.categories.isNotEmpty()) {
            CategoryTabs(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = { viewModel.onAction(MenuAction.SelectCategory(it)) },
            )
        }
        val error = state.error
        when {
            state.isLoading -> LoadingView()
            error != null -> ErrorBanner(stringResource(error.messageRes), modifier = Modifier.padding(16.dp))
            state.items.isEmpty() -> Text(
                stringResource(Res.string.menu_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(24.dp),
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.id }) { MenuItemCard(it) }
            }
        }
    }
}

/** "All" plus one tab per category; `null` selects "All". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(categories: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    val tabs = listOf<String?>(null) + categories
    PrimaryScrollableTabRow(selectedTabIndex = tabs.indexOf(selected).coerceAtLeast(0), edgePadding = 12.dp) {
        tabs.forEach { category ->
            Tab(
                selected = category == selected,
                onClick = { onSelect(category) },
                text = { Text(category ?: stringResource(Res.string.menu_tab_all)) },
            )
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp).alpha(if (item.isAvailable) 1f else 0.6f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            item.description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val (units, cents) = priceParts(item.priceInCents)
                Text(
                    stringResource(Res.string.menu_price, units, cents),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                StockIndicator(item.isAvailable)
            }
        }
    }
}

@Composable
private fun StockIndicator(isAvailable: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isAvailable) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
        contentColor = if (isAvailable) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            stringResource(if (isAvailable) Res.string.menu_in_stock else Res.string.menu_sold_out),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/** 1299 -> ("12", "99"), the arguments of the `menu_price` template, which owns symbol and separator. */
fun priceParts(priceInCents: Int): Pair<String, String> =
    (priceInCents / 100).toString() to (priceInCents % 100).toString().padStart(2, '0')
