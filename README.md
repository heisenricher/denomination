# Rupee Counter

A professional, high-reliability native Indian Rupee denomination counter application built specifically for Android and optimized for Google Pixel 7a.

[![Download APK](https://img.shields.io/badge/Download-RupeeCounter.apk%20(v1.0.0)-brightgreen?style=for-the-badge&logo=android)](https://github.com/heisenricher/denomination/releases/download/v1.0.0/RupeeCounter.apk)

![Rupee Counter](https://img.shields.io/badge/Platform-Android-green)
![API](https://img.shields.io/badge/Min%20SDK-26%20%7C%20Target%20SDK-35-blue)
![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-emerald)
![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-success)

---

## 📲 Ready-to-Install APK Download

You can download and install the app directly on your Android phone (Pixel 7a):

👉 **[Download RupeeCounter.apk directly (v1.0.0)](https://github.com/heisenricher/denomination/releases/download/v1.0.0/RupeeCounter.apk)** (15.1 MB)

*Release page:* [GitHub Releases v1.0.0](https://github.com/heisenricher/denomination/releases/tag/v1.0.0)

### Quick Install Steps:
1. Tap the **[Download RupeeCounter.apk](https://github.com/heisenricher/denomination/releases/download/v1.0.0/RupeeCounter.apk)** link.
2. In your mobile browser, tap **Download anyway** if prompted.
3. Once downloaded, tap the notification or open **Downloads** in the **Files** app.
4. Tap **RupeeCounter.apk** and choose **Install**.
5. Launch **Rupee Counter** and start counting notes and coins!

---

## 🌟 Key Features

### 1. Complete Indian Rupee Denominations
- **Banknotes**: ₹500, ₹200, ₹100, ₹50, ₹20, ₹10
- **Coins**: ₹20, ₹10, ₹5, ₹2, ₹1
- **Strict Distinction**: ₹20 notes and ₹20 coins are kept strictly separate. The same applies to ₹10 notes and ₹10 coins.
- **Visually Separated Sections**: Notes and Coins are displayed in dedicated, clear sections with individual and section-level subtotals.

### 2. Live Calculation & Indian Currency Formatting
- Real-time automatic recalculation of denomination subtotals, grand total, and total piece counts without requiring manual "Calculate" buttons.
- Authentic Indian numbering convention grouping: e.g. `₹5,000`, `₹1,00,000`, `₹12,50,000`, `₹1,23,45,678`.
- Automatic "Amount in Words" conversion (e.g., *One Lakh Twenty-Three Thousand Four Hundred Fifty Rupees Only*).
- Safe integer arithmetic (`Long` with strict bounds and overflow protection). Floating-point arithmetic is never used for currency.

### 3. Automatic Session Restoration (Highest Reliability)
- Active denomination counts and subtotals are persisted automatically to a local SQLite/Room database as edits happen.
- When the app is closed, force-stopped, killed by Android OS process management, or through a full device reboot, reopening the app immediately restores the exact state where you left off.
- Configuration changes (device rotation, dark mode toggling) and background-to-foreground transitions do not reset or lose counts.

### 4. Saved Historical Records
- One-tap **Save Record** action with debounce and double-tap prevention.
- Optional title/remarks for records (e.g., "Cash Drawer Close", "Bank Deposit").
- Dedicated **Saved Records** screen displaying past counting sessions ordered by timestamp.
- Full breakdown inspection dialog showing every note/coin count, subtotal, grand total, and piece count.
- Safe record deletion with confirmation dialog.
- Saving a record **never** resets or alters your active working session.

### 5. Safe Reset / Clear
- Prominent Reset action with a confirmation dialog to prevent accidental data loss.
- Resets counts to zero and updates active session storage while leaving historical records untouched.

### 6. Offline PDF Report Generation
- Clean, professional A4 PDF export generated 100% locally via native `android.graphics.pdf.PdfDocument`.
- Displays Rupee Counter branding, timestamp, grand total, piece breakdown, amount in words, and itemized tables for notes and coins.
- Can export both the active counting session and past historical records.

### 7. Android Sharesheet & WhatsApp Sharing
- Share generated PDF reports directly using Android's native Sharesheet and secure `FileProvider` content URIs.
- Option to share a clean, formatted plain-text summary.
- WhatsApp is immediately available when installed, with graceful handling and zero crashes if cancelled or if WhatsApp is absent.

### 8. 100% Offline & Private
- Zero network permissions (`android.permission.INTERNET` is completely absent from the manifest).
- No Firebase, no analytics, no external servers, no tracking, and no cloud leaks (`allowBackup="false"`).
- Operates flawlessly in Airplane mode.

---

## 🏗️ Architecture & Technology Stack

- **Language**: Kotlin 1.9.24
- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Theme**: Pixel 7a Material You Dynamic Theming support with elegant emerald/currency palette fallback, supporting both Light and Dark themes
- **Architecture**: Clean Architecture + MVVM (Model-View-ViewModel) + Repository Pattern
- **State Management**: Kotlin Coroutines & `StateFlow`
- **Local Persistence**: AndroidX Room Database 2.6.1 (SQLite) with separate tables for active session and historical records
- **Edge-to-Edge**: `enableEdgeToEdge()` with full status bar and navigation bar insets support
- **Build System**: Gradle 8.7 (Kotlin DSL)

---

## 📁 Project Structure

```
RupeeCounter/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/rupeecounter/app/
│   │   │   │   ├── RupeeCounterApplication.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── ActiveSessionDao.kt
│   │   │   │   │   │   ├── ActiveSessionEntity.kt
│   │   │   │   │   │   ├── SavedRecordDao.kt
│   │   │   │   │   │   ├── SavedRecordEntity.kt
│   │   │   │   │   │   └── RupeeCounterDatabase.kt
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── CalculationResult.kt
│   │   │   │   │   │   └── Denomination.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── CounterRepository.kt
│   │   │   │   │       └── CounterRepositoryImpl.kt
│   │   │   │   ├── domain/usecase/
│   │   │   │   │   ├── CalculateTotalUseCase.kt
│   │   │   │   │   ├── IndianCurrencyFormatter.kt
│   │   │   │   │   └── NumberToWordsConverter.kt
│   │   │   │   ├── export/
│   │   │   │   │   ├── PdfReportGenerator.kt
│   │   │   │   │   └── ShareHelper.kt
│   │   │   │   └── ui/
│   │   │   │       ├── MainActivity.kt
│   │   │   │       ├── theme/
│   │   │   │       │   ├── Color.kt
│   │   │   │       │   ├── Theme.kt
│   │   │   │       │   └── Type.kt
│   │   │   │       ├── counter/
│   │   │   │       │   ├── CounterScreen.kt
│   │   │   │       │   ├── CounterUiState.kt
│   │   │   │       │   ├── CounterViewModel.kt
│   │   │   │       │   └── components/
│   │   │   │       │       ├── DenominationRow.kt
│   │   │   │       │       ├── DenominationSection.kt
│   │   │   │       │       ├── GrandTotalCard.kt
│   │   │   │       │       ├── ResetConfirmationDialog.kt
│   │   │   │       │       └── SaveRecordDialog.kt
│   │   │   │       └── records/
│   │   │   │           ├── RecordsUiState.kt
│   │   │   │           ├── RecordsViewModel.kt
│   │   │   │           ├── SavedRecordsScreen.kt
│   │   │   │           └── components/
│   │   │   │               ├── DeleteConfirmationDialog.kt
│   │   │   │               └── RecordDetailDialog.kt
│   │   │   └── res/
│   │   └── test/
│   │       └── java/com/rupeecounter/app/
│   │           ├── data/repository/CounterRepositoryTest.kt
│   │           ├── domain/usecase/DenominationCalculationTest.kt
│   │           ├── domain/usecase/IndianCurrencyFormatterTest.kt
│   │           ├── domain/usecase/NumberToWordsConverterTest.kt
│   │           └── ui/counter/CounterViewModelTest.kt
│   └── build.gradle.kts
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

---

## 🚀 How to Build & Run

### Prerequisites
- JDK 17 or JDK 21 (e.g. Android Studio JBR)
- Android SDK with Platforms 34/35 and Build Tools 34.0.0+

### Command Line Build
```powershell
# Set Java & Android SDK paths
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\Srira\AppData\Local\Android\Sdk"

# Run unit tests
.\gradlew testDebugUnitTest

# Build debug APK
.\gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
