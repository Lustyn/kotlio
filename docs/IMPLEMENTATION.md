# Kotlio Implementation Summary

## Overview
Kotlio is a Kotlin-based library for building interactive web frontends, inspired by Gradio. It allows developers to prototype UIs using pure Kotlin code with an embedded server—no CLI tools required.

## Architecture

### Multiplatform Structure
- **commonMain**: Shared schema, DSL builders, and runtime logic
- **jvmMain**: Embedded Ktor server with REST API
- **jsMain**: DOM-native browser client with event dispatch

### Core Components

#### 1. Schema Models (`schema.kt`)
- `KotlioSchema`: Top-level schema containing pages
- `PageSchema`: Page definition with components and actions
- `ComponentSchema`: Component metadata (inputs/outputs)
- `ActionSchema`: Action button metadata
- `ActionInvocation`: Request payload for action execution
- `ActionResponse`: Response with state updates
- `ComponentUpdate`: Individual component state change

#### 2. DSL Builder (`builder.kt`)
- `kotlioApp { }`: Entry point for app definition
- `page(title) { }`: Page builder
- Component builders:
  - `textInput(label, id)`: Text input field
  - `fileInput(label, accepts, id)`: File upload
  - `textOutput(id, label)`: Text display
  - `listOutput(id, label)`: List display
- `action(label, id) { }`: Action button with handler

#### 3. Runtime (`runtime.kt`)
- `ActionContext`: Execution context for actions
  - `read(handle)`: Read input values
  - `update(handle, value)`: Update output values
  - `collectUpdates()`: Gather state changes
- `ActionContextFactory`: Creates contexts from input values

#### 4. JVM Server (`server/KotlioServer.kt`)
- Embedded Ktor Netty server
- Endpoints:
  - `GET /`: Health check message
  - `GET /schema`: Returns KotlioSchema JSON
  - `GET /health`: Health status
  - `POST /action`: Executes action handler
    - Accepts: `ActionInvocation` JSON
    - Returns: `ActionResponse` JSON with updates

#### 5. JS Client (`client/KotlioClient.kt`)
- `KotlioClient(baseUrl)`: Client instance
  - `mount(containerId, schema)`: Render UI to DOM
- `PageRenderer`: Builds DOM tree from schema
- `KotlioDomBindings`: Updates DOM components
  - `updateText(id, value)`
  - `updateList(id, entries)`
- HTTP dispatcher for action invocations

## Event Dispatch Flow

1. **User Interaction**: User clicks action button in browser
2. **Gather Inputs**: JS client collects all input values
3. **HTTP POST**: Client sends `ActionInvocation` to `/action` endpoint
4. **Server Execution**:
   - Looks up action handler by ID
   - Creates `ActionContext` with input values
   - Invokes handler (suspend function)
   - Collects output updates
5. **Response**: Server returns `ActionResponse` with updates
6. **DOM Update**: Client applies updates to DOM bindings

## Data Flow

```
┌─────────────────────────────────────────────────────┐
│                  Browser (JS)                        │
│  ┌──────────────────────────────────────────┐       │
│  │  KotlioClient                             │       │
│  │  • Fetches schema from server             │       │
│  │  • Renders DOM from PageSchema            │       │
│  │  • Dispatches actions via HTTP            │       │
│  │  • Updates DOM with ActionResponse        │       │
│  └──────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────┘
                        │ ▲
                        │ │
            POST /action│ │ ActionResponse
        ActionInvocation│ │ (updates)
                        │ │
                        ▼ │
┌─────────────────────────────────────────────────────┐
│                  JVM Server                          │
│  ┌──────────────────────────────────────────┐       │
│  │  Ktor Endpoints                           │       │
│  │  GET  /schema  → KotlioSchema             │       │
│  │  POST /action  → ActionResponse           │       │
│  │                                            │       │
│  │  Action Execution:                         │       │
│  │  1. Receive ActionInvocation               │       │
│  │  2. Create ActionContext(inputs)           │       │
│  │  3. Invoke handler(context)                │       │
│  │  4. Collect updates from context           │       │
│  │  5. Return ActionResponse(updates)         │       │
│  └──────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────┘
```

## Usage Example

```kotlin
import kotlio.*
import kotlio.server.runKotlioApp

fun main() {
    runKotlioApp(port = 7860) {
        page("Greeter") {
            val nameInput = textInput("Your Name")
            val greetingOutput = textOutput("greeting", "Greeting")
            
            action("Say Hello") {
                val name = read(nameInput)
                update(greetingOutput, "Hello, $name!")
            }
        }
    }.start(wait = true)
}
```

## Testing

### JVM Tests
- Schema construction validation
- Action handler execution
- Server startup/shutdown
- State update collection

### JS Tests
- Schema building (common multiplatform tests)

### Integration Tests
- Full round-trip action execution
- Input reading and output updating
- Error handling

## Build

```bash
# Build all targets
./gradlew build

# Run JVM tests
./gradlew kotlio-core:jvmTest

# Run JS tests
./gradlew kotlio-core:jsTest

# Run all tests
./gradlew kotlio-core:allTests
```

## Key Design Decisions

1. **DOM-Native Rendering**: JS client directly manipulates DOM instead of using Canvas or framework wrappers
2. **Embedded Server**: No separate CLI tool; server is embedded in user's Kotlin code
3. **Multiplatform Core**: Schema and DSL shared across JVM and JS
4. **HTTP-Based Dispatch**: Simple REST API for action invocations (WebSocket upgrade possible later)
5. **Type-Safe DSL**: Leverages Kotlin's DSL capabilities for declarative UI construction
6. **Suspend Actions**: Action handlers are suspend functions supporting async operations

## Future Enhancements

- WebSocket support for real-time bidirectional updates
- File upload handling with multipart support
- Additional component types (sliders, dropdowns, etc.)
- Client-side state persistence
- Error boundaries and validation
- CSS theming system
- Multiple page navigation
