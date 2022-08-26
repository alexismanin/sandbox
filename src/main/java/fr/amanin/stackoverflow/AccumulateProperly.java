package fr.amanin.stackoverflow;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AccumulateProperly {

    record Event(String data) {}
    record Resource(int id) {}

    record Group(String keyCode, List<Event> events) {
        Group merge(List<Event> newEvents) {
            var allEvents = new ArrayList<>(events);
            allEvents.addAll(newEvents);
            return new Group(keyCode, allEvents);
        }
    }

    record MyDto(List<Group> groups) { }

    static Flux<Resource> findResourcesByKeyCode(String keyCode) {
        return Flux.just(new Resource(1), new Resource(2));
    }

    static Flux<Event> findEventById(int id) {
        return Flux.just(
                new Event("resource_"+id+"_event_1"),
                new Event("resource_"+id+"_event_2")
        );
    }

    public static void main(String[] args) {
        MyDto dtoInstance = new MyDto(List.of(new Group("myGroup", List.of())));
        System.out.println("INITIAL STATE:");
        System.out.println(dtoInstance);

        // Asynchronous operation pipeline
        Mono<MyDto> dtoCompletionPipeline = Mono.just(dtoInstance)
                .flatMap(dto -> Flux.fromIterable(dto.groups)
                            // for each group, find associated resources
                            .flatMap(group -> findResourcesByKeyCode(group.keyCode())
                                    // For each resource, fetch its associated event
                                    .flatMap(resource -> findEventById(resource.id()))
                                    // Collect all events for the group
                                    .collectList()
                                    // accumulate collected events in a new instance of the group
                                    .map(group::merge)
                            )
                            // Collect all groups after they've collected events
                            .collectList()
                            // Build a new dto instance from the completed set of groups
                            .map(completedGroups -> new MyDto(completedGroups))
                );


        // NOTE: block is here only because we are in a main function and that I want to print
        // pipeline output before program extinction.
        // Try to avoid block. Return your mono, or connect it to another Mono or Flux object using
        // an operation like flatMap.
        dtoInstance = dtoCompletionPipeline.block(Duration.ofSeconds(1));
        System.out.println("OUTPUT STATE:");
        System.out.println(dtoInstance);
    }
}
