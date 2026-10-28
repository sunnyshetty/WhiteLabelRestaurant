# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

WhiteLabelRestaurant is a white-label Kotlin Multiplatform restaurant engine (Android, iOS, Desktop/JVM) with a shared Compose Multiplatform UI. One codebase serves many restaurant brands ("tenants"); a brand is selected by the app's platform identifier at runtime, not by forking code.

**Current state:** MVP. The Figma screens (file `AIcERrLsj19p9hy0KwTED9`) are built: choose a branch (onboarding), branch details, menu and book a table, on a brand-tinted Material 3 theme with data from GitLive Firestore (`data/repository/FirestoreRestaurantRepository`). Bookings are saved as `PENDING` documents; nothing processes them yet. The device remembers each branch's latest booking id (`domain/repository/ReservationHistory`, stored with multiplatform-settings), and the booking screen shows that booking, streamed live, while it is upcoming. `domain/usecase` is still empty: ViewModels call the repository directly. Desktop doesn't initialise Firebase, so it can't load data. `README.md` holds the user-facing overview, Firestore layout and known limitations; keep it in step with changes.

**Brands per platform:** Android ships only Burger Joint (`com.burgerjoint.app`); iOS ships only Cafe Local (`com.cafelocal.espresso`). Both use the Firebase project `skyhigh-restaurant-engine`. Each brand's Firebase config file is committed so the apps run straight from a clone (other copies stay gitignored): Android reads `androidApp/google-services.json` (module root — the Google Services plugin does not search `src/main`); iOS reads `iosApp/<brand>/GoogleService-Info.plist`, copied into the app by a build phase that checks its `BUNDLE_ID`.

**Brand artwork:** the source logo for each brand is `branding/<brand>/logo-mark.svg` (a disc in the secondary colour with a glyph in the primary colour). The launcher icon and launch screen are built from it, because both appear before the Firestore config loads.
- **Android:** each flavor's `src/<flavor>/res` supplies:
  - `@color/brand_primary` and `@color/brand_secondary`
  - `@drawable/splash_logo`, used by `Theme.WhiteLabel.Starting` (core-splashscreen)
  - the adaptive `mipmap-anydpi-v26/ic_launcher*.xml` icons and the legacy `mipmap-*dpi` PNGs
- **iOS:** the `<Brand>.xcconfig` sets `ASSETCATALOG_COMPILER_APPICON_NAME`, `LAUNCH_SCREEN_BACKGROUND` and `LAUNCH_SCREEN_LOGO`, which name `AppIcon-<Brand>`, `LaunchBackground-<Brand>` and `LaunchLogo-<Brand>` in `Assets.xcassets`. `Info.plist` reads the last two.
- **PNGs** are rendered from the SVG; nothing reads them directly from Figma.

## Commands

- Android: `./gradlew :androidApp:assembleBurgerjointDebug`
- Desktop: `./gradlew :desktopApp:run` — choose a tenant with `-Dtenant.applicationId=com.burgerjoint.app` (JVM arg)
- iOS: open `iosApp/` in Xcode and pick the `Cafelocal` scheme (configurations `Debug-Cafelocal` / `Release-Cafelocal`, from `iosApp/Configuration/Cafelocal.xcconfig`, which sets `PRODUCT_BUNDLE_IDENTIFIER` and `BRAND`). A new iOS brand = new `<Brand>.xcconfig` + configurations + scheme. The plain Debug/Release configurations have no brand and skip Firebase. The build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`. Linking requires the Xcode license to be accepted (`sudo xcodebuild -license`) and, once Firebase is used, the Firebase iOS SDK added via SPM.
- Tests: `./gradlew :shared:jvmTest` (fastest), `:shared:testAndroidHostTest`, `:shared:iosSimulatorArm64Test`. Single class: append `--tests "com.skyhightechnology.restaurantengine.SomeTest"`.

## Architecture

Modules (`settings.gradle.kts`): `shared` (all engine code + UI, built as static iOS framework `Shared`), and thin entry points `androidApp`, `desktopApp`, `iosApp` (SwiftUI shell, not a Gradle module). Root package everywhere: `com.skyhightechnology.restaurantengine`. Versions live in `gradle/libs.versions.toml`; Java/JVM target is 17 in every module.

### Tenant resolution

- `expect fun getApplicationId()` in `shared/src/commonMain/.../Platform.kt` is the single source of tenant identity.
  - Android: `packageName`, via `AndroidPlatform.init(context)` which **must** run in `WhiteLabelApplication.onCreate()` before anything else.
  - iOS: `NSBundle.mainBundle.bundleIdentifier` (set in `iosApp/Configuration/Config.xcconfig`).
  - JVM: `-Dtenant.applicationId`, defaulting to `com.skyhightechnology.restaurantengine`.
- Koin exposes it as `single(TenantId)` in `di/Koin.kt`. Consume it via injection, never by calling `getApplicationId()` directly from feature code.
- Android brands are `productFlavors` in the `brand` dimension (`androidApp/build.gradle.kts`): each sets `applicationId` and `resValue("string", "app_name", …)`. There is deliberately no static `app_name` in `res/values` — adding one causes a duplicate-resource error. New brand = new flavor block.

### Layers (`shared/src/commonMain/kotlin/com/skyhightechnology/restaurantengine/`)

`domain/{model,repository,usecase}` — pure Kotlin, no Compose/Firebase/serialization/Koin imports. `data/{model,repository}` — `@Serializable` DTOs and repository implementations (Firebase lives here only). `presentation/{component,theme,<feature>}` — composables and `ViewModel`s exposing `StateFlow<UiState>`; never imports `data`. `di/` — the only place implementations are bound to interfaces. Banned imports per layer and the review workflow: `.skills/architecture-linter.md`.

**Errors and text:** repositories throw only `domain.model.DataException(DataError)` (Firestore codes are mapped in `data/repository/FirestoreErrors.kt`). ViewModels convert it to presentation sealed types (`LoadError`, `ReservationError`) in their UI state and never hold display text. All user-facing text lives in `shared/src/commonMain/composeResources/values/strings.xml`; composables map each error case 1:1 via an exhaustive `messageRes` and call `stringResource(Res.string…)` (`Res` is in `com.skyhightechnology.restaurantengine.resources`). No string literals in UI code.

**Screens and previews:** each `<Feature>Screen` only obtains its ViewModel (`koinViewModel()`) and passes state and callbacks to a stateless `internal <Feature>Content`. `@Preview`s call the `Content` composable from `<Feature>Previews.kt` beside the screen, using `@ScreenPreview`, `PreviewTheme`, `BrandPreviewProvider` (once per brand) and sample data from `presentation/preview/PreviewData.kt`. New screens follow the same split.

`koin-core` is an `api` dependency of `shared` because `initKoin()` is called from each platform entry point (Android `WhiteLabelApplication`, desktop `main()`, iOS `MainViewController` configure block).

### Testing

Tests go in `shared/src/commonTest` mirroring the production package, using `kotlin.test` + `runTest` + Turbine, with hand-written fakes (no mocking libraries — they don't run on Kotlin/Native). Details: `.skills/unit-test-generator.md`.

UI tests are the exception: they live in `shared/src/jvmTest` as `<Feature>ContentTest`, because `runComposeUiTest` renders off-screen on the JVM (no emulator). They drive the stateless `<Feature>Content` with `PreviewTheme` and `PreviewData`, find nodes by the app's own strings via `text(Res.string…)` (`presentation/UiTestStrings.kt`), and assert on the callbacks or actions it sends.

Both `.skills/` files hold the team's established rules (`architecture-linter.md` adds project-specific bans and composition-root exemptions on top).
