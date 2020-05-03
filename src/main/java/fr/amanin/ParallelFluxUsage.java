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
        Stream<String> messages = Flux.range(0, 100)
                .parallel(4)
                .runOn(Schedulers.parallel())
                .map(ParallelFluxUsage::compute)
                .sequential()
                .toStream();

        final long count = messages
                .peek(message -> System.out.printf("Print on thread %s -> %s%n", Thread.currentThread(), message))
                .count();

        System.out.println("Total: "+count);
    }

    private static String compute(final long nb) {
        return String.format(
                "From %s: number %d",
                Thread.currentThread(), nb
        );
    }
}
