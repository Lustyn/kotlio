package kotlio

import kotlinx.coroutines.runBlocking
import kotlio.server.runKotlioApp
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTest {
    
    @Test
    fun testActionInvocationFlow() = runBlocking {
        val nameInput = InputHandle<String>("name")
        val greeting = OutputHandle<String>("greeting")
        
        val app = kotlioApp {
            page("Greeter") {
                textInput("Your Name", "name")
                textOutput("greeting", "Greeting")
                action("Greet", "greet-action") {
                    val name = read(nameInput)
                    update(greeting, "Hello, $name!")
                }
            }
        }
        
        // Verify schema contains expected components
        val page = app.schema.pages.first()
        assertEquals("Greeter", page.title)
        assertEquals(3, page.components.size) // input + output + action (now actions are components too)
        assertEquals(1, page.actions.size)
        
        // Verify action handler is registered
        val actionDef = app.actions["greet-action"]
        assertEquals("Greet", actionDef?.schema?.label)
        
        // Simulate action invocation
        val context = ActionContextFactory.create(mapOf("name" to "Kotlin"))
        actionDef?.handler?.invoke(context)
        
        val updates = context.collectUpdates()
        assertEquals(1, updates.size)
        assertEquals("Hello, Kotlin!", updates["greeting"]?.value)
        assertEquals(UpdateType.TEXT, updates["greeting"]?.type)
    }
    
    @Test
    fun testServerStartsSuccessfully() {
        val server = runKotlioApp(port = 0) { // Port 0 = let OS choose available port
            page("Test Page") {
                textInput("Input")
                textOutput("output")
                action("Submit") {
                    update(OutputHandle("output"), "Test")
                }
            }
        }
        
        try {
            // Verify server is running
            Thread.sleep(500) // Give server time to start
            // In a real test, you'd make HTTP requests here
        } finally {
            server.stop(1000, 2000)
        }
    }
}
