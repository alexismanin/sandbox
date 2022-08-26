package fr.amanin.stackoverflow

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = test(true)

fun test(withDelay: Boolean) {
    runBlocking {

        val flow = callbackFlow {
            send("value")
            awaitClose { println("CLOSED") }
            if (withDelay) delay(100)
        }

        val firstValue = flow.first()
        println(firstValue)

    }
}