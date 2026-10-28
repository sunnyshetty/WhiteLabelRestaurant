package com.skyhightechnology.restaurantengine.presentation

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** JVM only: reading string resources needs Android's real resources on the Android host-test target. */
class LoadErrorStringsTest {
    private val allErrors = listOf(LoadError.Offline, LoadError.PermissionDenied, LoadError.NotFound, LoadError.Unknown)

    @Test
    fun `every load error has its own string`() = runTest {
        assertEquals(allErrors.size, allErrors.map { it.messageRes }.toSet().size)
        allErrors.forEach { assertTrue(getString(it.messageRes).isNotBlank(), it.toString()) }
    }
}
