# Phone Analyzer

A modern Flutter application that provides detailed hardware and software information about Android devices. Features a beautiful UI with gradient cards, multi-language support (9 languages), and comprehensive device analytics.

## 🛠️ Tech Stack

- **Framework**: Flutter 3.5.0+ (Dart)
- **Platform**: Android (ARM64, API 23+)
- **Architecture**: MethodChannel for native Android integration
- **State Management**: StatefulWidget with reactive UI updates
- **Localization**: flutter_localizations with 9 language support
- **Permissions**: permission_handler package for runtime permission requests
- **Native Android**: Kotlin with Camera2 API, SensorManager, ConnectivityManager
- **Storage**: SharedPreferences for app settings
- **UI**: Material Design 3 with custom gradient cards and animations

## ✨ Features

### Device Information Categories
- **General Info**: Device model, brand, Android version, RAM, storage
- **Display**: Refresh rate, HDR support, Always-on Display, screen technology
- **Performance**: CPU cores, architecture, GPU model, clock speeds
- **Camera**: Camera count, resolution, autofocus, hardware level
- **Storage**: Total, used, and available storage space
- **Network**: Connection type, IP address, WiFi details, MAC addresses
- **Sensors**: Fingerprint, face unlock, accelerometer, gyroscope, barometer, and more
- **Battery**: Technology, temperature, voltage, wireless charging support

### UI/UX Highlights
- Modern gradient cards with smooth animations
- RTL support for Arabic language
- Pull-to-refresh functionality
- Category-based grid navigation
- Progress indicators for system status
- Dark/Light theme support

### Supported Languages
🇸🇦 Arabic | 🇨🇳 Chinese | 🇩🇪 German | 🇬🇧 English | 🇪🇸 Spanish | 🇮🇳 Hindi | 🇧🇷 Portuguese | 🇷🇺 Russian | 🇹🇷 Turkish

## 🚀 Getting Started

### Prerequisites
- Flutter SDK ^3.5.0
- Android Studio / VS Code with Flutter extension
- Android device or emulator (API 23+)

### Installation

```bash
# Clone the repository
git clone https://github.com/AhmedMesud/phone_analyzer.git

# Navigate to project
cd phone_analyzer

# Install dependencies
flutter pub get

# Run on device/emulator
flutter run
```

### Build Release APK

```bash
# Build for ARM64 devices (most modern phones)
flutter build apk --release --target-platform=android-arm64

# Or build split APKs for all architectures
flutter build apk --release --split-per-abi
```

## 📱 Screenshots

*(Screenshots will be added here)*

## 🔒 Permissions

The app requires the following permissions to function properly:

- **Camera**: For camera information and count
- **Phone State**: For IMEI and network type (Android 10+ limited)
- **Location**: For detailed WiFi connection info
- **Bluetooth**: For Bluetooth MAC address and device name
- **NFC**: For NFC availability detection

All permissions are requested at runtime on Android 6.0+ using the permission_handler package.

## 📝 Notes

- Some hardware information may be limited on emulators
- IMEI access is restricted on Android 10+ due to Google security policies
- MAC addresses are hidden on Android 6.0+ for privacy (shows "Hidden (Security)")
- GPU model detection is based on hardware board identification

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.

---

Built with ❤️ using Flutter
