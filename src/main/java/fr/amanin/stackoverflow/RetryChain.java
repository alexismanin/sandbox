package fr.amanin.stackoverflow;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Work on <a href="https://stackoverflow.com/questions/79273364/multiple-retry-spec-mutually-resets-each-other-how-to-avoid-this">combining multiple retry specs</a>
 */
public class RetryChain {

    static class Err1 extends RuntimeException {}
    static class Err2 extends RuntimeException {}

    public static Mono<String> withErrors(RuntimeException... errors) {
        final AtomicInteger i = new AtomicInteger();
        return Mono.defer(() -> {
            var cursor = i.getAndIncrement();
            return (cursor < errors.length)
                    ? Mono.error(errors[cursor])
                    : Mono.just("SUCCESS !");
        });
    }

    public static void main(String[] args) {
        var flow = withErrors(new Err1(), new Err1(), new Err1(), new Err2(), new Err1())
                .retryWhen(new RetryEither(
                        Retry.fixedDelay(3L, Duration.ofSeconds(1)).filter(Err1.class::isInstance),
                        Retry.fixedDelay(1L, Duration.ofSeconds(5)).filter(Err2.class::isInstance)
                ));

        var result = flow.block();

        System.out.println("==== RESULT ====");
        System.out.println(result);
        System.out.println("================");
    }

    /**
     * A retry strategy composed of multiple sub-strategies, one for each type of error.
     * This is different from chaining multiple "retryWhen" operators upon a flux, as sub-strategies does not reset
     * each other counter and delays.
     * <br/>
     * <em>WARNING</em>: does <em>NOT</em> work with "maxRetriesInARow" criterion.
     */
    static final class RetryEither extends Retry {
        private final List<Retry> retries;
        private final Retry fallback;

        RetryEither(Retry... retries) {
            Retry tmpFallback = null;
            int fallbackIdx = -1;
            for (int i = 0 ; i < retries.length ; i++) {
                if (getFilter(retries[i]) == null) {
                    if (tmpFallback != null) throw new IllegalArgumentException("Only one retry without filter is accepted");
                    tmpFallback = retries[i];
                    fallbackIdx = i;
                }
            }

            var tmpRetries = new ArrayList<>(Arrays.asList(retries));
            if (tmpFallback != null) tmpRetries.remove(fallbackIdx);

            this.retries = Collections.unmodifiableList(tmpRetries);
            this.fallback = tmpFallback == null ? RetrySpec.max(0) : tmpFallback;
        }

        private Predicate<Throwable> getFilter(Retry spec) {
            if (spec instanceof RetrySpec rs) return rs.errorFilter;
            else if (spec instanceof RetryBackoffSpec rbs) return rbs.errorFilter;
            else return null;
        }

        @Override
        public Publisher<?> generateCompanion(Flux<RetrySignal> retrySignals) {
            var dispatchedSignals = retrySignals
                    .groupBy(this::findMatching)
                    .map(group -> group.transform(this::resetCounter)
                                       .transform(group.key()::generateCompanion));

            return Flux.merge(dispatchedSignals);
        }

        private Retry findMatching(RetrySignal signal) {
            return retries.stream()
                          .filter(r -> getFilter(r).test(signal.failure()))
                          .findFirst()
                          .orElse(fallback);
        }

        private Flux<RetrySignal> resetCounter(Flux<RetrySignal> signals) {
            AtomicLong counter = new AtomicLong();
            return signals.map(s -> new RetrySignalUsingOverridenCount(counter.getAndIncrement(), s));
        }
    }

    private record RetrySignalUsingOverridenCount(long newTotalRetries, Retry.RetrySignal source) implements Retry.RetrySignal {

        @Override
        public long totalRetries() {
            return newTotalRetries;
        }

        @Override
        public long totalRetriesInARow() {
            return newTotalRetries;
        }

        @Override
        public Throwable failure() {
            return source.failure();
        }

        @Override
        public ContextView retryContextView() {
            return source.retryContextView();
        }

        @Override
        public Retry.RetrySignal copy() {
            return new RetrySignalUsingOverridenCount(newTotalRetries, source.copy());
        }
    }
}
