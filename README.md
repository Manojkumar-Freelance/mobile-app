# App Addiction Controller

An Android application designed to help users reduce social media addiction by tracking app usage, sending warnings, and blocking access when limits are exceeded.

## Features

- ✅ **App Selection**: Choose specific apps to monitor (Instagram, Facebook, TikTok, etc.)
- ✅ **Time Limits**: Set daily usage limits (15 minutes to 8 hours)
- ✅ **Real-time Tracking**: Monitor app usage in real-time using UsageStats API
- ✅ **Warning System**: Receive 3 warnings during continuous 3-hour usage
- ✅ **Auto-blocking**: Apps are blocked for 1 hour after ignoring all warnings
- ✅ **Offline Support**: All data stored locally using Room Database
- ✅ **Background Service**: Runs efficiently as a foreground service

## Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Permissions Required**:
  - Usage Access (to track app usage)
  - Display Over Other Apps (to show blocking screen)
  - Post Notifications (for warnings)

## How It Works

1. **Select Apps**: Choose which apps you want to monitor
2. **Set Limits**: Configure daily time limits for each app
3. **Start Service**: Enable the monitoring service
4. **Get Warnings**: Receive notifications when approaching limits
5. **Auto-block**: Apps are blocked after exceeding limits or ignoring warnings

### Continuous Usage Logic

- If you use an app continuously for **3 hours**, the system sends warnings
- **3 warnings** are sent at 1-hour intervals (at 3h, 4h, and 5h marks)
- If you ignore all warnings, the app is **blocked for 1 hour**
- After the block period, you can use the app normally again

### Daily Limit Logic

- Set a daily time limit (e.g., 60 minutes)
- When you reach the limit, the app is blocked for 1 hour
- Usage resets at midnight

## Project Structure

```
app/
├── data/                       # Data layer
│   ├── AppSelection.kt         # Room entity
│   ├── AppSelectionDao.kt      # Database access object
│   ├── AppDatabase.kt          # Room database
│   └── AppRepository.kt        # Repository pattern
├── service/                    # Background services
│   └── MonitoringService.kt    # Foreground service for monitoring
├── ui/                         # UI components
│   ├── MainActivity.kt         # Main screen
│   ├── BlockActivity.kt        # Blocking screen
│   └── AppAdapter.kt           # RecyclerView adapter
└── utils/                      # Utilities
    ├── NotificationHelper.kt   # Notification management
    └── UsageStatsHelper.kt     # Usage tracking utilities
```

## Building the Project

### Prerequisites

- Android Studio Hedgehog or later
- JDK 8 or higher
- Android SDK with API 34

### Steps

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project: `Build > Make Project`
4. Run on device/emulator: `Run > Run 'app'`

### Command Line Build

```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

## Installation

1. Install the APK on your Android device
2. Grant **Usage Access** permission
3. Grant **Display Over Other Apps** permission
4. Select apps to monitor and set time limits
5. Start the monitoring service

## Testing

Since this app requires real device permissions and background services, manual testing is recommended:

1. **Permission Testing**: Verify all permissions are granted correctly
2. **Usage Tracking**: Use a monitored app and check if usage is tracked
3. **Warning System**: Test continuous usage for 3+ hours
4. **Blocking**: Verify blocking screen appears when limits are exceeded
5. **Service Persistence**: Ensure service runs in background

## Known Limitations

- Requires Android 8.0 or higher
- May be affected by aggressive battery optimization
- Cannot block system apps
- Requires manual permission grants

## Future Enhancements

- [ ] Statistics and usage graphs
- [ ] Weekly/monthly reports
- [ ] Custom warning schedules
- [ ] App categories (Social Media, Games, etc.)
- [ ] Export usage data

## License

This project is created for educational purposes.
