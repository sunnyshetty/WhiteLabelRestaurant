---
name: architecture-linter
description: Check that changes in `shared/` respect the Clean Architecture layer boundaries of the WhiteLabelRestaurant engine. Run before committing any change under shared/src.
---

> **DRAFT — replace with the team's previously established rules.** These defaults were written
> from the project's package layout; the original rule set was not available when this file was created.

# Architecture Linter

Root package: `com.skyhightechnology.restaurantengine` (all paths below are relative to
`shared/src/commonMain/kotlin/com/skyhightechnology/restaurantengine/`).

## Layers

| Package | Holds | May depend on |
|---|---|---|
| `domain/model` | Immutable business entities (`data class`, `sealed interface`) | Kotlin stdlib, kotlinx-datetime only |
| `domain/repository` | Repository **interfaces** | `domain/model` |
| `domain/usecase` | One use case per class, single `operator fun invoke` | `domain/model`, `domain/repository` |
| `data/model` | DTOs (`@Serializable`), Firestore documents | kotlinx.serialization, GitLive Firebase |
| `data/repository` | Repository **implementations** + DTO↔domain mappers | `domain/*`, `data/model`, Firebase, serialization |
| `presentation/component` | Stateless reusable composables | `domain/model`, `presentation/theme` |
| `presentation/theme` | Colors, typography, tenant branding → `MaterialTheme` | Compose only |
| `presentation/<feature>` | Screen + `ViewModel` + `UiState` | `domain/*`, `presentation/component`, `presentation/theme` |
| `di` | Koin modules — the only place that wires `data` implementations to `domain` interfaces | everything |

## Rules (each violation is an error)

1. **Domain is pure.** No file in `domain/` imports `androidx.*`, `org.jetbrains.compose.*`,
   `dev.gitlive.*`, `kotlinx.serialization.*`, `io.ktor.*`, `org.koin.*`, or any `data`/`presentation` package.
2. **Presentation never touches data.** No file in `presentation/` imports `…restaurantengine.data.*`
   or `dev.gitlive.*`. ViewModels depend on use cases (or repository interfaces), never on implementations.
3. **DTOs stay in data.** `@Serializable` classes live only in `data/model`; they never cross into
   `domain` or `presentation` — map them in `data/repository`.
4. **No platform code in commonMain.** Platform behaviour goes through `expect`/`actual`
   (see `getApplicationId()`); `commonMain` must not import `android.*`, `platform.*`, or `java.*`.
5. **Tenant identity comes from one place.** Only `getApplicationId()` (via Koin's `TenantId`
   qualifier) decides the tenant. No hard-coded brand IDs, colors, or names outside `di`/tests.
6. **DI wiring lives in `di`.** Classes take dependencies through constructors; `koinInject()`/`koinViewModel()`
   are allowed only at screen entry points, never inside `domain` or `data`.
7. **State is exposed as `StateFlow<UiState>`.** ViewModels extend `androidx.lifecycle.ViewModel`,
   expose read-only `StateFlow`, and keep `MutableStateFlow` private.

## How to check

```bash
cd shared/src/commonMain/kotlin/com/skyhightechnology/restaurantengine
grep -rnE 'import (androidx|org\.jetbrains\.compose|dev\.gitlive|kotlinx\.serialization|org\.koin)|restaurantengine\.(data|presentation)' domain/   # rule 1
grep -rnE 'import (com\.skyhightechnology\.restaurantengine\.data|dev\.gitlive)' presentation/                                                 # rule 2
grep -rln '@Serializable' . | grep -v '^\./data/model/'                                                                                        # rule 3
grep -rnE '^import (android|platform|java)\.' .                                                                                                 # rule 4
```

Any output is a violation: report file:line, the rule broken, and the fix (move the type, add a
mapper, or introduce an interface in `domain/repository`).
