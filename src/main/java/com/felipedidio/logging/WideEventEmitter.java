package com.felipedidio.logging;

import com.felipedidio.logging.builder.WideEventEmitterBuilder;
import com.felipedidio.logging.builder.WideEventFilterFunction;
import com.felipedidio.logging.builder.WideEventOutcome;
import com.felipedidio.logging.sink.WideEventSink;
import com.felipedidio.logging.writer.AutoEmittableWideEventWriter;
import com.felipedidio.logging.writer.WideEventWriter;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main entry point for creating and emitting structured wide events.
 *
 * <p>A {@code WideEventEmitter} is typically created once and reused throughout the application
 * lifetime. It manages event configuration, filtering, sampling, and output to configured sinks.
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Create an emitter (typically a static singleton)
 * WideEventEmitter emitter = WideEventEmitter.builder("http-requests", Path.of("logs"))
 *     .parameter("service", "my-api")
 *     .sampleRate(1.0)
 *     .build();
 *
 * // Write events using try-with-resources
 * try (var event = emitter.begin("request-123")) {
 *     event.set("method", "GET");
 *     event.set("path", "/api/users");
 *     event.set("status", 200);
 * }
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>This class is thread-safe. Multiple threads can call {@link #begin(String)} and {@link #emit(WideEvent)}
 * concurrently. Event local IDs are assigned atomically.
 *
 * @see WideEventEmitterBuilder
 * @see WideEventWriter
 * @see WideEvent
 */
public final class WideEventEmitter {
    private final String name;
    private final JsonObject parameters;
    private final double sampleRate;
    private final WideEventFilterFunction filterFunction;
    private final List<WideEventSink> sinks;

    private final AtomicInteger nextLocalId = new AtomicInteger(0);

    /**
     * Creates a new wide event emitter with the specified configuration.
     *
     * <p>Use {@link #builder(String, Path)} for a more convenient way to create emitters.
     *
     * @param name0 the emitter name
     * @param parameters0 the emitter parameters
     * @param sampleRate0 the sample rate (0.0 to 1.0)
     * @param filterFunction0 the filter function, or null for no filtering
     * @param sinks0 the list of sinks to write events to
     */
    public WideEventEmitter(String name0, JsonObject parameters0, double sampleRate0, WideEventFilterFunction filterFunction0,
                            List<WideEventSink> sinks0) {
        this.name = name0;
        this.parameters = parameters0;
        this.sampleRate = sampleRate0;
        this.filterFunction = filterFunction0;
        this.sinks = sinks0;
    }

    /**
     * Returns the name of this emitter.
     *
     * <p>The name is used as the {@code emitter} field in emitted events
     * and as the default log file name.
     *
     * @return the emitter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameters configured for this emitter.
     *
     * <p>Parameters are metadata that can be accessed in filter functions
     * to make filtering decisions based on emitter configuration.
     *
     * @return the parameters as a JSON object
     */
    public JsonObject getParameters() {
        return parameters;
    }

    /**
     * Begins a new event and returns a writer for populating it.
     *
     * <p>The returned writer should be used within a try-with-resources block.
     * When the writer is closed, the event is automatically emitted to all
     * configured sinks (subject to filtering and sampling).
     *
     * <pre>{@code
     * try (var event = emitter.begin("request-123")) {
     *     event.set("field", "value");
     *     event.group("nested", g -> g.set("inner", 123));
     * }
     * }</pre>
     *
     * @param eventType the unique identifier for this event instance
     * @return a new event writer
     */
    public WideEventWriter begin(String eventType) {
        return new AutoEmittableWideEventWriter(this, eventType);
    }

    /**
     * Emits a completed event to all configured sinks.
     *
     * <p>The event is first passed through the filter function (if configured).
     * Based on the filter outcome and sample rate, the event may be kept,
     * sampled, or discarded.
     *
     * <p>This method is typically called automatically when closing
     * a {@link WideEventWriter} obtained from {@link #begin(String)}.
     *
     * @param wideEvent the completed event to emit
     */
    public void emit(WideEvent wideEvent) {
        boolean shouldStore = filterEvent(wideEvent);
        if (!shouldStore) {
            return;
        }

        for (WideEventSink sink : sinks) {
            sink.write(wideEvent);
        }
    }

    private boolean filterEvent(WideEvent wideEvent) {
        WideEventOutcome outcome = WideEventOutcome.KEEP;
        if (filterFunction != null) {
            outcome = filterFunction.filter(this, wideEvent);
        }

        if (outcome == WideEventOutcome.DISCARD) {
            return false;
        } else if (outcome == WideEventOutcome.SAMPLE) {
            return ThreadLocalRandom.current().nextDouble() <= sampleRate;
        } else {
            return true;
        }
    }

    /**
     * Creates a new builder for constructing a {@code WideEventEmitter}.
     *
     * <p>The builder automatically adds a file sink that writes to
     * {@code {loggingDirectory}/{name}.log}.
     *
     * @param name0 the name for the emitter (used in event_name field and default log file)
     * @param loggingDirectory the directory where log files will be written
     * @return a new emitter builder
     */
    public static WideEventEmitterBuilder builder(String name0, Path loggingDirectory) {
        return new WideEventEmitterBuilder(name0, loggingDirectory);
    }

    int getNextLocalId() {
        return nextLocalId.getAndIncrement();
    }

    UUID getId() {
        return UUID.randomUUID();
    }
}
