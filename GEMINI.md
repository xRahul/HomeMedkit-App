# HomeMedkit Project Instructions

This project is a modern Android application for managing home medicine kits, tracking inventory, and scheduling intakes.

## Technology Stack

- **UI:** Jetpack Compose with Material 3 Expressive APIs.
- **Language:** Kotlin.
- **Persistence:** Room Database (Version 35) with SQLite FTS4 support.
- **Networking:** Ktor client (supports crpt.ru API and Yandex Disk via OAuth2/PKCE).
- **Navigation:** Android Navigation 3.
- **Architecture:** MVVM (Model-View-ViewModel) with manual Dependency Injection (`AppModule`).
- **Background Tasks:** WorkManager for intake alarms and sync.
- **Image Handling:** Coil 3.
- **Scanning:** ZXing-cpp.

## Architecture & Conventions

- **Manual DI:** Managed via `ru.application.homemedkit.utils.di.AppModule`. Use the top-level getters (`Database`, `Preferences`, `AlarmManager`, `WorkManager`) for injection.
- **Navigation:** Use the `Screen` sealed class in `ru.application.homemedkit.ui.navigation` for type-safe routing.
- **Localization:** Support for multiple languages via `res/values-*/strings.xml` and Fastlane metadata.
- **State Management:** Use `StateFlow` and `collectAsStateWithLifecycle` in Compose screens.
- **Database Migrations:** Manual migrations are preferred for complex schema changes; check `MedicineDatabase.kt` for historical examples.

## Key Packages

- `ru.application.homemedkit.data`: Room entities, DAOs, and database configuration.
- `ru.application.homemedkit.models.viewModels`: Screen-level logic.
- `ru.application.homemedkit.ui.screens`: Compose screen implementations.
- `ru.application.homemedkit.network`: API clients and authentication logic.
