# perplexity-ai-android
A Perplexity.ai clone for Android, based on https://github.com/mckaywrigley/clarity-ai.

Perplexity is an Android application that uses OpenAI's GPT-3.5-turbo to answer user questions by searching the web, parsing relevant sources, and presenting the information in a concise and user-friendly format. The app features a smooth chat-like interface and animated typing indicators to provide an engaging experience.

## Features

- Search the web for relevant information to answer user queries
- Parse and filter website content using Kotlin
- Generate answers using OpenAI's GPT-3.5-turbo API
- Animated typing indicators and logo animations
- Display answers and sources with a visually appealing UI
- Make source links clickable for users to explore further

## Installation

1. Clone this repository:

```bash
git clone https://github.com/yourusername/Perplexity.git
```

2. Open the project in Android Studio
3. Build and run the application on an emulator or a physical device

## Configuration

Make sure to set up your OpenAI API key in the `local.properties` file:

```
openai.api_key=your_api_key_here
```

Replace `your_api_key_here` with your actual API key.

## Dependencies

- [OpenAI SDK](https://github.com/openai/openai)
- [Markwon](https://github.com/noties/Markwon)
- [OkHttp](https://github.com/square/okhttp)
- [Jsoup](https://github.com/jhy/jsoup)
