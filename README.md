# MyLibrary — Android App

A clean, native Android app for tracking your books and writing chapter summaries.

## Build Instructions

1. Open **Android Studio**
2. Select **File → New → New Project** → choose **Empty Activity**
   - Name: `MyLibrary`
   - Package: `com.mylibrary.app`
   - Language: `Kotlin`
   - Min SDK: `API 26`
3. Once created, **replace all the generated files** with the files in this zip (same folder structure)
4. Click **Sync Project with Gradle Files**
5. Click **Run** (▶) to build and install on your device or emulator

## Features
- Browse your book library in a clean grid
- Search for books via Open Library API
- Choose from multiple cover options (Open Library + Google Books)
- Write chapter summaries with a distraction-free editor
- View notes with previous/next chapter navigation
- Export / Import library as JSON backup

## Dependencies (auto-downloaded by Gradle)
- Glide — image loading
- Gson — JSON serialization
- Kotlin Coroutines — async API calls
- Material Components — UI components
