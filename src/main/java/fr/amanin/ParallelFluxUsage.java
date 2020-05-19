package fr.amanin;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

/**
 * Simple example of Spring Reactor Parallel computing APIs. This is a very light introduction.
 * See {@link ParallelCoroutinesKt} for a Kotlin coroutine based example of the same work.
 */
public class ParallelFluxUsage {

    public static void main(String[] args) {
        Stream<String> messages =
                // Create a simple stream
                Flux.range(0, 10)
                // Activate parallelization of further computing
                .parallel()
                /* Drawback: still unclear to me: we have to force a scheduler that will properly distribute computing
                 * upon multiple threads. By default, a "work-stealing" scheduler is used to avoid as much as possible
                 * scattering of elements on different threads.
                 */
                .runOn(Schedulers.parallel())
                 // Launch computation
                .map(ParallelFluxUsage::compute)
                // Once computation is done, we specify that we want next operations to be executed sequentially.
                .sequential()
                // Bridge to standard java stream API (return to "blocking" world)
                .toStream();

        // Print computed messages
        final long count = messages
                .peek(message -> System.out.printf("Print on thread %s -> %s%n", Thread.currentThread(), message))
                .count();

        System.out.println("Total: "+count);
    }

    /**
     * Example function, print a number along with the thread in which the message is computed into.
     */
    private static String compute(final long nb) {
        return String.format(
                "From %s: number %d",
                Thread.currentThread(), nb
        );
    }
}
