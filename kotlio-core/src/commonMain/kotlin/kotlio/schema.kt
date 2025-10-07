package kotlio

import kotlinx.serialization.Serializable
import kotlin.ConsistentCopyVisibility

@Serializable
data class KotlioSchema(
    val pages: List<PageSchema>
)

@Serializable
data class PageSchema(
    val title: String,
    val components: List<ComponentSchema>,
    val actions: List<ActionSchema>
)

@Serializable
data class ComponentSchema(
    val id: String,
    val role: ComponentRole,
    val label: String? = null,
    val accepts: List<String> = emptyList(),
    // Fields for static content components
    val content: String? = null,
    val level: Int? = null,  // For headings (1-6)
    val language: String? = null,  // For code blocks
    // Field for action component
    val actionId: String? = null,  // Links to action handler
    // Field for output display style
    val monospace: Boolean = false  // For code/JSON output
)

@Serializable
enum class ComponentRole {
    // Interactive components
    TEXT_INPUT,
    FILE_INPUT,
    TEXT_OUTPUT,
    LIST_OUTPUT,
    ACTION,
    // Static content components
    HEADING,
    TEXT,
    CODE,
    DIVIDER,
    HTML
}

@Serializable
data class ActionSchema(
    val id: String,
    val label: String
)

@ConsistentCopyVisibility
data class KotlioApp internal constructor(
    val schema: KotlioSchema,
    internal val actions: Map<String, ActionDefinition>
)

internal data class ActionDefinition(
    val schema: ActionSchema,
    val handler: ActionHandler
)

typealias ActionHandler = suspend ActionContext.() -> Unit

@Serializable
data class ActionInvocation(
    val id: String,
    val inputs: Map<String, String>
)

@Serializable
data class ActionResponse(
    val success: Boolean,
    val updates: Map<String, ComponentUpdate> = emptyMap(),
    val error: String? = null
)

@Serializable
data class ComponentUpdate(
    val type: UpdateType,
    val value: String
)

@Serializable
enum class UpdateType {
    TEXT,
    LIST
}
