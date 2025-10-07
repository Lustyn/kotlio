# Kotlio Components Reference

Kotlio provides a set of components for building interactive web applications in pure Kotlin.

## Component Types

### Interactive Components

Components that accept user input or display dynamic output.

#### Text Input

```kotlin
val nameInput = textInput(label = "Your Name", id = "name")
```

**Parameters:**
- `label`: String - Label displayed above the input (optional)
- `id`: String - Unique component identifier (auto-generated if not provided)

**Returns:** `InputHandle<String>` - Handle to read the input value

#### File Input

```kotlin
val fileInput = fileInput(
    label = "Upload File",
    accepts = listOf("image/*", ".pdf"),
    id = "file-upload"
)
```

**Parameters:**
- `label`: String - Label displayed above the input (optional)
- `accepts`: List<String> - MIME types or file extensions to accept (default: `["*/*"]`)
- `id`: String - Unique component identifier (auto-generated if not provided)

**Returns:** `InputHandle<FileReference>` - Handle to read the uploaded file reference

#### Text Output

```kotlin
val resultOutput = textOutput(id = "result", label = "Result")
```

**Parameters:**
- `id`: String - Unique component identifier (required)
- `label`: String - Label displayed above the output (optional)

**Returns:** `OutputHandle<String>` - Handle to update the output value

#### List Output

```kotlin
val listOutput = listOutput(id = "items", label = "Items")
```

**Parameters:**
- `id`: String - Unique component identifier (required)
- `label`: String - Label displayed above the list (optional)

**Returns:** `OutputHandle<List<JsonElementRef>>` - Handle to update the list items

### Static Content Components

Components for displaying documentation, instructions, and formatted content.

#### Heading

```kotlin
heading("Welcome to Kotlio!", level = 1)
heading("Getting Started", level = 2)
heading("Details", level = 3)
```

**Parameters:**
- `text`: String - Heading text (required)
- `level`: Int - Heading level 1-6 (default: 2)
- `id`: String - Unique component identifier (auto-generated if not provided)

**Renders:** `<h1>` through `<h6>` elements

#### Text

```kotlin
text("This is a paragraph of text explaining the feature.")
```

**Parameters:**
- `content`: String - Text content (required)
- `id`: String - Unique component identifier (auto-generated if not provided)

**Renders:** `<p>` element with the text content

#### Code Block

```kotlin
code("""
    fun main() {
        println("Hello, Kotlio!")
    }
""", language = "kotlin")

code("<div>HTML example</div>", language = "html")
```

**Parameters:**
- `content`: String - Code content (required, automatically trimIndent-ed)
- `language`: String - Programming language for syntax highlighting (default: "kotlin")
- `id`: String - Unique component identifier (auto-generated if not provided)

**Renders:** `<pre><code class="language-{lang}">` element

**Supported languages:** kotlin, java, javascript, typescript, html, css, json, xml, sql, python, bash, etc.

#### Divider

```kotlin
divider()
```

**Parameters:**
- `id`: String - Unique component identifier (auto-generated if not provided)

**Renders:** `<hr>` horizontal rule

#### HTML

```kotlin
html("""
    <ul>
        <li><strong>Feature 1</strong> - Description</li>
        <li><strong>Feature 2</strong> - Description</li>
    </ul>
""")
```

**Parameters:**
- `content`: String - Raw HTML content (required)
- `id`: String - Unique component identifier (auto-generated if not provided)

**Renders:** HTML content wrapped in a `<div>`

**⚠️ Security Note:** Be careful with user-generated HTML to avoid XSS vulnerabilities.

### Actions

Actions are interactive buttons that trigger server-side logic.

```kotlin
action(label = "Submit", id = "submit-action") {
    // Handler code
    val input = read(inputHandle)
    update(outputHandle, "Result: $input")
}
```

**Parameters:**
- `label`: String - Button text (required)
- `id`: String - Unique action identifier (auto-generated if not provided)
- `handler`: ActionHandler - Suspend lambda executed when clicked (required)

## Usage Examples

### Simple Form

```kotlin
page("Contact Form") {
    heading("Contact Us", level = 1)
    text("Fill out the form below to get in touch.")
    
    val nameInput = textInput("Your Name")
    val emailInput = textInput("Email Address")
    val messageOutput = textOutput("confirmation")
    
    action("Submit") {
        val name = read(nameInput)
        val email = read(emailInput)
        update(messageOutput, "Thanks, $name! We'll contact you at $email soon.")
    }
}
```

### Documentation Page

```kotlin
page("API Documentation") {
    heading("API Reference", level = 1)
    text("Learn how to use our REST API.")
    
    divider()
    
    heading("Authentication", level = 2)
    text("All requests require an API key in the Authorization header:")
    code("""
        curl -H "Authorization: Bearer YOUR_API_KEY" \\
             https://api.example.com/data
    """, language = "bash")
    
    divider()
    
    heading("Endpoints", level = 2)
    html("""
        <ul>
            <li><code>GET /users</code> — List all users</li>
            <li><code>POST /users</code> — Create a new user</li>
            <li><code>GET /users/:id</code> — Get user by ID</li>
        </ul>
    """)
}
```

