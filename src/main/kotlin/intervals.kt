package fr.amanin

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

fun main() {
    val start = System.nanoTime()
    val handle = Flux.interval(Duration.ofSeconds(2), Schedulers.newSingle("toto"))
        .map { Thread.sleep(1000) ; System.nanoTime() - start }
        .subscribe { println(it * 1e-6) }

    Thread.sleep(30000)

    handle.dispose()
}