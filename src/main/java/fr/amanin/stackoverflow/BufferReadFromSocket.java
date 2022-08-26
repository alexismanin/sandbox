package fr.amanin.stackoverflow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Optional;

/**
 * Alternative for <a href="https://stackoverflow.com/questions/73335858/problem-with-java-nio-bytebuffer-and-java-nio-socketchannel-java-lang-outofmemo">this SO question</a>.
 * This code replace direct buffer usage with a byte array wrapper.
 */
public class BufferReadFromSocket {

    public Optional<String> readFromSocket(SocketChannel socket, int bytesToRead) throws IOException, InterruptedException {
        if (bytesToRead < 1) return Optional.empty();
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[bytesToRead]);
        while (socket.read(buffer) >= 0 && buffer.hasRemaining()) {
            Thread.sleep(1);
        }

        if (buffer.position() < 1) {
            logError("Nothing read");
            return Optional.empty();
        }
        final byte[] content = buffer.hasRemaining()
                ? Arrays.copyOf(buffer.array(), buffer.position())
                : buffer.array();

        return Optional.of(toHex(content));
    }

    private static void logError(String nothing_read) {
        // do nothing
    }

    private static String toHex(byte[] content) {
        return "";
    }
}
