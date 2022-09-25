package fr.amanin.stackoverflow;

import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Support for <a href="https://stackoverflow.com/a/73829625">SO answer about possible reactie stats</a>
 */
public class ExplainReactorErrors {

	public static void main(String[] args) {
		monoStates();
		gracefulHandling();
		queryExample();
	}

	private static void queryExample() {
		String[] inputs = { null, "hello_world", "___" };

		for (var input : inputs) {
			try {
				String result = processRequest(input)
						.collect(Collectors.joining(", ", "[", "]"))
						.block();
				System.out.println("COMPLETED: " + result);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
			}
		}
	}

	public static Flux<String> processRequest(String queryParam) {
		if (queryParam == null || queryParam.isEmpty()) return Flux.error(new IllegalArgumentException("Bad request"));
		return Mono.just(queryParam)
		           .flatMapMany(param -> Flux.fromArray(param.split("_")))
		           .switchIfEmpty(Mono.error(new IllegalStateException("No data")));
	}

	public static void monoStates() {
		// Reactive streams does not accept null values:
		try {
			Mono.just(null);
		} catch (NullPointerException e) {
			System.out.println("NULL VALUE NOT ACCEPTED !");
		}

		// Mono/Flux operations stop if an error occurs internally, and send it downstream
		try {
			Mono.just("Something")
			    .map(it -> { throw new IllegalStateException("Bouh !"); })
			    .block();
		} catch (IllegalStateException e) {
			System.out.println("Error propagated: "+e.getMessage());
		}

		// A mono or a flux can end "empty". It means that no value or error happened.
		// The operation just finished without any result
		var result = Mono.just("Hello !")
				.filter(it -> !it.endsWith("!"))
				// Materialize allow to receive the type of signal produced by the pipeline (next value, error, completion, etc.)
				.materialize()
				.block();
		System.out.println("Input value has been filtered out. No 'next' value " +
				"received, just 'completion' signal:" + result.getType());
	}

	public static void gracefulHandling() {
		// Errors can be intercepted and replaced by a value:
		var result = Mono.error(new IllegalStateException("No !"))
		                 .onErrorResume(err -> Mono.just("Override error: Hello again !"))
		                 .block();
		System.out.println(result);

		// Empty pipelines can also be replaced by another one that produce a value:
		result = Mono.just("Hello !")
		             .filter(it -> !it.endsWith("!"))
		             .switchIfEmpty(Mono.just("Override empty: Hello again !"))
		             .block();

		System.out.println(result);
	}

}