### Interactive Demo with Code Examples

```kotlin
page("Calculator Demo") {
    heading("Simple Calculator", level = 1)
    text("Try out the calculator by entering two numbers:")
    
    val num1 = textInput("First Number")
    val num2 = textInput("Second Number")
    val result = textOutput("result", "Result")
    
    action("Add") {
        val a = read(num1).toIntOrNull() ?: 0
        val b = read(num2).toIntOrNull() ?: 0
        update(result, "Sum: ${a + b}")
    }
    
    divider()
    
    heading("How it works", level = 2)
    text("The calculator is implemented with this simple action:")
    code("""
        action("Add") {
            val a = read(num1).toIntOrNull() ?: 0
            val b = read(num2).toIntOrNull() ?: 0
            update(result, "Sum: ${'$'}{a + b}")
        }
    """)
}
```

## Built-in Helper Functions

### Example Page

Kotlio provides a pre-built example page showcasing all features:

```kotlin
fun main() {
    runKotlioApp(port = 8080) {
        examplePage()  // Complete demo with documentation
    }
}
```

This generates a full-featured demo page with:
- Interactive greeter example
- API schema viewer
- Code samples
- Feature showcase
- Documentation

### Simple Demo Page

For a minimal demo:

```kotlin
fun main() {
    runKotlioApp(port = 8080) {
        simpleDemoPage()  // Minimal interactive demo
    }
}
```

## Reading Input Values

Use the `read()` function within an action handler:

```kotlin
val handle = textInput("Name")

action("Greet") {
    val name = read(handle)  // Returns String
    // Use the value...
}
```

For file inputs:

```kotlin
val fileHandle = fileInput("Upload")

action("Process") {
    val fileRef = read(fileHandle)  // Returns FileReference
    println("File: ${fileRef.name}, Size: ${fileRef.sizeBytes}")
}
```

## Updating Output Values

Use the `update()` suspend function within an action handler:

```kotlin
val output = textOutput("result")

action("Update") {
    update(output, "New value")  // Updates the DOM
}
```

For lists:

```kotlin
val listOut = listOutput("items")

action("Load") {
    update(listOut, listOf(
        JsonElementRef("""{"name": "Item 1"}"""),
        JsonElementRef("""{"name": "Item 2"}""")
    ))
}
```

## Component IDs

All components have a unique ID:

- **Auto-generated:** `textInput("Name")` → ID: `textInput-0`
- **Explicit:** `textInput("Name", id = "user-name")` → ID: `user-name`

IDs must be unique within a page. Duplicates will throw an error at build time.

## Best Practices

### ✅ DO

- Use semantic component types (headings, text, code for documentation)
- Provide descriptive labels for inputs
- Group related components logically
- Use dividers to separate sections
- Validate input in action handlers
- Handle edge cases (empty input, invalid data)

### ❌ DON'T

- Don't use raw HTML when a semantic component exists
- Don't reuse component IDs
- Don't skip labels on form inputs (accessibility)
- Don't trust user input without validation
- Don't create deeply nested or overly complex pages

## Styling

All components use the built-in CSS theme with dark mode support. Components automatically inherit:

- Typography scale
- Color palette
- Spacing system
- Responsive breakpoints

See `THEMING.md` for customization options.

## API Reference

Full type signatures:

```kotlin
// Interactive
fun PageBuilder.textInput(label: String = "", id: String = ...): InputHandle<String>
fun PageBuilder.fileInput(label: String = "", accepts: List<String> = ..., id: String = ...): InputHandle<FileReference>
fun PageBuilder.textOutput(id: String, label: String = ""): OutputHandle<String>
fun PageBuilder.listOutput(id: String, label: String = ""): OutputHandle<List<JsonElementRef>>
fun PageBuilder.action(label: String, id: String = ..., handler: ActionHandler)

// Static content
fun PageBuilder.heading(text: String, level: Int = 2, id: String = ...)
fun PageBuilder.text(content: String, id: String = ...)
fun PageBuilder.code(content: String, language: String = "kotlin", id: String = ...)
fun PageBuilder.divider(id: String = ...)
fun PageBuilder.html(content: String, id: String = ...)

// Helpers
fun KotlioAppBuilder.examplePage()
fun KotlioAppBuilder.simpleDemoPage()

// Context methods (within action handlers)
suspend fun ActionContext.read<T>(handle: InputHandle<T>): T
suspend fun ActionContext.update<T>(handle: OutputHandle<T>, value: T)
```

## Summary

Kotlio provides a rich set of components for building interactive applications and documentation pages. Use interactive components (inputs, outputs, actions) for user interaction, and static components (headings, text, code, dividers, HTML) for documentation and instructions.

🎯 **Quick Start:** Use `examplePage()` to see all components in action!
