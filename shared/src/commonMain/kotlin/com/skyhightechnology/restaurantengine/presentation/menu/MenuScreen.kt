package com.skyhightechnology.restaurantengine.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.presentation.component.AppIcons
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.menu_empty
import com.skyhightechnology.restaurantengine.resources.menu_price
import com.skyhightechnology.restaurantengine.resources.menu_sold_out
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Menu (Figma "4 · Menu"): one tab and one swipeable page per category. */
@Composable
fun MenuScreen(branchId: String, viewModel: MenuViewModel = koinViewModel()) {
    LaunchedEffect(branchId) { viewModel.onAction(MenuAction.SelectBranch(branchId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val error = state.error
    when {
        state.isLoading -> LoadingView()
        error != null -> ErrorBanner(stringResource(error.messageRes), modifier = Modifier.padding(16.dp))
        state.categories.isEmpty() -> Text(
            stringResource(Res.string.menu_empty),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(24.dp),
        )
        else -> CategoryPager(state.categories)
    }
}

@Composable
private fun CategoryPager(categories: List<MenuCategory>) {
    val pagerState = rememberPagerState { categories.size }
    Column(modifier = Modifier.fillMaxSize()) {
        CategoryTabs(categories, pagerState)
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), key = { categories[it].name }) { page ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(categories[page].items, key = { it.id }) { DishCard(it) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(categories: List<MenuCategory>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val selected = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    PrimaryScrollableTabRow(selectedTabIndex = selected, containerColor = Color.Transparent) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = index == selected,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                text = { Text(category.name, maxLines = 1) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DishCard(item: MenuItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 112.dp)
                .padding(12.dp)
                .alpha(if (item.isAvailable) 1f else 0.6f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Placeholder until menu photos are loaded from imageUrl.
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                item.description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (units, cents) = priceParts(item.priceInCents)
                    Text(
                        stringResource(Res.string.menu_price, units, cents),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (!item.isAvailable) {
                        Tag(
                            stringResource(Res.string.menu_sold_out),
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
    }
}

/** The design's small meta tag (8dp corners, labelSmall). */
@Composable
private fun Tag(text: String, container: Color, content: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = container, contentColor = content) {
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

/** 1299 -> ("12", "99"), the arguments of the `menu_price` template, which owns symbol and separator. */
fun priceParts(priceInCents: Int): Pair<String, String> =
    (priceInCents / 100).toString() to (priceInCents % 100).toString().padStart(2, '0')
