# ECHO OS — Android Console & Handheld Launcher

<p align="center">
  <img src="app/src/main/res/drawable/ic_echo_logo.xml" width="100" height="100" alt="ECHO OS Logo" />
</p>

<p align="center">
  <b>A sleek, console-native Android Home Launcher built with Jetpack Compose & Kotlin.</b><br>
  Engineered specifically for handheld gaming consoles, retro devices, and landscape Android displays.
</p>

---

## Highlights

- **Pure Console Immersion:** Zero-touch controller navigation with tactile audio feedback on every button press and D-pad move.
- **Custom BIOS Boot Sequence:** Cybernetic startup telemetry and animated holographic core on device launch.
- **Dynamic Ambient Glow:** Real-time radial canvas background that smoothly interpolates and shifts color based on the selected game's extracted palette.
- **Console Horizontal Carousel:** Center horizontal `LazyRow` with specular glass highlight tiles, active focus zoom (`1.12f`), electric cyan glowing outline, and instant auto-centering.
- **Dual Hero Actions:** Primary pill `[ ▶ Play ]` button alongside secondary `[ ⋯ Options ]` sheet for app info, quick settings, and direct launches.
- **Authentic Controller Legend Bar:** Bottom HUD featuring action badges `(A) Play`, `(X) All Apps`, `(Y) Options`, `(L1)/(R1) Tabs`.
- **System Dock:** Quick-access dock for All Apps drawer, ECHO Updates, Wi-Fi & Bluetooth, and Android Settings.
- **Handheld Gamepad Compatibility:** Native analog thumbstick, D-pad, and HAT axis support with configurable confirm button layouts (South Confirm vs. East Confirm).
- **True System Home Screen:** Configured with `CATEGORY_HOME` and `CATEGORY_DEFAULT` with locked landscape orientation and transient system bar hiding. Back button never exits home screen.

---

## Controller & Keyboard Mappings

| Input | Controller Button | Keyboard Equivalent | Action |
|---|---|---|---|
| **Navigate** | `D-PAD LEFT` / `RIGHT` | Arrow Left / Right | Scroll game cards or dock actions |
| **Zone Focus** | `D-PAD UP` / `DOWN` | Arrow Up / Down | Move between Top Tabs, Carousel & Dock |
| **Select / Launch** | `BUTTON A` / `DPAD_CENTER` | `Enter` / `Space` | Launch highlighted game or dock action |
| **Options Sheet** | `BUTTON Y` | `Y` | Open game options & Android app info |
| **All Apps Drawer**| `BUTTON X` | `X` | Toggle full-screen all apps grid |
| **Switch Tabs** | `BUTTON L1` / `R1` | `Q` / `E` or `PgUp` / `PgDn` | Switch between Games and Media tabs |
| **Dismiss / Back** | `BUTTON B` | `Esc` / `Backspace` | Close active overlay sheet or modal |

---

## Architecture & Tech Stack

- **UI Toolkit:** Jetpack Compose (Material3 + Custom Hardware Shaders & Canvas)
- **Language:** Kotlin 2.2+
- **Platform:** Android 8.0+ (API 26+) — targeted for Android 14 / 15 / 16
- **Architecture:** Clean component architecture with reactive state flows, custom focus management, and hardware key/axis event interceptors.
- **Build System:** Gradle 9.4 with Android Gradle Plugin 9.2

---

## Building from Source

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (API 34+)

### Build APK
Clone the repository and run:

```bash
# On Linux / macOS:
./gradlew assembleDebug

# On Windows (PowerShell):
.\gradlew.bat assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install on Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Setting ECHO OS as Default Launcher
1. Install the APK onto your Android console or handheld.
2. Press the physical **Home** button on your device.
3. Android will prompt: *"Select a home app"*.
4. Choose **ECHO OS** and select **Always**.

---

## License
MIT License. Open source for all console and handheld enthusiasts.
