---
name: unit-test-generator
description: Generate unit tests for WhiteLabelRestaurant use cases, repositories, and ViewModels using kotlin.test, kotlinx-coroutines-test, and Turbine. Use when adding or changing logic under shared/src/commonMain.
---

> **DRAFT — replace with the team's previously established rules.** These defaults were written
> from the project's test stack; the original rule set was not available when this file was created.

# Unit Test Generator

## Where tests go

- Mirror the production package under `shared/src/commonTest/kotlin/com/skyhightechnology/restaurantengine/`.
  `domain/usecase/GetMenuUseCase.kt` → `domain/usecase/GetMenuUseCaseTest.kt`.
- Tests are **common** by default so they run on JVM, Android host, and iOS. Only use
  `jvmTest`/`androidHostTest`/`iosTest` for code that is itself platform-specific.

## Stack (already in `commonTest`)

- `kotlin.test` — `@Test`, `assertEquals`, `assertIs`, `assertFailsWith`. No JUnit-only APIs in common tests.
- `kotlinx.coroutines.test` — every suspending test body is `= runTest { … }`.
- Turbine — every `Flow`/`StateFlow` assertion uses `flow.test { awaitItem() … }`; never `first()` + `delay`.
- `koin-test` — only for verifying module graphs (`checkModules`/`verify`), not for unit tests.

## Rules

1. **Fakes over mocks.** Write hand-rolled fakes implementing `domain/repository` interfaces
   (e.g. `FakeRestaurantRepository`) in `commonTest/.../fake/`. No mocking libraries — they don't run on Kotlin/Native.
2. **Name tests by behaviour**: backtick names `` `returns menu grouped by category when repository succeeds` ``.
   One behaviour per test; Arrange / Act / Assert separated by blank lines.
3. **ViewModels**: replace `Dispatchers.Main` with `StandardTestDispatcher` in `@BeforeTest`
   (`Dispatchers.setMain`) and reset in `@AfterTest`. Assert the full `UiState` sequence with Turbine:
   initial → loading → content/error. End each `test {}` block with `cancelAndIgnoreRemainingEvents()`
   only when later emissions are irrelevant.
4. **Cover every branch**: success, empty, error/exception, and — for tenant-aware code — at least
   two different tenant IDs (`com.burgerjoint.app`, `com.cafelocal.espresso`).
5. **Mappers** (`data/repository`): one test per DTO → domain mapping, including missing/nullable fields.
6. **No real Firebase, network, clock, or randomness** in unit tests. Inject time/ID providers.
7. **Never weaken an assertion to make a test pass**; if behaviour is wrong, report it.

## Verify

```bash
./gradlew :shared:jvmTest                 # fastest feedback
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test   # requires Xcode (license accepted)
```

Single class: append `--tests "com.skyhightechnology.restaurantengine.domain.usecase.GetMenuUseCaseTest"`.
