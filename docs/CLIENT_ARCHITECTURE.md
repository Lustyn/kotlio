# Client Architecture

## Overview

The Kotlio client is a Kotlin/JS application that runs in the browser and renders the UI based on a schema fetched from the server.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                               │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 1. Load index.html (minimal shell + CSS)              │ │
│  └──────────────────────┬─────────────────────────────────┘ │
│                         ↓                                    │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 2. Load kotlio-core.js (Kotlin/JS bundle)             │ │
│  │    • main() entry point executes                      │ │
│  │    • Waits for DOMContentLoaded                       │ │
│  └──────────────────────┬─────────────────────────────────┘ │
│                         ↓                                    │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 3. Fetch /schema → GET Request                        │ │
│  └──────────────────────┬─────────────────────────────────┘ │
└────────────────────────┼──────────────────────────────────┘
                         │
                    HTTP (JSON)
                         │
┌────────────────────────▼──────────────────────────────────┐
│                    JVM Server                              │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ GET /schema → Returns KotlioSchema                   │ │
│  │ {                                                    │ │
│  │   "pages": [{                                        │ │
│  │     "title": "Example",                              │ │
│  │     "components": [...],                             │ │
│  │     "actions": [...]                                 │ │
│  │   }]                                                 │ │
│  │ }                                                    │ │
│  └──────────────────────┬───────────────────────────────┘ │
└─────────────────────────┼─────────────────────────────────┘
                          │
                     JSON Schema
                          │
┌─────────────────────────▼─────────────────────────────────┐
│                      Browser                               │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ 4. Parse schema → KotlioSchema                       │ │
│  └──────────────────────┬───────────────────────────────┘ │
│                         ↓                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ 5. KotlioClient.mount("app", schema)                 │ │
│  │    • Create PageRenderer                             │ │
│  │    • Render each component to DOM                    │ │
│  │    • Attach event listeners to actions               │ │
│  └──────────────────────┬───────────────────────────────┘ │
│                         ↓                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ 6. User sees rendered page                           │ │
│  │    • Inputs, outputs, buttons, static content        │ │
│  │    • Dark mode support                               │ │
│  │    • All styling from CSS variables                  │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

## Entry Point

### main() Function

**File:** `kotlio-core/src/jsMain/kotlin/kotlio/client/KotlioClient.kt`

```kotlin
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    window.addEventListener("DOMContentLoaded", {
        GlobalScope.launch {
            try {
                // Fetch schema from server
                val response = window.fetch("/schema").await()
                if (!response.ok) {
                    showError("Failed to load application")
                    return@launch
                }
                
                val schemaJson = response.text().await()
                val json = Json { ignoreUnknownKeys = true }
                val schema = json.decodeFromString<KotlioSchema>(schemaJson)
                
                // Initialize Kotlio client and mount to DOM
                val client = KotlioClient()
                client.mount("app", schema)
                
                println("Kotlio app loaded successfully")
            } catch (e: Exception) {
                showError("Failed to initialize Kotlio app: ${e.message}")
            }
        }
    })
}
```

**What it does:**
1. Waits for DOM to be ready (`DOMContentLoaded`)
2. Fetches `/schema` endpoint from server
3. Deserializes JSON to `KotlioSchema`
4. Creates `KotlioClient` instance
5. Mounts app to `#app` div
6. Handles errors gracefully

## KotlioClient Class

### Initialization

```kotlin
class KotlioClient(
    private val baseUrl: String = "",
    private val scope: CoroutineScope = GlobalScope
)
```

**Parameters:**
- `baseUrl` - API base URL (empty for same origin)
- `scope` - Coroutine scope for async operations

### mount() Method

```kotlin
fun mount(containerId: String, schema: KotlioSchema): KotlioDomBindings
```

**What it does:**
1. Finds container element in DOM
2. Clears existing content
3. Gets first page from schema
4. Creates `PageRenderer`
5. Renders all components
6. Appends to container
7. Returns bindings for programmatic updates

## PageRenderer

### Component Rendering

The `PageRenderer` renders each component type:

**Interactive Components:**
- `TEXT_INPUT` → `<input type="text">`
- `FILE_INPUT` → `<input type="file">`
- `TEXT_OUTPUT` → `<div class="kotlio-text-output">`
- `LIST_OUTPUT` → `<ul>` with `<li>` items
- Actions → `<button>` with click handlers

**Static Content Components:**
- `HEADING` → `<h1>` through `<h6>` (based on level)
- `TEXT` → `<p>`
- `CODE` → `<pre><code class="language-{lang}">`
- `DIVIDER` → `<hr>`
- `HTML` → `<div>` with innerHTML

### Event Handling

**Action Dispatch:**
```kotlin
button.addEventListener("click", {
    scope.launch {
        val invocation = ActionInvocation(action.id, gatherInputValues())
        dispatchAction(invocation, bindings)
    }
})
```

