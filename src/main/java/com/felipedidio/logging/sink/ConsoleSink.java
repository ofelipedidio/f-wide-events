package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;

/**
 * A sink that writes events to the console as JSON.
 *
 * <p>Events without errors are written to {@link System#out} (stdout),
 * while events with errors are written to {@link System#err} (stderr).
 *
 * <p>This sink is useful for development, debugging, and environments where
 * console output is captured by a logging infrastructure.
 *
 * @see WideEventSink
 * @see com.felipedidio.logging.builder.WideEventEmitterBuilder#addConsoleSink()
 */
public class ConsoleSink implements WideEventSink {
    /**
     * Writes an event to the console as JSON.
     *
     * <p>Events without errors are written to stdout; events with errors
     * are written to stderr.
     *
     * @param wideEvent the event to write
     */
    @Override
    public void write(WideEvent wideEvent) {
        String eventJson = GsonSerializer.serializeWideEvent(wideEvent);
        boolean hasError = wideEvent.hasError();
        if (!hasError) {
            System.out.println(eventJson);
        } else {
            System.err.println(eventJson);
        }
    }
}
