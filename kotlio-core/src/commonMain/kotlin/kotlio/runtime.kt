package kotlio

class ActionContext internal constructor(
    private val inputValues: Map<String, String>,
    private val updates: MutableMap<String, ComponentUpdate> = mutableMapOf(),
    internal val schema: KotlioSchema? = null
) {

    fun <T> read(handle: InputHandle<T>): T {
        val rawValue = inputValues[handle.id]
            ?: error("No input value provided for '${handle.id}'.")
        @Suppress("UNCHECKED_CAST")
        return rawValue as T
    }
    
    /**
     * Returns the application schema as a JSON string.
     * Useful for debugging or displaying schema information.
     */
    fun getSchemaJson(): String {
        return schema?.let { 
            kotlinx.serialization.json.Json.encodeToString(KotlioSchema.serializer(), it) 
        } ?: "{}"
    }

    suspend fun <T> update(handle: OutputHandle<T>, value: T) {
        val update = when (value) {
            is String -> ComponentUpdate(UpdateType.TEXT, value)
            is List<*> -> {
                val jsonArray = (value as List<*>).joinToString(",", "[", "]") { item ->
                    when (item) {
                        is JsonElementRef -> item.json
                        is String -> "\"${item.replace("\"", "\\\"")}\""
                        else -> "\"$item\""
                    }
                }
                ComponentUpdate(UpdateType.LIST, jsonArray)
            }
            else -> error("Unsupported output type for handle '${handle.id}': ${value?.let { it::class.simpleName }}")
        }
        updates[handle.id] = update
    }

    internal fun collectUpdates(): Map<String, ComponentUpdate> = updates.toMap()
}

internal data class StateBinding<T>(
    val reader: (() -> T)? = null,
    val writer: (suspend (T) -> Unit)? = null
)

internal object ActionContextFactory {
    fun create(inputValues: Map<String, String>, schema: KotlioSchema? = null): ActionContext = 
        ActionContext(inputValues, mutableMapOf(), schema)
}
