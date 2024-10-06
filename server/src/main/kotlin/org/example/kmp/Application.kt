package org.example.kmp

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowHost("localhost:8081")
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Put)
    }
    install(ContentNegotiation) {
        json()
    }

    val listWondons = mutableListOf(
        Wondon(name = "Wondon1", age = 1)
    )

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/wondon") {
            call.respond(HttpStatusCode.OK, Json.encodeToString(Wondon.serializer(), listWondons[0]))
        }

        get("/wondons") {
            call.respond(HttpStatusCode.OK, Json.encodeToString(ListSerializer(Wondon.serializer()), listWondons))
        }

        put("/wondons") {
            val wondon = call.receive<Wondon>()
            listWondons.add(wondon)
            call.respond(HttpStatusCode.Created, "Wondon added successfully")
        }
    }
}
