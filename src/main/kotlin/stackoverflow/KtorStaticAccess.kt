package fr.amanin.stackoverflow

import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.application.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

// Related SO answer: https://stackoverflow.com/a/61898001/2678097

/**
 * Thread local in which you'll inject application call.
 */
private val localCall : ThreadLocal<ApplicationCall> = ThreadLocal();

object Main {

    fun start() {
        val server = embeddedServer(Jetty, 8081) {
            routing {
                // Solution requiring full coroutine/ supendable execution.
                get("/async") {
                    // Ktor will launch this block of code in a coroutine, so you can create a subroutine with
                    // an overloaded context providing needed information.
                    launch(coroutineContext + ApplicationCallContext(call)) {
                        PrintQuery.processAsync()
                    }
                }

                // Solution based on Thread-Local, not requiring suspending functions
                get("/blocking") {
                    launch (coroutineContext + localCall.asContextElement(value = call)) {
                        PrintQuery.processBlocking()
                    }
                }
            }

            intercept(ApplicationCallPipeline.ApplicationPhase.Call) {
                call.respondText("Hé ho", ContentType.Text.Plain, HttpStatusCode.OK)
            }
        }
        server.start(wait = true)
    }
}

fun main() {
    Main.start();
}

interface AsyncAddon {
    /**
     * Asynchronicity propagates in order to properly access coroutine execution information
     */
    suspend fun processAsync();
}

interface BlockingAddon {
    fun processBlocking();
}

object PrintQuery : AsyncAddon, BlockingAddon {
    override suspend fun processAsync() = processRequest("async", fetchCurrentCallFromCoroutineContext())

    override fun processBlocking() = processRequest("blocking", fetchCurrentCallFromThreadLocal())

    private fun processRequest(prefix : String, call : ApplicationCall?) {
        println("$prefix -> Query parameter: ${call?.parameters?.get("q") ?: "NONE"}")
    }
}

/**
 * Custom coroutine context allow to provide information about request execution.
 */
private class ApplicationCallContext(val call : ApplicationCall) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ApplicationCallContext>
}

/**
 * This is your RequestUtils rewritten as a first-order function. It defines as asynchronous.
 * If not, you won't be able to access coroutineContext.
 */
suspend fun fetchCurrentCallFromCoroutineContext(): ApplicationCall? {
    // Here is where I am getting lost..
    return coroutineContext.get(ApplicationCallContext.Key)?.call
}

fun fetchCurrentCallFromThreadLocal() : ApplicationCall? {
    return localCall.get()
}