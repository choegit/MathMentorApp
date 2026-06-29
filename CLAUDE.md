# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Math Mentor is a single-Activity Android app (Kotlin + Jetpack Compose, Material 3) that acts as a Socratic math tutor for children (ages 7–12). It sends the child's question to OpenAI's `gpt-3.5-turbo` Chat Completions API and renders the reply.

Note: despite the repo/README name "MathMentorApp", the actual package and namespace is `com.example.mathmentor2` and the Gradle project name is "Math Mentor 2".

## Build & Test Commands

Use the Gradle wrapper (`./gradlew` on Unix, `gradlew.bat` on Windows). Requires JDK 17 and the Android SDK.

- Build debug APK: `./gradlew assembleDebug`
- Build everything: `./gradlew build`
- Install to a connected device/emulator: `./gradlew installDebug`
- Run JVM unit tests: `./gradlew test` (or `testDebugUnitTest`)
- Run instrumented tests (needs a device/emulator): `./gradlew connectedAndroidTest`
- Run a single unit test: `./gradlew test --tests "com.example.mathmentor2.ExampleUnitTest"`
- Lint: `./gradlew lint`

## Required Local Setup

The OpenAI API key is injected at build time, not hardcoded. `app/build.gradle.kts` reads `OPENAI_API_KEY` from `local.properties` and exposes it via `BuildConfig.OPENAI_API_KEY`. Add to `local.properties` (gitignored):

```
OPENAI_API_KEY=sk-...
```

Without it the app compiles but every API call returns an auth error.

## Architecture

The entire app lives in a single file: `app/src/main/java/com/example/mathmentor2/MainActivity.kt`. There is no repository or DI layer; state and networking are owned by one ViewModel.

- `MainActivity` → sets the Compose content with `MathMentor2Theme` and renders `MathTutorScreen`.
- `MathTutorScreen(viewModel = viewModel())` (composable) → stateless; reads `question`/`response`/`loading` from the ViewModel and delegates input/clicks to `onQuestionChange` / `askTutor`.
- `MathTutorViewModel : ViewModel()` → owns the UI state as `mutableStateOf` with `private set` (read-only to the composable). `askTutor()` runs the call in `viewModelScope` (tied to the ViewModel lifecycle, so it survives configuration changes and is cancelled on clear). It reuses a single `OkHttpClient` configured with 15s connect / 60s read timeouts.
- `fetchGPTResponse(userQuestion)` → a private suspend method that does the blocking work in `withContext(Dispatchers.IO)`: builds the request body with `org.json` (`JSONObject`/`JSONArray`) as a two-message chat (a `system` message + the raw `user` question), POSTs to `https://api.openai.com/v1/chat/completions` with raw OkHttp, and manually parses `choices[0].message.content`. Errors are caught and returned as a user-facing `"Error: ..."` string rather than thrown: `IOException` maps to a "check your internet connection" message, and non-2xx responses surface OpenAI's `error.message` from the body when present. State updates happen back on the main dispatcher (the `viewModelScope` default).
- The tutor persona lives in the `SYSTEM_PROMPT` constant in the ViewModel's `companion object` — edit it there to change tutoring behavior (it is sent as the `system` message, separate from the child's question).

`ui/theme/` (Color.kt, Theme.kt, Type.kt) holds the standard generated Compose Material 3 theme.

Key consequences for changes:
- The tutoring behavior is controlled entirely by the `SYSTEM_PROMPT` constant in `MathTutorViewModel`'s `companion object`.
- The `viewModel()` composable accessor comes from `androidx.lifecycle:lifecycle-viewmodel-compose` (declared in the version catalog).

## Dependencies

Managed via Gradle version catalog at `gradle/libs.versions.toml` — add/update dependencies and versions there, not inline in `build.gradle.kts`. Networking is OkHttp; JSON is the built-in `org.json` (no Moshi/Gson/kotlinx-serialization).

- `compileSdk`/`targetSdk` = 35, `minSdk` = 34, Java/Kotlin target 17.