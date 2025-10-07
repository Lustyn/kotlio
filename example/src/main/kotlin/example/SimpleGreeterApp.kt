package example

import kotlio.*
import kotlio.server.runKotlioApp

/**
 * A simple example demonstrating the Kotlio library.
 * 
 * This app creates a greeting page where users can:
 * 1. Enter their name
 * 2. Click a button to generate a greeting
 * 3. See the result displayed on the page
 * 
 * To run:
 * 1. Execute this main function
 * 2. Open http://localhost:8080 in your browser
 * 3. The JS client will fetch the schema and render the UI
 */
fun main() {
    println("Starting Kotlio example server...")
    
    runKotlioApp(port = 8080) {
        // Use the built-in example page with documentation
        examplePage()
    }
    
    println()
    println("=".repeat(60))
    println("Kotlio Example Server Started!")
    println("=".repeat(60))
    println("Open your browser to: http://localhost:8080")
    println("API Endpoints:")
    println("  - GET  /schema  -> App schema (JSON)")
    println("  - POST /action  -> Execute actions")
    println("  - GET  /health  -> Health check")
    println("=".repeat(60))
    println("Press Ctrl+C to stop the server")
    println()
    
    // Keep the main thread alive
    Thread.currentThread().join()
}
