package kotlio.client

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlio.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Headers

/**
 * Main entry point for Kotlio client application.
 * Called automatically when the page loads.
 */
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    window.addEventListener("DOMContentLoaded", {
        GlobalScope.launch {
            try {
                // Fetch schema from server
                val response = window.fetch("/schema").await()
                if (!response.ok) {
                    showError("Failed to load application: ${response.status} ${response.statusText}")
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
                console.error("Kotlio initialization error:", e)
            }
        }
    })
}

private fun showError(message: String) {
    val app = document.getElementById("app") as? HTMLElement ?: return
    app.innerHTML = """
        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; gap: 1rem;">
            <div style="font-size: 2rem; color: var(--color-error);">⚠️</div>
            <div style="color: var(--color-text-primary); font-size: 1.25rem; text-align: center;">$message</div>
        </div>
    """.trimIndent()
}

@OptIn(DelicateCoroutinesApi::class)
class KotlioClient(
    private val baseUrl: String = "",
    private val scope: CoroutineScope = GlobalScope
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun mount(containerId: String, schema: KotlioSchema): KotlioDomBindings {
        val container = document.getElementById(containerId)
            ?: error("Mount point '$containerId' was not found in the DOM")
        return mount(container, schema)
    }

    fun mount(container: Node, schema: KotlioSchema): KotlioDomBindings {
        if (container is HTMLElement) {
            container.innerHTML = ""
        } else {
            while (container.hasChildNodes()) {
                container.firstChild?.let { container.removeChild(it) } ?: break
            }
        }

        val page = schema.pages.firstOrNull()
            ?: error("Kotlio schema must contain at least one page to render")

        val bindings = PageRenderer(scope, ::dispatchAction).render(page)
        container.appendChild(bindings.root)
        return bindings
    }

    private suspend fun dispatchAction(invocation: ActionInvocation, bindings: KotlioDomBindings) {
        try {
            val url = "$baseUrl/action"
            val body = json.encodeToString(invocation)
            
            val headers = Headers()
            headers.append("Content-Type", "application/json")
            
            val response = window.fetch(
                url,
                RequestInit(
                    method = "POST",
                    headers = headers,
                    body = body
                )
            ).await()

            if (!response.ok) {
                println("Action request failed: ${response.status} ${response.statusText}")
                return
            }

            val responseText = response.text().await()
            val actionResponse = json.decodeFromString<ActionResponse>(responseText)

            if (!actionResponse.success) {
                println("Action execution failed: ${actionResponse.error}")
                return
            }

            actionResponse.updates.forEach { (componentId, update) ->
                when (update.type) {
                    UpdateType.TEXT -> bindings.updateText(componentId, update.value)
                    UpdateType.LIST -> {
                        val items = json.decodeFromString<List<String>>(update.value)
                        bindings.updateList(componentId, items)
                    }
                }
            }
        } catch (e: Exception) {
            println("Failed to dispatch action: ${e.message}")
        }
    }
}

class KotlioDomBindings internal constructor(
    internal val root: HTMLElement,
    private val textOutputs: Map<String, HTMLElement>,
    private val listOutputs: Map<String, HTMLUListElement>
) {
    fun updateText(id: String, value: String) {
        val element = textOutputs[id]
            ?: error("No text output component with id '$id' is mounted")
        
        element.textContent = value
        
        // Show the output container when it gets content
        val container = element.parentElement as? HTMLElement
        container?.style?.display = "flex"
    }

    fun updateList(id: String, entries: List<String>) {
        val element = listOutputs[id]
            ?: error("No list output component with id '$id' is mounted")
        element.innerHTML = ""
        entries.forEach { value ->
            val item = document.createElement("li") as HTMLLIElement
            item.textContent = value
            element.appendChild(item)
        }
        
        // Show the output container when it gets content
        val container = element.parentElement as? HTMLElement
        container?.style?.display = "flex"
    }
}

