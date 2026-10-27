package com.skyhightechnology.restaurantengine.presentation

import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoadErrorTest {
    private val allErrors = listOf(LoadError.Offline, LoadError.PermissionDenied, LoadError.NotFound, LoadError.Unknown)

    @Test
    fun `maps domain errors to load errors`() {
        assertEquals(LoadError.Offline, DataException(DataError.Offline).toLoadError())
        assertEquals(LoadError.PermissionDenied, DataException(DataError.PermissionDenied).toLoadError())
        assertEquals(LoadError.NotFound, DataException(DataError.NotFound).toLoadError())
        assertEquals(LoadError.Unknown, DataException(DataError.Unknown).toLoadError())
        assertEquals(LoadError.Unknown, IllegalStateException("bug").toLoadError())
    }

    @Test
    fun `every load error has its own string`() = runTest {
        assertEquals(allErrors.size, allErrors.map { it.messageRes }.toSet().size)
        allErrors.forEach { assertTrue(getString(it.messageRes).isNotBlank(), it.toString()) }
    }
}
