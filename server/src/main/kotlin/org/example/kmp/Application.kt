package org.example.kmp

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowHost("localhost:8081")
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
    }
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/wondons") {
            log.info("Received request for /wondons")
            try {
                val wondons = listOf("Wondon A", "Wondon B", "Wondon C")
                call.respond(mapOf("wondons" to wondons))
            } catch (e: Exception) {
                log.error("Error handling /wondons request", e)
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            }
        }
    }
}