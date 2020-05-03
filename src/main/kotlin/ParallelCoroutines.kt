package fr.amanin

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


/**
 * Similar to [Reactor example][fr.amanin.ParallelFluxUsage], except it uses coroutines instead of reactive streams.
 */
fun main() {
    /* Start by defining execution contexts. These objects define how tasks are "dispatched". That means that a chosen
     * dispatcher is in charge to schedule and execute given asynchronous tasks on thread pools. Here, we choose:
     *  - Default dispatcher will parallelize tasks on a common thread-pool (like Java Common-fork-join pool).
     *  - Unconfined is a special dispatcher that re-uses caller thread. Here, we use it to give work to main thread.
     *
     * Note: Kotlin coroutine "Dispatchers" are like Reactor "Schedulers".
     */
    val parallelContext = Dispatchers.Default
    val sequentialContext = Dispatchers.Unconfined

    /* Special construct: Specify that all code inside runBlocking is launched asynchronously in coroutines, but main
     * thread will wait all coroutines are over before continuation.
     * What it really does is using current (main here) thread for coroutine scheduling, to avoid only blocking until
     * completion.
     */
    runBlocking {
        for (i in 0 until 100) {
            // Asynchronously compute a value. the return value is a future holding computing state.
            val promise = async(parallelContext) {
                compute(i)
            }

            // Start a new coroutine running sequentially on main thread
            launch(sequentialContext) {
                // Pauses current coroutine until promise result have been computed. The thread is not waiting. A
                // continuation is scheduled, then the thread is immediately released. It allows it to run another
                // task ready for processing, if any.
                val message = promise.await()
                // Magically resume processing at this line once the above coroutine work is done.
                println("Print on thread ${Thread.currentThread()} -> $message")
            }
        }
    }

    println("TADA !")
}


fun compute(nb : Number) = "From ${Thread.currentThread()}: number ${nb}"