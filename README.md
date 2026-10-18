#  Météo — Modern Weather App for Android

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin_100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose_&_Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![API](https://img.shields.io/badge/Data_Provider-Open--Meteo_API-00A8E8?style=for-the-badge)](https://open-meteo.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

**Météo** is a feature-packed native Android weather application crafted with **100% Jetpack Compose** and **Material Design 3**. It delivers hyper-local forecasts, real-time weather analytics, interactive metric charts, and fluid physics-based animations with full offline support.

---

##  Screenshots Showcase

<p align="center">
  <img src="screenshots/cities_screen.jpg" width="31%" alt="Cities Screen" />
  &nbsp;&nbsp;
  <img src="screenshots/forecast_screen.jpg" width="31%" alt="Forecast Screen" />
  &nbsp;&nbsp;
  <img src="screenshots/day_screen.jpg" width="31%" alt="Day Details Screen" />
</p>

<p align="center">
  <i>Left to Right: <b>Cities Overview & Navigation</b> • <b>Interactive Forecast & Charts</b> • <b>Hourly Deep-Dive & Metrics</b></i>
</p>

---

##  Key Features

###  Multi-City Management
- Save and monitor weather conditions across multiple worldwide locations.
- Real-time temperature, condition indicators, high/low daily range preview cards.
- Quick drag-to-dismiss or swipe management.

###  Global Geocoding Search
- Fast location lookup powered by Open-Meteo Geocoding.
- Instant search suggestions with seamless one-tap city additions.

###  Interactive Weather Analytics & Charts
- Dynamic metric charts for **Temperature**, **Precipitation**, **Wind Speed & Direction**, **Humidity**, **Pressure**, and **UV Index**.
- Interactive hour-by-hour slider and data visualization.

###  7-Day & Detailed Daily Forecast
- Comprehensive 7-day outlook with condition breakdown.
- Deep-dive into specific days with granular hourly stats.

###  Material 3 & Fluid Motion Physics
- **Animated Navigation Drawer**: Smooth depth-scaling `ModalNavigationDrawer` with staggered entry transitions and custom spring motion physics.
- Dynamic color themes and adaptive top bars with scroll behaviors.

### ️ Tailored Preferences & Accessibility
- **Unit Systems**: Metric and Imperial toggles (Celsius / Fahrenheit, km/h / mph / m/s / knots, mm / inches, hPa / inHg).
- **Theme Modes**: System Default, Light Theme, Dark Theme.
- **Dynamic Text Scaling**: Small, Normal, and Large typography for enhanced readability.
- **Multilingual**: Native English and French localization.

###  Offline Resilience
- Local persistence using **Room Database** ensures instant data availability even without network connection.

---

## ️ Architecture & Project Structure

Météo follows Google's recommended **Modern Android Architecture** guidelines using the **MVVM** pattern and **Unidirectional Data Flow (UDF)**.

```
com.brahim.meteo
├── component/          # Top-level composable screens & Navigation (AppShell, Cities, Forecast, Day, Search, Settings)
├── config/             # DataStore Module & configuration persistence
├── data/               # Repository, Room Database (MeteoDatabase), and WeatherApi (Retrofit)
│   └── model/          # Data transfer objects & domain models
├── element/            # Reusable UI elements (CityCard, MetricChart, HourlyStrip, SummaryCard, etc.)
├── ui/                 # Application locale, styling, theme, and Motion animation specs
├── util/               # Unit formatting, Date formatting, and Weather condition mappers
└── viewModel/          # Screen-level ViewModels (CitiesVM, ForecastVM, DayVM, SearchVM, SettingsVM, AppConfigVM)
```

---

##  Tech Stack & Libraries

| Category | Technologies / Libraries |
| :--- | :--- |
| **Language** | Kotlin (100%) |
| **UI Framework** | Jetpack Compose, Material Design 3 Components |
| **Architecture** | MVVM, ViewModel, StateFlow, Coroutines |
| **Navigation** | Jetpack Compose Navigation with Animated Transitions |
| **Networking** | Retrofit 2, Kotlinx Serialization, OkHttp |
| **Database & Cache** | Room Persistence Library |
| **Preferences** | Jetpack DataStore Preferences |
| **Asynchronous** | Kotlin Coroutines & Flow |

---

##  Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK Version**: Java 17
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

### Build Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/Weather.git
   cd Weather
   ```

2. **Open in Android Studio**:
   Open Android Studio and choose **Open**, then select the project root folder.

3. **Build the Debug APK**:
   ```bash
   ./gradlew :app:assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

---

##  Testing

The codebase includes automated unit tests covering utility formatters, date parsing, UV index calculations, and weather condition logic:

```bash
# Run unit tests with detailed output
./gradlew :app:testDebugUnitTest --info
```

---

##  License

```text
MIT License

Copyright (c) 2026 Brahim

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```
