# Notely Voice

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.8.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

A completely free, modern, cross-platform, 100% private AI voice transcription & note-taking application with powerful Whisper AI Voice to Text capabilities built with Compose Multiplatform.

Perfect for students capturing lectures, professionals documenting meetings, doctors recording patient notes, researchers transcribing interviews, and anyone needing accessible hands-free note-taking across all their devices.



> **Two versions of NotelyVoice**
> This repository is the **open source** version, distributed freely on F-Droid under GPL-3.0 and actively maintained.
> A separate, fully rebuilt version with a new codebase, redesigned UI, and a subscription model is available on Google Play.
> Revenue from the Play Store version funds ongoing development of **both** versions.

| | F-Droid (this repo) | Google Play |
|---|---|---|
| Price | Free | Subscription |
| Source | Open source (GPL-3.0) | Proprietary |
| Codebase | This repository | Rebuilt from scratch |
| UI | Original | Fully redesigned |
| Updates | Active | Active |

## Download the app
<div style="display:flex;" >
<a href="https://f-droid.org/en/packages/com.module.notelycompose.android">
    <img alt="Get it on F-Droid" height="64" src="https://raw.githubusercontent.com/anwilli5/coin-collection-android-US/main/images/fdroid-repo-badge.png" />
</a>
<a href="https://play.google.com/store/apps/details?id=com.module.notelycompose.android">
    <img alt="Get it on Google Play" height="64" src="https://raw.githubusercontent.com/anwilli5/coin-collection-android-US/main/images/google-play-badge.png" />
</a>
<a href="https://apps.apple.com/us/app/notely-voice-speech-to-text/id6745835691">
    <img alt="Available at Appstore" height="64" src="https://dbsqho33cgp4y.cloudfront.net/github/app-store-badge.png" />
</a>
</div>

## Screenshots

<img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="screenshot3" width="250">

<img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" alt="screenshot3" width="250">

## Features

### Note-Taking
✏️ **Rich Text Editing** - Format your notes with:
- Headers and sub-headers
- Title styling
- Bold, italic, and underline text
- Text alignment (left, right, center)

🔍 **Simple Search** - Find your notes instantly with text search  
📊 **Smart Filtering** - Filter notes by type (Starred, Voice Notes, Recent)  
📂 **Organization** - Categorize notes with folders and tags

### Speech Recognition
🎙️ **Advanced Speech-to-Text** - Convert speech to text with high accuracy  
🌐 **Offline Capability** - Speech recognition works without an internet connection  
🔄 **Seamless Integration** - Dictate directly into notes or transcribe audio recordings  
🎧 **Audio Recording** - Record voice notes and play them back within the app  
🎧 **Unlimited Transcriptions** - Transcribe unlimited voice notes to multiple languages

### General
🌓 **Theming** - Switch between dark and light themes based on your preference  
💻 **Cross-Platform** - Seamless experience across Android & iOS  
📱 **Share Audio Functionality** - Share audios recorded on the App to Messages, WhatsApp, Files, Google Drive etc  
📱 **Share Texts** - Share texts on the App to Messages, WhatsApp, Files, Google Drive etc

## Audio Processing Improvements

### Memory-Efficient Audio Processing

The app now includes memory-optimized audio processing to handle large files without Out of Memory (OOM) errors:

1. **Streaming WAV Decoder**: Processes audio files in chunks instead of loading entire files into memory
2. **Overlapping Chunk Transcription**: Splits long audio files into manageable chunks with overlap for seamless transcription
3. **Configurable Chunk Sizes**: Adjustable chunk sizes based on device memory and performance requirements

### Chunking Configuration

The transcription process uses configurable parameters:

- **Default Chunk Size**: 30 seconds per chunk
- **Overlap Size**: 3 seconds overlap between chunks
- **Maximum Chunk Size**: 60 seconds (configurable)
- **Memory Buffer**: 1MB processing chunks

### Benefits

- **No More OOM Errors**: Large audio files (hours long) can be processed without memory issues
- **Better Performance**: Smaller chunks process faster and use less memory
- **Seamless Transcription**: Overlapping chunks ensure no text is lost at chunk boundaries
- **Progressive Processing**: Real-time progress updates as chunks are processed

## Speech Recognition Technology

- **OpenAI Whisper** - State-of-the-art open-source automatic speech recognition
  - Robust multilingual speech recognition with support for over 50 languages
  - Trained on 680,000 hours of multilingual and multitask supervised data
  - Excellent performance across diverse audio conditions and accents
  - Can run locally without internet dependency once model is downloaded

- **Cross-Platform Compatibility** - Designed for versatile deployment
  - Consistent speech recognition quality across different operating systems
  - Flexible model sizes from tiny (39 MB) to large (1550 MB) based on accuracy needs
  - Advanced noise robustness and speaker independence
  - Perfect for applications requiring high-quality transcription in various environments

## Built With 🛠

- **[Kotlin](https://kotlinlang.org/)** - Official programming language for Android development
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)** - UI toolkit for building native applications
- **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** - For asynchronous programming
- **[Android Architecture](https://developer.android.com/topic/architecture)** - Ensures scalability and testability
- **[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)** - Stores and manages UI-related data
- **[Koin](https://insert-koin.io/docs/quickstart/android/)** - Dependency injection for Android
- **[Material 3](https://m3.material.io/)** - Design system for modern UI
- **[Whisper AI](https://openai.com/index/whisper/)** - Human level robustness speech recognition
- **Native Compose Navigation** - No third-party navigation libraries
- **Custom Text Editor** - Built from scratch without external editing libraries

## Architecture

Notely is built with Android Architecture principles, separating the app into distinct layers:

- **UI Layer**: Compose UI components
- **Presentation Layer**: Platform Independent ViewModels
- **Domain Layer**: Business logic and use cases
- **Data Layer**: Repositories and data sources

<img src="assets/layered_architecture_diagram.png" alt="Logo" width="70%">

## Project Structure
`shared/`: Contains shared business logic and UI code.

`androidApp/`: Contains Android-specific code.

`iosApp/`: Contains iOS-specific code.

## Contributing
Contributions are welcome! Please follow these steps:

- Fork the repository.
- Create a new branch for your feature or bug fix.
- Submit a pull request with a clear description of your changes.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- XCode 16.1
- JDK 11 or higher
- Kotlin 2.0.21 or higher

### Installation

1. Clone the repository
   ```sh
   git clone https://github.com/tosinonikute/NotelyVoice.git
   ```

2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

### License

```
Copyright (C) 2025 NotelyVoice

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3 only 
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
```
