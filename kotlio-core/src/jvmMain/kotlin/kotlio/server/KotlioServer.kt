package kotlio.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlio.*
import kotlinx.serialization.json.Json
import java.io.File

private val defaultJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun runKotlioApp(
    port: Int = 7860,
    host: String = "0.0.0.0",
    configure: KotlioAppBuilder.() -> Unit
): ApplicationEngine {
    val appDefinition: KotlioApp = kotlioApp(configure)
    return embeddedServer(Netty, port = port, host = host) {
        install(ContentNegotiation) {
            json(defaultJson)
        }
        routing {
            // Define specific API routes first (they take precedence)
            get("/schema") {
                call.respond(appDefinition.schema)
            }
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }
            post("/action") {
                val invocation = call.receive<ActionInvocation>()
                val actionDef = appDefinition.actions[invocation.id]
                
                if (actionDef == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ActionResponse(
                            success = false,
                            error = "Action '${invocation.id}' not found"
                        )
                    )
                    return@post
                }
                
                try {
                    val context = ActionContextFactory.create(invocation.inputs, appDefinition.schema)
                    actionDef.handler.invoke(context)
                    val updates = context.collectUpdates()
                    call.respond(ActionResponse(success = true, updates = updates))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ActionResponse(
                            success = false,
                            error = e.message ?: "Unknown error occurred"
                        )
                    )
                }
            }
            
            // Serve static resources last (includes index.html at /)
            // This catches all unmatched routes and serves from classpath
            staticResources("/", "kotlio/static")
        }
    }.apply { start(wait = false) }
}
