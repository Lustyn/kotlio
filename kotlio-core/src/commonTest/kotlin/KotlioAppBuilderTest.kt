import kotlio.ComponentRole
import kotlio.kotlioApp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlioAppBuilderTest {

    @Test
    fun buildsSchemaForSimplePage() {
        val app = kotlioApp {
            page("Demo") {
                val prompt = textInput(label = "Prompt")
                textOutput(id = "answer", label = "Answer")
                action(label = "Submit") {
                    // no-op for now
                }
            }
        }

        val schema = app.schema
        assertEquals(1, schema.pages.size)
        val page = schema.pages.single()
        assertEquals("Demo", page.title)
        assertEquals(3, page.components.size) // input + output + action
        assertTrue(page.components.any { it.id.startsWith("textInput") && it.role == ComponentRole.TEXT_INPUT })
        assertTrue(page.components.any { it.id == "answer" && it.role == ComponentRole.TEXT_OUTPUT })
        assertTrue(page.components.any { it.role == ComponentRole.ACTION && it.label == "Submit" })
        assertEquals(1, page.actions.size)
        assertEquals("Submit", page.actions.single().label)
    }
}
