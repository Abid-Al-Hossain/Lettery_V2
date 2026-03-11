# Letterly - Smart Word Suggestion App

## Overview
Letterly is a powerful Android dictionary and word-finding tool designed to help users discover words based on specific patterns and constraints. Whether you're solving puzzles, expanding your vocabulary, or looking for writing inspiration, Letterly provides exhaustive, real-time suggestions powered by the cloud.

## Features
- **Dynamic Word Scaling**: Add or remove letter boxes to match any word length.
- **Precision Filtering**:
  - **Inclusions**: Define specific letters that *must* appear at certain positions (supports multiple options like `a,e,i`).
  - **Exclusions**: Blacklist letters to ensure they *never* appear in specific positions.
- **Cloud-Powered Engine**: Leverages the Datamuse API to provide an exhaustive and up-to-date vocabulary.
- **Meanings & Definitions**: Instantly view definitions for suggested words, cleaned of technical part-of-speech tags for readability.
- **Real-Time Input Validation**: Built-in formatting checks to ensure your search patterns are always valid.

## 🚀 Getting Started

### Prerequisites
- [Android Studio Arctic Fox](https://developer.android.com/studio) or newer.
- Android SDK 34+.
- Active Internet connection (for real-time word lookups).

### Installation & Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Abid-Al-Hossain/Lettery_V2.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio and select **File > Open**.
   - Navigate to the cloned folder and click **OK**.
3. **Sync & Build**:
   - Let the Gradle sync complete. If prompted, click **"Sync Project with Gradle Files"**.
4. **Run**:
   - Connect your device or start an emulator and click the green **Run** button.

## 📊 Project Presentation
Detailed information about the app's design and purpose can be found in the presentation file:
[Download Presentation (Final.pptx)](./App_Presentation_Media/Final.pptx)

## Technical Architecture
- **Language**: Kotlin
- **Network**: Retrofit 2 + GSON for efficient API communication.
- **Concurrency**: Kotlin Coroutines & Lifecycle scoping for smooth, non-blocking UI.
- **UI Components**: Material Design & RecyclerView for a fluid user experience.

## Authors
- **Abid Al Hossain Swakkhar**
- **Denesh Barua Pantho**
- **Fahad Bin Aziz Nabil**

## License
This project is licensed under the [MIT License](LICENSE).
