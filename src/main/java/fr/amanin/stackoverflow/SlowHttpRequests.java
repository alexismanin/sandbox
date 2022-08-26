package fr.amanin.stackoverflow;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class SlowHttpRequests {

    static String SERVER_URI = "http://localhost:8080/hello";
    static int NUMBER_OF_REQUESTS = 1000;

    public static void main(String[] args) throws Exception {
        System.out.println("WITH WEBFLUX");
        usingWebFlux();

        // usingJavaNetClient();
    }

    private static void usingWebFlux() throws Exception {
        final long start = System.nanoTime();
        var client = WebClient.create(SERVER_URI);
        final CountDownLatch barrier = new CountDownLatch(1);
        Flux.range(0, NUMBER_OF_REQUESTS)
                        .flatMap(i -> client
                                .get()
                                .retrieve()
                                .bodyToMono(String.class))
                        .subscribe(
                                value -> {},
                                err -> err.printStackTrace(),
                                () -> barrier.countDown());
        barrier.await();
        System.out.println((System.nanoTime() - start) * 1e-6);
    }

    private static void usingJavaNetClient() throws Exception {
        final long start = System.nanoTime();

        var cli = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(new URI(SERVER_URI)).build();
        var responseReader = HttpResponse.BodyHandlers.ofString(StandardCharsets.US_ASCII);

        List<CompletableFuture<HttpResponse<String>>> tasks = new ArrayList<>(NUMBER_OF_REQUESTS);
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            tasks.add(cli.sendAsync(request, responseReader));
        }

        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) tasks.get(i).get();

        System.out.println((System.nanoTime() - start) * 1e-6);
    }

    static String getIP() {
        try {
            URL url = new URL("https://httpbin.org/ip");
            URLConnection conn = url.openConnection();
            try (var stream = conn.getInputStream()) {
                return new String(stream.readAllBytes(), StandardCharsets.US_ASCII);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
