package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;
import com.google.gson.JsonIOException;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A sink that writes events to a file as newline-delimited JSON.
 *
 * <p>Each event is written as a single JSON line followed by a newline character.
 * The file is opened in append mode, so events are added to the end of existing content.
 *
 * <h2>Thread Safety</h2>
 * <p>This sink is thread-safe. Writes are synchronized to ensure events from
 * concurrent threads don't interleave.
 *
 * <h2>File Handling</h2>
 * <ul>
 *   <li>Parent directories are created automatically if they don't exist</li>
 *   <li>The file is opened with UTF-8 encoding</li>
 *   <li>Each write is flushed immediately to ensure durability</li>
 *   <li>Write errors are silently ignored</li>
 * </ul>
 *
 * @see WideEventSink
 * @see com.felipedidio.logging.builder.WideEventEmitterBuilder#addFileSink(Path)
 */
public class FileSink implements WideEventSink, Closeable {
    private final Object lock = new Object();
    private final BufferedWriter out;

    /**
     * Creates a new file sink that writes to the specified path.
     *
     * <p>Parent directories are created automatically if they don't exist.
     * The file is opened in append mode with UTF-8 encoding.
     *
     * @param path the path to the log file
     * @throws IOException if the file cannot be created or opened
     */
    public FileSink(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        out = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Writes an event to the file as a JSON line.
     *
     * <p>The event is serialized to JSON, written to the file, followed by a newline,
     * and flushed immediately. Write errors are silently ignored.
     *
     * @param wideEvent the event to write
     */
    @Override
    public void write(WideEvent wideEvent) {
        synchronized (lock) {
            try {
                GsonSerializer.serializeWideEvent(wideEvent, out);
                out.newLine();
                out.flush();
            } catch (JsonIOException | IOException ignored) {
            }
        }
    }

    /**
     * Closes the underlying file writer.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        out.close();
    }
}
