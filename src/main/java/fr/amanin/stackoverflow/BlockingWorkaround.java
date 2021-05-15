package fr.amanin.stackoverflow;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Example on how to work with blocking calls in Reactor.
 * Serves as response for: <a href="https://stackoverflow.com/questions/67534602">this SO question</a>
 */
public class BlockingWorkaround {

    public static void main(String[] args) throws Exception {
        System.out.println("Ok context: not running from reactor Threads");
        System.out.println("value is "+blockingFunction());

        System.out.println("Problematic stack: working with scheduler not compatible with blocking call");
        executeAndWait(() -> blockingFunction());

        System.out.println("Bad way to subscribe on a blocking compatible scheduler");
        executeAndWait(() -> blockingFunctionUsingSubscribeOn());

        System.out.println("Bad way to publish on blocking compatible scheduler");
        executeAndWait(() -> blockingFunctionUsingPublishOn());

        System.out.println("Possible workaround: share the reactive stream before blocking on it");
        executeAndWait(() -> blockingFunctionShared());

        System.out.println("Right way to subscribe on blocking compatible scheduler");
        subscribeOnAndWait(() -> blockingFunction());

        System.out.println("Right way to publish on blocking compatible scheduler");
        publishOnAndWait(() -> blockingFunction());
    }

    static Boolean blockingFunction() {
        return delay()
                .flatMap(delay -> Mono.just(true))
                .block();
    }

    static Boolean blockingFunctionShared() {
        return delay()
                .flatMap(delay -> Mono.just(true))
                .share() // Mono result is cached internally
                .block();
    }

    static Boolean blockingFunctionUsingSubscribeOn() {
        return delay()
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(delay -> Mono.just(true))
                .block();
    }

    static Boolean blockingFunctionUsingPublishOn() {
        return delay()
                .flatMap(delay -> Mono.just(true))
                .publishOn(Schedulers.boundedElastic())
                .block();
    }

    static Mono<Long> delay() {
        return Mono.delay(Duration.ofMillis(10));
    }

    private static void executeAndWait(Supplier<Boolean> blockingAction) throws InterruptedException {
        delay()
                .map(it -> blockingAction.get())
                .subscribe(
                        val -> System.out.println("It worked"),
                        err -> System.out.println("ERROR: " + err.getMessage())
                );

        Thread.sleep(100);
    }

    private static void subscribeOnAndWait(Callable<Boolean> blockingAction) throws InterruptedException {
        final Mono<Boolean> blockingMono = Mono.fromCallable(blockingAction)
                .subscribeOn(Schedulers.boundedElastic()); // Upstream is executed on given scheduler

        delay()
                .flatMap(it -> blockingMono)
                .subscribe(
                        val -> System.out.println("It worked"),
                        err -> System.out.println("ERROR: " + err.getMessage())
                );

        Thread.sleep(100);
    }

    private static void publishOnAndWait(Supplier<Boolean> blockingAction) throws InterruptedException {
        delay()
                .publishOn(Schedulers.boundedElastic()) // Cause downstream to be executed on given scheduler
                .map(it -> blockingAction.get())
                .subscribe(
                        val -> System.out.println("It worked"),
                        err -> System.out.println("ERROR: " + err.getMessage())
                );

        Thread.sleep(100);
    }
}
