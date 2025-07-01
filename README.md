# Math Mentor – AI-Powered Android App for Kids

**Math Mentor** is an educational Android app built with Kotlin and Jetpack Compose. 
It helps children understand foundational math concepts by simulating an interactive AI tutor. 
Using OpenAI's GPT-3.5-turbo model, the app guides students through math explanations with friendly, 
step-by-step conversation tailored for ages 7–12.

---

## Key Features

- **Conversational Math Tutor**\
  Children can ask math-related questions like "What is multiplication?" and receive personalized, Socratic-style responses.

- **Kid-Friendly Explanations**\
  The app uses simple language and relatable examples to teach core math skills.

- **Compose UI for Android**\
  A modern, accessible user interface built using Jetpack Compose and Material 3.

- **AI-Powered Responses**\
  Integrates OpenAI's GPT-3.5-turbo API for dynamic and age-appropriate explanations.

- **Secure and Configurable**\
  The app supports secure API key injection and easy customization for developers.

---

## Tech Stack

- **Kotlin** with **Jetpack Compose** for UI
- **OpenAI GPT-3.5 API** for AI-driven tutoring
- **OkHttp** for network calls
- **Gradle Version Catalogs** to manage dependencies
- **Material3** UI components for modern Android design

---

## Setting Up OpenAI API Access

To enable AI features in the app:

1. Visit: [OpenAI API Keys](https://platform.openai.com/account/api-keys)
2. Create a new secret key
3. Replace `OPENAI_API_KEY` in the code:

```kotlin
.header("Authorization", "Bearer OPENAI_API_KEY")
```

> Never hardcode API keys in a production release. Use encrypted storage or route through a secure backend.

---

## Planned Features

- Voice input and text-to-speech output
- Offline AI fallback with local inference
- Quiz generator and personalized feedback
- Teacher dashboard and parental progress insights

---

## Project Structure

```
MathMentorApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/mathmentor/MainActivity.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## License

Released under the **MIT License** — use, modify, and distribute freely for educational or personal projects.

---

## Screenshots (Coming Soon)

*Add screenshots of the app once installed and running on an Android device.*

