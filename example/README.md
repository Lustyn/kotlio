# Kotlio Example Application

This is a simple example demonstrating the Kotlio library in action.

## Quick Start

Just run the example app:

```bash
./gradlew example:run
```

Then open **http://localhost:8080** in your browser!

That's it! All static assets (HTML, CSS, JS bundle) are automatically included in the `kotlio-core` library.

## What's Included

This example demonstrates:
- ✨ **Pure Kotlin DSL** for defining interactive UIs
- 🚀 **Embedded Ktor server** running on port 8080
- 🌐 **REST API** for schema and action execution
- 📋 **Interactive Web UI** with live demo
- 🎨 **Modern Theme** with automatic dark mode support

## The Application

The example creates a simple greeter application defined in `src/main/kotlin/example/SimpleGreeterApp.kt`:

```kotlin
runKotlioApp(port = 8080) {
    page("Welcome") {
        val nameInput = textInput("What's your name?")
        val greetingOutput = textOutput("greeting-output", "Greeting")
        
        action("Say Hello", "greet") {
            val name = read(nameInput)
            update(greetingOutput, "Hello, $name! Welcome to Kotlio!")
        }
    }
}.start(wait = false)
```

## Available Endpoints

Once the server is running:

- **GET http://localhost:8080/** → Interactive web UI
- **GET http://localhost:8080/schema** → Application schema (JSON)
- **POST http://localhost:8080/action** → Execute actions
- **GET http://localhost:8080/health** → Health check

## Project Structure

```
example/
├── build.gradle.kts                      # Simple build config
├── src/
│   └── main/
│       └── kotlin/
│           └── example/
│               └── SimpleGreeterApp.kt   # Your application code
└── README.md                             # This file
```

All static assets (HTML, CSS, JS) are bundled inside `kotlio-core` - no manual resource management needed!

## How It Works

When you call `runKotlioApp()`, the Ktor server automatically serves:
- **Interactive web UI** from bundled resources
- **REST API endpoints** for your application
- **WebSocket support** (future enhancement)

The static assets are packaged into `kotlio-core` during build, so you just need to:
1. Add `kotlio-core` as a dependency
2. Write your app with the Kotlin DSL
3. Call `runKotlioApp()` and you're done!

## Gradle Tasks

- `example:run` — Run the example server
- `example:build` — Build the example project

## Notes

- Static assets are automatically bundled in `kotlio-core` library
- The server keeps running until you press Ctrl+C
- All you need is the dependency on `kotlio-core` - everything else is included!

## Next Steps

Try modifying `SimpleGreeterApp.kt` to:
- Add more input fields
- Create multiple pages
- Add list outputs
- Implement more complex actions

Happy prototyping with Kotlio! 🎉
