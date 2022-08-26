package fr.amanin.stackoverflow;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class ReactorAccumulator {

    public static void main(String[] args) {
        Flux<Integer[]> growingSlider = Flux.range(0, 10)
                .scan(new SlidingBuffer<>(5, Integer[]::new), SlidingBuffer::accumulate)
                .skip(1)
                .map(SlidingBuffer::getBuffer);

        print("Growing slider", growingSlider);

        var decreasingSlider = growingSlider.last()
                .flatMapMany(buffer -> slideDecrease(buffer, 1));
        print("Decreasing slider", decreasingSlider);

        var slider = thenDecrease(growingSlider);
        print("Growing then decreasing slider", slider);
    }

    private static <V> void print(String title, Flux<V[]> dataStream) {
        var nl = System.lineSeparator();
        var log = dataStream
                .map(Arrays::toString)
                .collect(Collectors.joining(nl, "-- "+ title + nl, nl + "--"))
                .block(Duration.ofSeconds(1));
        System.out.println(log);
    }

    private static <V> Flux<V[]> slideDecrease(V[] buffer, int startIndex) {
        if (buffer.length <= startIndex) return Flux.empty();
        else return Flux.range(startIndex, buffer.length - startIndex)
                .map(from -> Arrays.copyOfRange(buffer, from, buffer.length));
    }

    private static <V> Flux<V[]> thenDecrease(Flux<V[]> dataStream) {
        var cacheLast = dataStream.cache(1);
        return cacheLast.concatWith(cacheLast.last()
                .flatMapMany(buffer -> slideDecrease(buffer, 1)));
    }
    static final class SlidingBuffer<V> {

        private final int maxSize;

        private final V[] buffer;

        SlidingBuffer(int maxSize, IntFunction<V[]> creator) {
            this(maxSize, creator.apply(0));
        }

        private SlidingBuffer(int maxSize, V[] buffer) {
            this.maxSize = maxSize;
            this.buffer = buffer;
        }

        SlidingBuffer<V> accumulate(V newValue) {
            final V[] nextBuffer = (buffer.length < maxSize)
                    ? Arrays.copyOf(buffer, buffer.length + 1)
                    : Arrays.copyOfRange(buffer, 1, maxSize + 1);

            nextBuffer[nextBuffer.length - 1] = newValue;

            return new SlidingBuffer<>(maxSize, nextBuffer);
        }

        V[] getBuffer() {
            // Def copy: might not be needed if you trust consumer code not to modify the array directly
            return Arrays.copyOf(buffer, buffer.length);
        }
    }
}
