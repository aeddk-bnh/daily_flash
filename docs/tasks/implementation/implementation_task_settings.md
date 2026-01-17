# Task: Implement Settings & Notifications Module

## Role
Implementation Agent

## Authority
You own the code within `com.dailyflash.core.notification`, `com.dailyflash.core.settings`, `com.dailyflash.domain.settings`, `com.dailyflash.presentation.settings`.

## Context
We are implementing the Settings requirements (User Preferences) and Daily Notifications.
- **Data Model:** `UserSettings` (Scaffolded)
- **Interfaces:** `ISettingsRepository`, `INotificationManager` (Scaffolded)

## Task Objective
1.  Implement `SettingsDataStore` (Core) to persist `UserSettings`.
2.  Implement `SettingsRepositoryImpl` (Data) to bridge Domain and Core.
3.  Implement `DailyNotificationManager` (Core) using `AlarmManager`.
4.  Implement `SettingsViewModel` and `SettingsDialog` (UI).
5.  Implement Domain UseCases: `GetUserSettingsUseCase`, `UpdateReminderUseCase`.

## File Permissions
- `app/src/main/java/com/dailyflash/core/settings/*`
- `app/src/main/java/com/dailyflash/core/notification/*`
- `app/src/main/java/com/dailyflash/data/SettingsRepositoryImpl.kt` [NEW]
- `app/src/main/java/com/dailyflash/domain/settings/*`
- `app/src/main/java/com/dailyflash/presentation/settings/*`
- `app/src/main/java/com/dailyflash/presentation/navigation/NavGraph.kt` (To add Settings Route)

## Dependencies
- **DataStore:** `androidx.datastore:datastore-preferences`
- **Accompanist Permissions:** For Notification Permission (Android 13+).
- **WorkManager/AlarmManager:** Android SDK.

## TODO Map
- [ ] Implement `SettingsManager` in `core/settings` using DataStore.
- [ ] Implement `DailyNotificationManager` in `core/notification`.
- [ ] Implement `SettingsRepositoryImpl` in `data`.
- [ ] Create `SettingsDialog` composable.

## Definition of Done
- User can toggle Daily Reminders.
- User can set a custom time for reminders.
- "Streaks" are tracked (stubbed logic for now, or hooked into SaveVideo).
- App compiles and runs without crashes.
