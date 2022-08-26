package fr.amanin.stackoverflow

import io.ktor.application.*
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty

fun main() {
    val server = embeddedServer(Jetty, 8080) {
        routing {
            get("/hello") {
                call.respondText { "hello" }
            }
        }
    }
    server.start(wait = true)
}