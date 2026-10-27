package com.skyhightechnology.restaurantengine.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import kotlin.test.Test

class DependencyInjectionTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `verify all common koin modules assemble cleanly without leaks`() {
        // Statically verifies definition integrity without instantiating live infrastructure dependencies
        engineModule.verify()
    }
}
