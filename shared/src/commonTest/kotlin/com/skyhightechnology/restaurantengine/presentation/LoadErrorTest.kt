package com.skyhightechnology.restaurantengine.presentation

import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadErrorTest {
    @Test
    fun `maps domain errors to load errors`() {
        assertEquals(LoadError.Offline, DataException(DataError.Offline).toLoadError())
        assertEquals(LoadError.PermissionDenied, DataException(DataError.PermissionDenied).toLoadError())
        assertEquals(LoadError.NotFound, DataException(DataError.NotFound).toLoadError())
        assertEquals(LoadError.Unknown, DataException(DataError.Unknown).toLoadError())
        assertEquals(LoadError.Unknown, IllegalStateException("bug").toLoadError())
    }
}
