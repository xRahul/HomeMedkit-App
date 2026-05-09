# MedicineKit Comprehensive Technical Guide

This guide provides an itemized breakdown of the core complex features within the MedicineKit application.

## 1. AI-Powered Medicine Parsing (The Pipeline)
The app automates data entry by converting physical medicine packaging into structured JSON data.

- **Trigger**: User initiates the "Scan/Add by Image" feature from the UI (`MedicineDialogs.kt`).
- **Capture**: The `CameraPreview` component captures an image, which is processed in `MedicineViewModel`.
- **Logic**:
    1. **Local OCR (ML Kit)**: The `AiMedicineParser` first uses on-device ML Kit to extract raw text from the image.
    2. **Remote AI Parsing (Gemini)**: If initial text is found, it sends the image and text to Gemini 1.5 Flash with a structured prompt.
    3. **JSON Deserialization**: The Gemini response is parsed into an `AiMedicineResult` object.
- **Completion**: The `MedicineViewModel` collects this result, prompts the user for verification, and saves the verified fields into the `Medicine` entity via the `MedicineDAO`.

## 2. Alarm & Intake Scheduling System
The app ensures timely medication compliance using a dual-stage notification mechanism.

- **Pre-Alarm Logic (30 mins before)**:
    - `AlarmSetter.kt` schedules a `PreAlarmReceiver` exactly 30 minutes before the scheduled time.
    - `PreAlarmReceiver.kt` wakes the system, creates a "pending" entry in the `IntakeTaken` table (to mark the intake as "in-progress"), and triggers the final `AlarmReceiver`.
- **Final Notification**:
    - `AlarmReceiver.kt` handles the actual system notification that alerts the user to take their medicine.
- **Sync**: Both receivers are managed by the system `AlarmManager`, ensuring they function correctly even when the app is in the background or terminated.

## 3. Data Synchronization (`SyncWorker`)
The app utilizes `WorkManager` for asynchronous data synchronization with cloud services (Yandex Disk).

- **Scope**: The current implementation synchronizes the `medicines` table and associated images. Note: History and Kits are NOT part of this sync.
- **Workflow**:
    - `SyncWorker.kt` runs as a periodic background task.
    - **Serialization**: Converts the `medicines` database table into a `medicines.json` file.
    - **Conflict Resolution**: It calculates the MD5 hash of local vs. remote versions and compares modification timestamps to decide whether to upload or download.
    - **Image Sync**: Iterates through the `images/` directory, comparing MD5 hashes with remote images to perform an incremental sync.

## 4. Manual Backup & Restore (`DataManager`)
Provides a manual method for users to migrate their entire application data across devices.

- **Export**: `DataManager.kt` zips the active SQLite database files and the `images/` folder into a single archive file.
- **Import**:
    - **Validation**: Upon restoration, it cross-references the Room `identity_hash` to ensure the backup schema matches the current app's database schema.
    - **Cleanup**: It clears existing data and cancels all pending alarms to prevent duplication or stale notifications.
    - **Restart**: Triggers an application-wide restart to refresh all Koin-injected singletons with the newly imported database state.