**Flow:**
1. User clicks button
2. Gather all input values
3. Create `ActionInvocation` with action ID and inputs
4. POST to `/action` endpoint
5. Parse `ActionResponse`
6. Update outputs based on response

## Action Dispatch

### Request

**Endpoint:** `POST /action`

**Body:**
```json
{
  "id": "greet",
  "inputs": {
    "textInput-0": "Alice"
  }
}
```

### Response

```json
{
  "success": true,
  "updates": {
    "greeting-output": {
      "type": "TEXT",
      "value": "Hello, Alice! Welcome to Kotlio!"
    }
  },
  "error": null
}
```

### Update Application

```kotlin
actionResponse.updates.forEach { (componentId, update) ->
    when (update.type) {
        UpdateType.TEXT -> bindings.updateText(componentId, update.value)
        UpdateType.LIST -> bindings.updateList(componentId, items)
    }
}
```

## DOM Bindings

### KotlioDomBindings

Provides programmatic access to rendered components:

```kotlin
class KotlioDomBindings(
    internal val root: HTMLElement,
    private val textOutputs: Map<String, HTMLElement>,
    private val listOutputs: Map<String, HTMLUListElement>
)
```

**Methods:**
- `updateText(id, value)` - Update text output
- `updateList(id, items)` - Update list output

## Loading Flow

### 1. Initial State (index.html)

```html
<div id="app">
    <div class="loading">
        <div class="spinner"></div>
        <div class="loading-text">Loading Kotlio app...</div>
    </div>
</div>
```

### 2. After Schema Fetch

The loading state is replaced with rendered components:

```html
<div id="app">
    <div class="kotlio-page">
        <h2>Example Page</h2>
        <div class="kotlio-components">
            <!-- Rendered components here -->
        </div>
        <div class="kotlio-actions">
            <!-- Action buttons here -->
        </div>
    </div>
</div>
```

## Error Handling

### Schema Fetch Failure

```kotlin
if (!response.ok) {
    showError("Failed to load application: ${response.status} ${response.statusText}")
    return@launch
}
```

Shows error UI:
```html
<div style="...error styles...">
    ⚠️
    Failed to load application: 500 Internal Server Error
</div>
```

### Action Dispatch Failure

```kotlin
if (!actionResponse.success) {
    println("Action execution failed: ${actionResponse.error}")
    return
}
```

Logs to console, doesn't crash the app.

## Styling

All styles are defined in `index.html` using CSS variables:

```css
:root {
    --color-primary: #7c3aed;
    --color-bg-primary: #ffffff;
    /* ... */
}

@media (prefers-color-scheme: dark) {
    :root {
        --color-primary: #8b5cf6;
        --color-bg-primary: #0f172a;
        /* ... */
    }
}
```

Components automatically inherit theme values.

## Bundle Size

**Production Bundle:**
- `kotlio-core.js` - ~300 KB (includes Kotlin stdlib, coroutines, serialization)
- `kotlio-core.js.map` - ~273 KB (source map for debugging)
- `index.html` - ~16 KB (CSS theme + minimal HTML shell)

**Total:** ~589 KB uncompressed

With gzip compression (typical on production servers):
- `kotlio-core.js` - ~70-80 KB gzipped
- Total delivered - ~90-95 KB gzipped

## Browser Support

**Requires:**
- ES6+ JavaScript
- Fetch API
- Promises/async-await
- CSS Variables
- CSS Grid/Flexbox

**Tested on:**
- Chrome/Edge 88+
- Firefox 86+
- Safari 14+
- Opera 74+

## Development

### Building

```bash
./gradlew kotlio-core:jsBrowserProductionWebpack
```

**Output:**
- `build/kotlin-webpack/js/productionExecutable/kotlio-core.js`

### Testing

```bash
./gradlew kotlio-core:jsTest
```

Runs tests in headless Chrome via Karma.

### Debugging

1. Load source maps in browser DevTools
2. Set breakpoints in Kotlin source
3. Inspect variables and coroutines
4. Console logs from `println()` appear in DevTools

## Key Files

- **Entry Point:** `src/jsMain/kotlin/kotlio/client/KotlioClient.kt` (main function)
- **Client Class:** `KotlioClient` (mount, dispatch)
- **Renderer:** `PageRenderer` (component rendering)
- **Bindings:** `KotlioDomBindings` (DOM updates)
- **HTML Shell:** `src/jsMain/resources/index.html` (CSS + loading state)

## Summary

The Kotlio client:

1. ✅ **Starts automatically** via `main()` entry point
2. ✅ **Fetches schema** from `/schema` endpoint
3. ✅ **Renders dynamically** based on schema
4. ✅ **Handles actions** via HTTP POST to `/action`
5. ✅ **Updates UI** based on server responses
6. ✅ **Supports theming** via CSS variables
7. ✅ **Handles errors** gracefully
8. ✅ **Zero configuration** for end users

The entire page is generated from the Kotlin DSL on the server - the HTML is just a shell that loads the client! 🎉