private class PageRenderer(
    private val scope: CoroutineScope,
    private val dispatcher: suspend (ActionInvocation, KotlioDomBindings) -> Unit
) {
    private val textInputs = mutableMapOf<String, HTMLInputElement>()
    private val fileInputs = mutableMapOf<String, HTMLInputElement>()
    private val textOutputs = mutableMapOf<String, HTMLElement>()
    private val listOutputs = mutableMapOf<String, HTMLUListElement>()

    fun render(page: PageSchema): KotlioDomBindings {
        val wrapper = document.createElement("div") as HTMLDivElement
        wrapper.className = "kotlio-page"

        val title = document.createElement("h2") as HTMLElement
        title.textContent = page.title
        wrapper.appendChild(title)

        val componentsHost = document.createElement("div") as HTMLDivElement
        componentsHost.className = "kotlio-components"
        wrapper.appendChild(componentsHost)

        // Create bindings early so actions can reference them
        val bindings = KotlioDomBindings(wrapper, textOutputs, listOutputs)

        page.components.forEach { component ->
            val rendered = when (component.role) {
                ComponentRole.TEXT_INPUT -> renderTextInput(component)
                ComponentRole.FILE_INPUT -> renderFileInput(component)
                ComponentRole.TEXT_OUTPUT -> renderTextOutput(component)
                ComponentRole.LIST_OUTPUT -> renderListOutput(component)
                ComponentRole.ACTION -> renderActionComponent(component, bindings)
                ComponentRole.HEADING -> renderHeading(component)
                ComponentRole.TEXT -> renderText(component)
                ComponentRole.CODE -> renderCode(component)
                ComponentRole.DIVIDER -> renderDivider()
                ComponentRole.HTML -> renderHtml(component)
            }
            componentsHost.appendChild(rendered)
        }

        return bindings
    }

    private fun renderTextInput(component: kotlio.ComponentSchema): HTMLElement {
        val container = document.createElement("label") as HTMLElement
        container.className = "kotlio-input"

        component.label?.let { labelText ->
            val label = document.createElement("span") as HTMLElement
            label.textContent = labelText
            container.appendChild(label)
        }

        val input = document.createElement("input") as HTMLInputElement
        input.type = "text"
        input.className = "kotlio-text-input"
        container.appendChild(input)
        textInputs[component.id] = input
        return container
    }

    private fun renderFileInput(component: kotlio.ComponentSchema): HTMLElement {
        val container = document.createElement("label") as HTMLElement
        container.className = "kotlio-input"

        component.label?.let { labelText ->
            val label = document.createElement("span") as HTMLElement
            label.textContent = labelText
            container.appendChild(label)
        }

        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.className = "kotlio-file-input"
        if (component.accepts.isNotEmpty()) {
            input.accept = component.accepts.joinToString(",")
        }
        container.appendChild(input)
        fileInputs[component.id] = input
        return container
    }

    private fun renderTextOutput(component: kotlio.ComponentSchema): HTMLElement {
        val container = document.createElement("div") as HTMLDivElement
        container.className = "kotlio-output kotlio-text-output"
        container.style.display = "none"  // Hide until populated
        component.label?.let { labelText ->
            val label = document.createElement("span") as HTMLElement
            label.textContent = labelText
            label.className = "kotlio-output-label"
            container.appendChild(label)
        }
        val value = document.createElement("div") as HTMLDivElement
        value.className = "kotlio-output-value"
        
        // Apply monospace styling if specified
        if (component.monospace) {
            value.style.fontFamily = "var(--font-mono)"
            value.style.fontSize = "var(--font-size-sm)"
            value.style.whiteSpace = "pre-wrap"
            value.style.overflowX = "auto"
            value.style.maxHeight = "400px"
            value.style.overflowY = "auto"
        }
        
        container.appendChild(value)
        textOutputs[component.id] = value
        return container
    }

    private fun renderListOutput(component: kotlio.ComponentSchema): HTMLElement {
        val container = document.createElement("div") as HTMLDivElement
        container.className = "kotlio-output kotlio-list-output"
        container.style.display = "none"  // Hide until populated
        component.label?.let { labelText ->
            val label = document.createElement("span") as HTMLElement
            label.textContent = labelText
            label.className = "kotlio-output-label"
            container.appendChild(label)
        }
        val list = document.createElement("ul") as HTMLUListElement
        container.appendChild(list)
        listOutputs[component.id] = list
        return container
    }

    private fun renderHeading(component: ComponentSchema): HTMLElement {
        val level = component.level ?: 2
        val heading = document.createElement("h$level") as HTMLElement
        heading.textContent = component.content ?: ""
        return heading
    }

    private fun renderText(component: ComponentSchema): HTMLElement {
        val p = document.createElement("p") as HTMLElement
        p.textContent = component.content ?: ""
        return p
    }

    private fun renderCode(component: ComponentSchema): HTMLElement {
        val pre = document.createElement("pre") as HTMLElement
        val code = document.createElement("code") as HTMLElement
        code.textContent = component.content ?: ""
        component.language?.let { lang ->
            code.className = "language-$lang"
        }
        pre.appendChild(code)
        return pre
    }

    private fun renderDivider(): HTMLElement {
        return document.createElement("hr") as HTMLElement
    }

    private fun renderHtml(component: ComponentSchema): HTMLElement {
        val div = document.createElement("div") as HTMLDivElement
        div.innerHTML = component.content ?: ""
        return div
    }

    private fun renderActionComponent(component: ComponentSchema, bindings: KotlioDomBindings): HTMLElement {
        val button = document.createElement("button") as HTMLButtonElement
        button.className = "kotlio-action"
        button.textContent = component.label ?: "Action"
        val actionId = component.actionId ?: component.id
        button.addEventListener("click", {
            scope.launch {
                val invocation = ActionInvocation(actionId, gatherInputValues())
                runCatching { dispatcher(invocation, bindings) }
                    .onFailure { error -> println("Failed to dispatch Kotlio action: ${error.message}") }
            }
        })
        return button
    }

    private fun renderAction(action: ActionSchema, bindings: KotlioDomBindings): HTMLElement {
        val button = document.createElement("button") as HTMLButtonElement
        button.className = "kotlio-action"
        button.textContent = action.label
        button.addEventListener("click", {
            scope.launch {
                val invocation = ActionInvocation(action.id, gatherInputValues())
                runCatching { dispatcher(invocation, bindings) }
                    .onFailure { error -> println("Failed to dispatch Kotlio action: ${error.message}") }
            }
        })
        return button
    }

    private fun gatherInputValues(): Map<String, String> {
        val values = linkedMapOf<String, String>()
        textInputs.forEach { (id, input) ->
            values[id] = input.value
        }
        fileInputs.forEach { (id, input) ->
            val file = input.files?.item(0)
            values[id] = file?.name ?: ""
        }
        return values
    }
}
