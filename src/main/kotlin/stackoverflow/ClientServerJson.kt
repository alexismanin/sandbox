package fr.amanin.stackoverflow

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.tcp.TcpClient
import reactor.netty.tcp.TcpServer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

val SERV_LOG = Logger.getLogger("server")
val CLI_LOG = Logger.getLogger("client")

fun time() = System.currentTimeMillis()

fun main() {
    val s1 = Server(9000).start()
    val s2 = Server(9001).start()
    val s3 = Server(9002).start()
    val s4 = Server(9003).start()

//    val client = BlockingClient()
    val client = ReactiveClient()
    client.connect(9000, 9001, 9002, 9003)

    client.shutdown(Duration.ofSeconds(30))

    s1.dispose()
    s2.dispose()
    s3.dispose()
    s4.dispose()
}

@Serializable
data class Payload(val idx: Long, val emittedAt: Long, val port: Int)

class Server(val port: Int) {

    fun start() = TcpServer.create().port(port)
        .handle { _, output -> output.sendString(dataStream()) }
        .bind()
        .subscribe( { }, { error -> SERV_LOG.log(Level.WARNING, "Server failed to bind", error) } )

    fun dataStream() = Flux.interval(Duration.ofMillis(1))
        .map { Payload(it, time(), port) }
        .map { Json.encodeToString(it) + '\n' }
        .doOnError { SERV_LOG.log(Level.WARNING, "Failure while producing messages", it) }
}

interface Client {
    fun connect(port : Int)
    fun cancel(port : Int)
    fun shutdown(timeout : Duration)
}

fun Client.connect(vararg ports : Int) {
    for (port in ports) connect(port)
}

class BlockingClient : Client {
    val executor = Executors.newCachedThreadPool();

    private var cancelled = ConcurrentHashMap<Int, Boolean>();
    override fun connect(port: Int) {
        executor.submit {
            try {
                Socket("localhost", port).use { socket ->
                    InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8).use {
                        BufferedReader(it).use {
                            var line: String?
                            do {
                                line = it.readLine()
                                CLI_LOG.info(line)
                            } while (line != null && !isCancelled(port))
                        }
                    }
                }
            } catch (e : Exception) { CLI_LOG.log(Level.WARNING, "Client failed", e) }
        }
    }

    fun isCancelled(port: Int) = cancelled.get(port) ?: false || Thread.currentThread().isInterrupted

    override fun cancel(port: Int) {
        cancelled[port] = true;
    }

    override fun shutdown(timeout: Duration) {
        executor.shutdown()
        executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)
        executor.shutdownNow()
    }
}

class ReactiveClient : Client {
    val connections = ConcurrentHashMap<Int, Connection>()

    override fun connect(port: Int) {
        TcpClient.create().port(port)
            .connect()
            .subscribe { c ->
                connections.put(port, c)
                c.inbound().receive()
                    .asString(StandardCharsets.UTF_8)
                    .subscribe { CLI_LOG.info(it) }
            }
    }

    override fun cancel(port: Int) {
        connections[port]?.dispose()
    }

    override fun shutdown(timeout: Duration) {
        Mono.delay(timeout).map { connections.values.forEach { it.dispose() } }.block()
    }
}