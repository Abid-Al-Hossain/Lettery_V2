# Letterly - Smart Word Suggestion App

## Overview
Letterly is a powerful Android dictionary and word-finding tool designed to help users discover words based on specific patterns and constraints. Whether you're solving puzzles, expanding your vocabulary, or looking for writing inspiration, Letterly provides exhaustive, real-time suggestions powered by the cloud.

## Features
- **14 Search Modes (API Superpowers)**:
  - **🔠 Pattern**: Exhaustive pattern matching using custom boxes and and multi-thread API synthesis.
  - **📖 Dictionary**: Instant lookup for meanings.
  - **🎵 Rhyme**: Perfect phonetic rhymes.
  - **💡 Concept**: Reverse-dictionary search (Ideas -> Words).
  - **👯 Synonym/Antonym**: Similar and opposite meanings.
  - **👂 Sound-Alike**: Phonetic search for spelling-agnostic results.
  - **👯 Homophone**: Find words that sound identical.
  - **🎨 Adjectives/Nouns**: Descriptors and modified nouns.
  - **🎶 Consonant**: Alliteration helper and consonant matching.
  - **➡️ Followers/Predecessors**: Contextual word prediction (Bigrams).
  - **🔫 Trigger**: Associated concepts and text triggers.
- **Dynamic Instruction System**: Every mode includes a real-time **Description Card** explaining exactly what it does and how to use it.
- **Resizable Interface**: Draggable divider for a custom-sized workspace.
- **Improved Performance**: Consolidated UI logic for lightweight, high-speed word discovery.
- **Cloud-Powered Vocabulary**: Millions of terms via real-time Datamuse API integration.

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
