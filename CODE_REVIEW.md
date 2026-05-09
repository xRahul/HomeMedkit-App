# Comprehensive Codebase Audit: HomeMedkit (v1.2.0)

## Executive Summary
This document provides a granular analysis of the `in.rahulja.medicinekit` codebase. While the architectural foundation (MVVM + Koin + Room) is sound, there are significant areas for improvement regarding code modularity, sync reliability, and legacy cleanup.

---

## 1. Package: `data` (Persistence Layer)
**Purpose:** Manages Room schema, entities, and DAOs.
- **Files:** `MedicineDatabase.kt`, `dao/*`, `dto/*`, `model/*`, `queries/*`.
- **Findings:**
  - **`MedicineDatabase.kt`:** 
    - *Issue:* Manual migration loops (`db.query` inside `migrate`) are error-prone and slow.
    - *Issue:* `MIGRATION_1_30` is empty and redundant.
  - **`dao/*`:** Generally robust. Use of `@Transaction` and `@RawQuery` is well-implemented.
  - **Suggestion:** Transition away from manual migrations to auto-migrations where possible, especially for schema changes that don't involve complex data restructuring.

## 2. Package: `di` (Dependency Injection)
**Purpose:** Koin configuration.
- **Files:** `KoinModules.kt`.
- **Findings:**
  - *Issue:* The manual "Service Locator" pattern atop Koin is redundant and hides the true dependency graph.
  - *Suggestion:* Refactor into modular Koin modules (Database, Network, ViewModel, Worker) to improve maintainability.

## 3. Package: `models/viewModels` (Presentation Logic)
**Purpose:** MVVM ViewModels and State Management.
- **Files:** `MedicineViewModel.kt`, `IntakesViewModel.kt`, `ScannerViewModel.kt`, `SettingsViewModel.kt`, `AuthViewModel.kt`, `IntakeViewModel.kt`, `MedicinesViewModel.kt`, `MainViewModel.kt`.
- **Findings:**
  - **`MedicineViewModel.kt`:** 
    - *Smell:* Excessively large (25KB). Violates the Single Responsibility Principle.
    - *Violation:* Injects/accepts `Context` in events, hindering unit testing.
  - *Suggestion:* Refactor into smaller UseCases or features-specific components. Remove `Context` dependencies in favor of `androidContext()` or resource-specific helpers.

## 4. Package: `network` (Communication Layer)
**Purpose:** Ktor client, Yandex Disk / Google Drive APIs.
- **Files:** `GoogleDriveApi.kt`, `YandexAuthProvider.kt`, `Network.kt`.
- **Findings:**
  - *Technical Debt:* `GoogleDriveApi.kt` contains largely dead code (commented-out blocks).
  - *Reliability:* Ensure OAuth2/PKCE token refresh logic is robust against token expiration edge cases.

## 5. Package: `ui/navigation` (UI & Navigation)
**Purpose:** Compose screens and custom Navigation 3 implementation.
- **Files:** `AppNavGraph.kt`, `Navigation.kt`, `Screen.kt`, etc.
- **Findings:**
  - *Observation:* The custom multi-backstack management is sophisticated but brittle.
  - *Performance:* Audit `LazyColumn` usage and lambda stability to prevent unnecessary recompositions.

## 6. Package: `worker` (Background Tasks)
**Purpose:** WorkManager tasks (sync, alarms).
- **Files:** `SyncWorker.kt`.
- **Findings:**
  - *Gap:* Synchronization is incomplete; only handles `medicines` and `images`.
  - *Recommendation:* Prioritize extending `SyncWorker` to sync `intakes`, `kits`, and history to fulfill the cloud-backup requirement.

## 7. Package: `receivers` (Broadcast Handling)
**Purpose:** Alarms and OS events.
- **Files:** `AlarmReceiver.kt`, `PreAlarmReceiver.kt`, `AlarmSetter.kt`.
- **Findings:**
  - *Safety:* Critical path. Ensure all logic here is non-blocking to prevent UI jank.

---
## Summary Roadmap
1. **Remove Dead Code:** Prune `GoogleDriveApi.kt` immediately. (Completed ✅)
2. **Modularize DI:** Separate `KoinModules` into domain-specific files. (Completed ✅)
3. **Refactor ViewModels:** Extract business logic from `MedicineViewModel` into testable UseCases. (Completed ✅)
4. **Sync Expansion:** Expand `SyncWorker` coverage. (Completed ✅)
5. **Database Audit:** Simplify migrations and resolve legacy database cruft. (Completed ✅)
