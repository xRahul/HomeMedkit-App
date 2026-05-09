# MedicineKit Exhaustive Technical Specification

## 1. Executive Summary
MedicineKit is a reactive Android application utilizing Jetpack Compose for the UI layer, Room (SQLite) for high-performance persistent storage, and WorkManager/AlarmManager for intelligent background task management. It employs a clean MVVM architecture, where ViewModels serve as the single source of truth for UI state, driving reactive updates across the application.

## 2. Package-Level Technical Breakdown

### 2.1 Persistence Layer (`in.rahulja.medicinekit.data`)
- **Purpose**: Defines the data schema, storage entities, and access patterns (CRUD).
- **Key Components**:
    - `MedicineDatabase`: The Room database entry point; defines migration logic and accessors for all DAOs.
    - `DAOs (Data Access Objects)`: Interfaces (e.g., `MedicineDAO`, `AlarmDAO`) providing type-safe SQL queries. Supports both asynchronous (suspend) operations and reactive stream processing (Flow).
    - **Data Models**: Domain models and DTOs (Data Transfer Objects) that map directly to the SQLite schema, facilitating complex joins (e.g., `IntakeFull`, `MedicineMain`) to support rich UI views.
- **Data Flow**: Entities -> DAO -> ViewModels (using Flow/Suspend).

### 2.2 Domain & Logic Layer (`in.rahulja.medicinekit.models`)
- **Purpose**: Encapsulates business logic, state management, and interaction handling.
- **Key Components**:
    - `viewModels`: The core processing hubs. Each screen has a corresponding ViewModel (e.g., `MedicineViewModel`) that consumes user events and exposes `StateFlow` to the Compose UI.
    - `states`: Data classes representing the UI state at any given moment.
    - `events`: Sealed classes defining valid user/system actions, keeping business logic centralized and testable.
- **Feature Mapping**: Central to every interactive feature, from inventory management to intake logging.

### 2.3 User Interface (`in.rahulja.medicinekit.ui`)
- **Purpose**: Declaration of the visual tree using Jetpack Compose and Material 3.
- **Key Components**:
    - `screens`: Top-level composition functions mapped to specific application routes.
    - `elements`: Reusable design components (buttons, text fields, custom scaffolds).
    - `navigation`: Type-safe routing using a sealed `Screen` class, managing the application's backstack and transitions via `AppNavGraph.kt`.
- **System Logic**: Collects state from ViewModels via `collectAsStateWithLifecycle` to ensure efficient UI updates synchronized with the Android lifecycle.

### 2.4 Scheduling & Background Processing (`in.rahulja.medicinekit.receivers` & `worker`)
- **Purpose**: Manages time-sensitive user notifications and heavy, non-time-critical background synchronization.
- **System Architecture**:
    - `AlarmManager` (via `AlarmSetter`): Used for precise, time-triggered intake reminders.
    - `BroadcastReceivers` (`AlarmReceiver`, `PreAlarmReceiver`): Handle system-level callbacks to display notifications or trigger application logic, even when the app is in the background.
    - `WorkManager` (`SyncWorker`): Handles data persistence to external clouds (Google Drive/Yandex Disk). It runs periodically to ensure the application state remains synchronized.

### 2.5 Utilities & Integration (`in.rahulja.medicinekit.utils`)
- **Purpose**: Cross-cutting concerns and external API/Library integrations.
- **Key Integrations**:
    - `AiMedicineParser`: Leverages ML Kit (local) and Gemini 1.5 Flash (cloud) for intelligent text recognition and structural parsing of medicine packaging.
    - `DataManager`: Handles backup/restore logic, file management, and database export.
    - `di`: Koin dependency injection configuration, ensuring singleton scoping for core services like Database and Preferences.

## 3. Core Technical Data Flow
The application maintains data consistency through a unidirectional flow:
1. **User Action**: Triggered in the UI (e.g., `MedicinesScreen`).
2. **Event Dispatch**: Dispatched as an event to the `ViewModel` (e.g., `MedicineEvent`).
3. **Domain Logic**: ViewModel modifies state or triggers DAO/Network calls.
4. **Data Sync**: DB/DAO updates, triggering a new emission in the `StateFlow`.
5. **UI Update**: Compose UI observes the new state and recomposes.
