# Letterly - Smart Word Suggestion App

## Overview
Letterly is a powerful Android dictionary and word-finding tool designed to help users discover words based on specific patterns and constraints. Whether you're solving puzzles, expanding your vocabulary, or looking for writing inspiration, Letterly provides exhaustive, real-time suggestions powered by the cloud.

## Features
- **4 Search Modes**:
  - **🔠 Pattern Mode**: Discover words matching specific patterns using addable letter boxes with inclusive and exclusive filters.
  - **📖 Dictionary Mode**: Quick lookup for any word's definition.
  - **🎵 Rhyme Mode**: Find perfect rhymes for any given word.
  - **💡 Concept Mode**: Reverse-dictionary search to find words based on concepts or descriptions.
- **Resizable Interface**: Draggable divider between input and results sections for optimized workspace.
- **Improved Coverage**: Uses 26 parallel API calls for pattern matching to ensure exhaustive results.
- **Cloud-Powered Vocabulary**: Real-time integration with the Datamuse API for millions of terms.
- **Clean Definitions**: Direct, easy-to-read meanings without technical clutter.

## 🚀 Getting Started

### Prerequisites
- [Android Studio Iguana](https://developer.android.com/studio) or newer.
- Android SDK 34+.
- Active Internet connection.

### Installation & Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Abid-Al-Hossain/Lettery_V2.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio and select **File > Open**.
   - Navigate to the project root and click **OK**.
3. **Build & Run**:
   - Connect your device or emulator and click the **Run** button.

## Technical Architecture
- **Language**: Kotlin
- **Network**: Retrofit 2 + GSON for Datamuse API integration.
- **Concurrency**: Kotlin Coroutines & Lifecycle scoping.
- **UI**: Material Design components & Custom Touch Logic for resizing.

## Authors
- **Abid Al Hossain Swakkhar**
- **Denesh Barua Pantho**
- **Fahad Bin Aziz Nabil**

## License
This project is licensed under the [MIT License](LICENSE).
