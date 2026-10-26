package com.skyhightechnology.restaurantengine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.skyhightechnology.restaurantengine.di.TenantId
import com.skyhightechnology.restaurantengine.presentation.theme.RestaurantEngineTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    RestaurantEngineTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val tenantId: String = koinInject(TenantId)
            Box(
                modifier = Modifier.fillMaxSize().safeContentPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Tenant: $tenantId", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
