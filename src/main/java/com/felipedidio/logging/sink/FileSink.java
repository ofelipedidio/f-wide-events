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

public class FileSink implements WideEventSink, Closeable {
    private final Object lock = new Object();
    private final BufferedWriter out;

    public FileSink(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        out = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

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

    @Override
    public void close() throws IOException {
        out.close();
    }
}
