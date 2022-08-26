package fr.amanin.stackoverflow;

import reactor.core.publisher.Mono;

import java.time.Duration;

public class WhyMonoRunnableIsNotVoid {

    public static void main(String[] args) {
        Mono<String> myMono = Mono.empty();
        myMono = myMono.switchIfEmpty(Mono.fromRunnable(()
                -> System.err.println("WARNING, empty signal !")));

        String value = myMono.block(Duration.ofSeconds(1));
        System.out.println("Exported value is "+value);
    }
}
