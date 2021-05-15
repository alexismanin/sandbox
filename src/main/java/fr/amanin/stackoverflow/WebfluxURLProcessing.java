package fr.amanin.stackoverflow;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WebfluxURLProcessing {

    private static final Logger LOGGER = Logger.getLogger("example");

    public static void main(String[] args) {

        final List<String> urls = Arrays.asList("https://www.google.com", "https://kotlinlang.org/kotlin/is/wonderful/", "https://stackoverflow.com", "http://doNotexists.blabla");

        final Flux<ExchangeDetails> events = Flux.fromIterable(urls)
                // unwrap request async operations
                .flatMap(url -> request(url))
                // Add a side-effect to log results
                .doOnNext(details -> log(details))
                // Keep only results that show an error
                .filter(details -> details.status < 0 || !HttpStatus.valueOf(details.status).is2xxSuccessful());

        sendEmail(events);
    }

    /**
     * Mock emails by collecting all events in a text and logging it.
     * @param report asynchronous flow of responses
     */
    private static void sendEmail(Flux<ExchangeDetails> report) {
        final String formattedReport = report
                .map(details -> String.format("Error on %s. status: %d. Reason: %s", details.url, details.status, details.error.getMessage()))
                // collecting (or reducing, folding, etc.) allows to gather all upstream results to use them as a single value downstream.
                .collect(Collectors.joining(System.lineSeparator(), "REPORT:"+System.lineSeparator(), ""))
                // In a real-world scenario, replace this with a subscribe or chaining to another reactive operation.
                .block();
        LOGGER.info(formattedReport);
    }

    private static void log(ExchangeDetails details) {
        if (details.status >= 0 && HttpStatus.valueOf(details.status).is2xxSuccessful()) {
            LOGGER.info("Success on: "+details.url);
        } else {
            LOGGER.log(Level.WARNING,
                    "Status {0} on {1}. Reason: {2}",
                    new Object[]{
                            details.status,
                            details.url,
                            details.error == null ? "None" : details.error.getMessage()
                    });
        }
    }

    private static Mono<ExchangeDetails> request(String url) {
        return WebClient.create(url).get()
                .retrieve()
                // workaround to counter fail-fast behavior: create a special error that will be converted back to a result
                .onStatus(status -> !status.is2xxSuccessful(), cr -> cr.createException().map(err -> new RequestException(cr.statusCode(), err)))
                .toBodilessEntity()
                .map(response -> new ExchangeDetails(url, response.getStatusCode().value(), null))
                // Convert back custom error to result
                .onErrorResume(RequestException.class, err -> Mono.just(new ExchangeDetails(url, err.status.value(), err.cause)))
                // Convert errors that shut connection before server response (cannot connect, etc.) to a result
                .onErrorResume(Exception.class, err -> Mono.just(new ExchangeDetails(url, -1, err)));
    }

    public static class ExchangeDetails {
        final String url;
        final int status;
        final Exception error;

        public ExchangeDetails(String url, int status, Exception error) {
            this.url = url;
            this.status = status;
            this.error = error;
        }
    }

    private static class RequestException extends RuntimeException {
        final HttpStatus status;
        final Exception cause;

        public RequestException(HttpStatus status, Exception cause) {
            this.status = status;
            this.cause = cause;
        }
    }
}
