# Letterly V2 - Smart Word Suggestion App

## Overview
Letterly V2 is an enhanced version of the original word-finding utility. It has been refactored to replace massive local dictionary files with a high-performance integration of the **Datamuse API**, making the app faster, significantly smaller, and more exhaustive in its search capabilities.

## ✨ New in V2
- **Cloud-Powered Search**: Integrates the Datamuse API for real-time, exhaustive word lookups.
- **Memory Efficient**: Removed the 8.9MB local JSON dictionary, reducing the APK size and memory overhead.
- **Improved Coverage**: Uses 26 parallel API calls to ensure valid matches across the entire alphabet for any word length.
- **Live Definitions**: Modern, up-to-date word definitions fetched directly from the cloud.

## Features
- **Dynamic Box Management**: Add or remove letter boxes based on the desired word size.
- **Custom Letter Constraints**:
  - **Inclusions**: Match specific letters at specific positions (supports multiple letters e.g. `a,e`).
  - **Exclusions**: Blacklist forbidden letters for any position.
- **Real-Time Validation**: Alerts for incorrect inputs and formatting issues.
- **Alphabetical Sorting**: Results are presented in an easy-to-read uppercase, sorted list.

## 🚀 Getting Started

### Prerequisites
- [Android Studio Arctic Fox](https://developer.android.com/studio) or newer.
- Android SDK 34+.
- Internet connection (for API-based search).

### Installation & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Abid-Al-Hossain/Lettery_V2.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **File > Open** and navigate to the cloned directory.
3. **Sync Gradle**:
   - Wait for the project to finish syncing. If prompted, click **"Sync Project with Gradle Files"**.
4. **Run the App**:
   - Connect an Android device or start an emulator.
   - Click the green **Run** button in the toolbar.

## 📊 Presentation
You can find the project presentation file here:
[Project Presentation (Final.pptx)](./App_Presentation_Media/Final.pptx)

## Technical Stack
- **Language**: Kotlin
- **Networking**: Retrofit 2 + GSON
- **Concurrecy**: Kotlin Coroutines + lifecycleScope
- **Architecture**: Android Views + RecyclerView

## Contributions
We welcome contributions!
1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a pull request.

## License
Licensed under the [MIT License](LICENSE).

## Authors
- **Abid Al Hossain Swakkhar**
- **Denesh Barua Pantho**
- **Fahad Bin Aziz Nabil**
