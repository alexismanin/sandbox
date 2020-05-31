package fr.amanin.stackoverflow

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.stringify
import reactor.core.publisher.Flux
import reactor.netty.tcp.TcpServer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val json = Json(JsonConfiguration.Default)

fun time() = System.currentTimeMillis()

@ImplicitReflectionSerializer
fun main() {
    val s1 = Server(9000).start()
    val s2 = Server(9001).start()
    val s3 = Server(9002).start()
    val s4 = Server(9003).start()

    val executor = Executors.newFixedThreadPool(4)

    executor.submit( { NIOClient(9000).connect() } )
    executor.submit( { NIOClient(9001).connect() } )
    executor.submit( { NIOClient(9002).connect() } )
    executor.submit( { NIOClient(9003).connect() } )

    executor.shutdown()
    executor.awaitTermination(30, TimeUnit.SECONDS)
    executor.shutdownNow()

    s4.onDispose().block()
}

@Serializable
data class Payload(val idx: Long, val emittedAt: Long, val port: Int)

@ImplicitReflectionSerializer
class Server(val port: Int) {

    fun start() = TcpServer.create().port(port)
        .handle { _, output -> output.sendString(dataStream()) }
        .bindNow()

    fun dataStream() = Flux.interval(Duration.ofMillis(2))
        .map { Payload(it, time(), port) }
        .map { json.stringify(it) + '\n' }
}

class NIOClient(val port: Int) {

    private var cancelled = false
    fun connect() {
        Socket("localhost", port).use {socket ->
            InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8).use {
                BufferedReader(it).use {
                    var line : String
                    do {
                        line = it.readLine()
                        print(line)
                    } while (line != null && !isCancelled())
                }
            }
        }
    }

    fun isCancelled() = cancelled || Thread.currentThread().isInterrupted

    fun cancel() {
        cancelled = true
    }
}

