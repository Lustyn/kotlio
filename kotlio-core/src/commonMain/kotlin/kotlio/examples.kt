package kotlio

/**
 * Adds a pre-built example/documentation page to demonstrate Kotlio capabilities.
 * 
 * This creates a full demo page with:
 * - Interactive greeter example
 * - API schema viewer
 * - Documentation and code samples
 * - Feature showcase
 */
fun KotlioAppBuilder.examplePage() {
    page("Kotlio Example") {
        // Hero section
        heading("🎉 Kotlio Example Server", level = 1)
        text("Welcome to Kotlio! This is a live demo of the Kotlin-based web UI library.")
        text("The server is running a Kotlio application defined entirely in Kotlin code. Try the interactive demo below!")
        
        divider()
        
        // Interactive Greeter
        heading("🚀 Interactive Greeter", level = 2)
        text("Test the live action handler by entering your name:")
        
        val nameInput = textInput("What's your name?")
        val greetingOutput = textOutput("greeting-output")
        
        action("Say Hello 👋", "greet") {
            val name = read(nameInput)
            val greeting = if (name.isNotBlank()) {
                "Hello, $name! Welcome to Kotlio!"
            } else {
                "Hello! Please enter your name."
            }
            update(greetingOutput, greeting)
        }
        
        divider()
        
        // API Schema Viewer
        heading("📋 API Schema", level = 2)
        text("View the application schema to see all available components and actions:")
        
        val schemaOutput = textOutput("schema-output", monospace = true)
        action("View Schema", "fetch-schema") {
            val schemaJson = getSchemaJson()
            update(schemaOutput, schemaJson)
        }
        
        divider()
        
        // How It Works
        heading("💡 How It Works", level = 2)
        text("This server is running a Kotlio application defined in pure Kotlin code:")
        
        code("""
            runKotlioApp(port = 8080) {
                page("Welcome") {
                    val nameInput = textInput("What's your name?")
                    val greetingOutput = textOutput("greeting-output", "Greeting")
                    
                    action("Say Hello", "greet") {
                        val name = read(nameInput)
                        update(greetingOutput, "Hello, ${'$'}name! Welcome to Kotlio!")
                    }
                }
            }.start(wait = false)
        """, language = "kotlin")
        
        heading("Available API Endpoints:", level = 3)
        html("""
            <ul>
                <li><code>GET /schema</code> — Returns the application schema (JSON)</li>
                <li><code>POST /action</code> — Executes action handlers with input values</li>
                <li><code>GET /health</code> — Health check endpoint</li>
            </ul>
        """)
        
        heading("Key Features:", level = 3)
        html("""
            <ul>
                <li>✨ <strong>Pure Kotlin DSL</strong> — Define UIs in type-safe Kotlin code</li>
                <li>🚀 <strong>Embedded Server</strong> — No CLI tools required, just run your Kotlin app</li>
                <li>🌐 <strong>DOM-Native</strong> — JS client renders directly to browser DOM</li>
                <li>🔄 <strong>REST API</strong> — Simple HTTP endpoints for actions and schema</li>
                <li>⚡ <strong>Multiplatform</strong> — Shared code across JVM server and JS client</li>
                <li>🎨 <strong>Modern Theme</strong> — Dark mode support with CSS variables</li>
            </ul>
        """)
    }
}

/**
 * Creates a simple demo page showing basic Kotlio features.
 */
fun KotlioAppBuilder.simpleDemoPage() {
    page("Kotlio Demo") {
        heading("Welcome to Kotlio!", level = 1)
        text("Build interactive web UIs with pure Kotlin.")
        
        divider()
        
        heading("Try it out:", level = 2)
        val input = textInput("Your name")
        val output = textOutput("result")
        
        action("Greet") {
            val name = read(input)
            update(output, "Hello, $name!")
        }
        
        divider()
        
        heading("Learn more:", level = 3)
        text("Check out the documentation to build your own Kotlio app.")
        code("""
            fun main() {
                runKotlioApp {
                    page("My App") {
                        // Your components here
                    }
                }
            }
        """)
    }
}
