package kotlio

fun kotlioApp(configure: KotlioAppBuilder.() -> Unit): KotlioApp =
    KotlioAppBuilder().apply(configure).build()

class KotlioAppBuilder internal constructor() {
    private val pages = mutableListOf<PageBuilder>()

    fun page(title: String, build: PageBuilder.() -> Unit) {
        pages += PageBuilder(title).apply(build)
    }

    internal fun build(): KotlioApp {
        val pageSchemas = pages.map { it.toSchema() }
        val actionMap = pages.flatMap { it.actions }.associateBy { it.schema.id }
        return KotlioApp(
            schema = KotlioSchema(pageSchemas),
            actions = actionMap
        )
    }
}

class PageBuilder internal constructor(private val title: String) {
    private val components = mutableListOf<ComponentSchema>()
    internal val actions = mutableListOf<ActionDefinition>()

    fun textInput(label: String = "", id: String = ComponentIdGenerator.next("textInput")): InputHandle<String> {
        val identifier = ensureUnique(id)
        components += ComponentSchema(identifier, ComponentRole.TEXT_INPUT, normalizeLabel(label))
        return InputHandle(identifier)
    }

    fun fileInput(
        label: String = "",
        accepts: List<String> = listOf("*/*"),
        id: String = ComponentIdGenerator.next("fileInput")
    ): InputHandle<FileReference> {
        val identifier = ensureUnique(id)
        components += ComponentSchema(identifier, ComponentRole.FILE_INPUT, normalizeLabel(label), accepts)
        return InputHandle(identifier)
    }

    fun textOutput(id: String, label: String = "", monospace: Boolean = false): OutputHandle<String> {
        val identifier = ensureUnique(id)
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.TEXT_OUTPUT,
            label = normalizeLabel(label),
            monospace = monospace
        )
        return OutputHandle(identifier)
    }

    fun listOutput(id: String, label: String = ""): OutputHandle<List<JsonElementRef>> {
        val identifier = ensureUnique(id)
        components += ComponentSchema(identifier, ComponentRole.LIST_OUTPUT, normalizeLabel(label))
        return OutputHandle(identifier)
    }

    fun action(label: String, id: String = ComponentIdGenerator.next("action"), handler: ActionHandler) {
        val identifier = ensureUnique(id)
        
        // Add action to actions map for handler execution
        actions += ActionDefinition(
            ActionSchema(identifier, normalizeLabel(label) ?: label),
            handler
        )
        
        // Also add as a component so it renders inline
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.ACTION,
            label = normalizeLabel(label) ?: label,
            actionId = identifier
        )
    }
    
    // Static content components
    
    fun heading(text: String, level: Int = 2, id: String = ComponentIdGenerator.next("heading")) {
        val identifier = ensureUnique(id)
        require(level in 1..6) { "Heading level must be between 1 and 6" }
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.HEADING,
            content = text,
            level = level
        )
    }
    
    fun text(content: String, id: String = ComponentIdGenerator.next("text")) {
        val identifier = ensureUnique(id)
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.TEXT,
            content = content
        )
    }
    
    fun code(content: String, language: String = "kotlin", id: String = ComponentIdGenerator.next("code")) {
        val identifier = ensureUnique(id)
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.CODE,
            content = content.trimIndent(),
            language = language
        )
    }
    
    fun divider(id: String = ComponentIdGenerator.next("divider")) {
        val identifier = ensureUnique(id)
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.DIVIDER
        )
    }
    
    fun html(content: String, id: String = ComponentIdGenerator.next("html")) {
        val identifier = ensureUnique(id)
        components += ComponentSchema(
            id = identifier,
            role = ComponentRole.HTML,
            content = content
        )
    }

    internal fun toSchema(): PageSchema = PageSchema(title, components.toList(), actions.map { it.schema })

    private fun ensureUnique(id: String): String {
        val exists = components.any { it.id == id } || actions.any { it.schema.id == id }
        require(!exists) { "Duplicate component id '$id' on page '$title'" }
        return id
    }

    private fun normalizeLabel(label: String): String? = label.takeIf { it.isNotBlank() }
}

object ComponentIdGenerator {
    private var counter: Int = 0

    fun next(prefix: String): String = "$prefix-${counter++}".replace(" ", "-")
}

class InputHandle<T> internal constructor(internal val id: String) {
    operator fun invoke(): T = error(
        "Runtime context is not available to read from handle '$id'. Ensure you're executing within a Kotlio runtime."
    )
}

class OutputHandle<T> internal constructor(internal val id: String) {
    suspend fun update(value: T): Nothing = error(
        "Runtime context is not available to update handle '$id'. Ensure you're executing within a Kotlio runtime."
    )
}

data class FileReference(val name: String, val sizeBytes: Long)

data class JsonElementRef(val json: String)
