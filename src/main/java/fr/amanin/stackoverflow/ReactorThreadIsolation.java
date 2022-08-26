package fr.amanin.stackoverflow;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.Loggers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Reproduce https://stackoverflow.com/questions/73467845/how-can-this-project-reactor-behavior-explained
 */
public class ReactorThreadIsolation {

    public static void main(String[] args) throws InterruptedException {
        Loggers.useConsoleLoggers();
        launch().await(5, TimeUnit.MINUTES);
    }

    private static CountDownLatch launch() {
        Consumer<String> slowConsumer = x -> {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch(Exception ignore) {
                System.err.println("INTERRUPTED !");
            }
        };
        Flux<String> publisher = Flux
                .just(1, 2, 3, 4)
                .parallel(2)
                .runOn(Schedulers.newParallel("writer", 2, true))
                .flatMap(rail -> Flux.range(1, 300)
                        .map(i -> {
                            String msg = String.format("Rail %d -> Row %d", rail, i);
                            System.out.println("[PRINT] ("+Thread.currentThread()+") "+msg);
                            return msg;
                        })
                        .log())
                .sequential()
                .publishOn(Schedulers.newSingle("reader", true))
                .doOnNext(slowConsumer);

        final CountDownLatch barrier = new CountDownLatch(1);
        publisher.subscribe(
                value -> {},
                err -> { err.printStackTrace() ; barrier.countDown(); },
                () -> barrier.countDown()
        );
        return barrier;
    }
}
