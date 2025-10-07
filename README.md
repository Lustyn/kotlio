# Kotlio

A Kotlin-based library for building interactive web frontends, inspired by Gradio. Build prototype UIs with pure Kotlin code—no CLI tools, no HTML required.

## Features

- ✨ **Pure Kotlin DSL** — Define UIs in type-safe Kotlin code
- 🚀 **Embedded Server** — Built-in Ktor server, no external tools needed
- 🌐 **DOM-Native** — JS client renders directly to browser DOM
- 🔄 **REST API** — Simple HTTP endpoints for actions and schema
- 🎨 **Modern Theme** — Dark mode support with CSS variables
- 📝 **Rich Components** — Interactive inputs/outputs and static content (headings, code, text)

## Quick Start

### 1. Add Dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("kotlio:kotlio-core:1.0.0")
}
```

### 2. Create Your App

```kotlin
import kotlio.*
import kotlio.server.runKotlioApp

fun main() {
    runKotlioApp(port = 8080) {
        page("Hello Kotlio") {
            heading("Welcome!", level = 1)
            text("Try the interactive greeter below:")
            
            val nameInput = textInput("Your Name")
            val greeting = textOutput("greeting")
            
            action("Say Hello") {
                val name = read(nameInput)
                update(greeting, "Hello, $name! 👋")
            }
        }
    }.start(wait = false)
    
    println("Server running at http://localhost:8080")
    Thread.currentThread().join()
}
```

### 3. Run

```bash
./gradlew run
```

Visit **http://localhost:8080** in your browser!

## Example App

For a complete demo with examples:

```kotlin
fun main() {
    runKotlioApp {
        examplePage()  // Pre-built demo page
    }
}
```

Try it:
```bash
./gradlew example:run
```

## Components

### Interactive Components

```kotlin
// Inputs
val textInput = textInput("Label")
val fileInput = fileInput("Upload", accepts = listOf("image/*"))

// Outputs
val textOutput = textOutput("result", "Result Label")
val listOutput = listOutput("items", "Items")

// Actions
action("Submit") {
    val input = read(textInput)
    update(textOutput, "You entered: $input")
}
```

### Static Content Components

```kotlin
// Documentation
heading("Getting Started", level = 2)
text("This is a paragraph of instructional text.")
divider()

// Code samples
code("""
    fun example() {
        println("Hello!")
    }
""", language = "kotlin")

// Custom HTML
html("""
    <ul>
        <li>Feature 1</li>
        <li>Feature 2</li>
    </ul>
""")
```

## Architecture

```
┌─────────────────────┐
│   Browser (JS)      │
│  • KotlioClient     │
│  • DOM Rendering    │
│  • Event Dispatch   │
└──────────┬──────────┘
           │ HTTP (JSON)
           │
┌──────────▼──────────┐
│   JVM Server        │
│  • Ktor Embedded    │
│  • Action Handlers  │
│  • State Management │
└─────────────────────┘
```

**Multiplatform:**
- `commonMain` — Shared schema & DSL
- `jvmMain` — Ktor server
- `jsMain` — DOM renderer

## Documentation

Explore the full documentation in the [`docs/`](docs/) directory:

- **[Components Reference](docs/COMPONENTS.md)** — Complete API for all components
- **[Theming Guide](docs/THEMING.md)** — Customize colors, spacing, and design tokens
- **[Implementation Details](docs/IMPLEMENTATION.md)** — Architecture and technical overview
- **[Static Components](docs/STATIC_COMPONENTS.md)** — Using content components
- **[Routing](docs/ROUTING.md)** — How Ktor routing works
- **[Static Assets](docs/STATIC_ASSETS.md)** — Asset bundling architecture
- **[Theme Upgrade](docs/THEME_UPGRADE.md)** — Theme system improvements

## Project Structure

```
kotlio/
├── kotlio-core/           # Main library
│   ├── src/
│   │   ├── commonMain/    # Shared Kotlin code
│   │   ├── jvmMain/       # JVM server
│   │   ├── jsMain/        # JS client
│   │   └── commonTest/    # Tests
│   └── build.gradle.kts
├── example/               # Example application
│   └── src/main/kotlin/
└── docs/                  # Documentation
```

## Development

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test              # JVM + JS tests
./gradlew jvmTest           # JVM only
./gradlew jsTest            # JS only
```

### Run Example

```bash
./gradlew example:run
```

## API Endpoints

When running a Kotlio app:

- `GET /` — Serves the interactive UI
- `GET /schema` — Returns application schema (JSON)
- `POST /action` — Executes action handlers
- `GET /health` — Health check

## Examples

### Simple Form

```kotlin
page("Contact") {
    heading("Contact Us", level = 1)
    
    val name = textInput("Name")
    val email = textInput("Email")
    val message = textOutput("confirmation")
    
    action("Submit") {
        update(message, "Thanks ${read(name)}! We'll email you at ${read(email)}")
    }
}
```

### Documentation Page

```kotlin
page("Docs") {
    heading("API Documentation", level = 1)
    text("Learn how to use our API.")
    
    divider()
    
    heading("Authentication", level = 2)
    code("""
        curl -H "Authorization: Bearer TOKEN" \\
             https://api.example.com/data
    """, language = "bash")
}
```

## Theme

Kotlio includes a modern, dark mode-friendly theme with CSS variables:

- 🌓 Automatic light/dark mode based on system preference
- 🎨 Purple accent colors (fully customizable)
- 📐 Consistent spacing and typography
- ♿ WCAG AA accessible
- 📱 Mobile responsive

See [THEMING.md](docs/THEMING.md) for customization.

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Contributing

Contributions welcome! Please read the contributing guidelines first.

## Credits

Inspired by [Gradio](https://gradio.app/) — A Python library for building ML demos.

---

**Built with ❤️ using Kotlin Multiplatform**
