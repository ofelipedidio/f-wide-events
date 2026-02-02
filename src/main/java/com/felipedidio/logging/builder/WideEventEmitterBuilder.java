package com.felipedidio.logging.builder;

import com.felipedidio.logging.WideEventEmitter;
import com.felipedidio.logging.sink.ConsoleSink;
import com.felipedidio.logging.sink.FileSink;
import com.felipedidio.logging.sink.WideEventSink;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A fluent builder for configuring and creating {@link WideEventEmitter} instances.
 *
 * <p>Use this builder to configure emitter parameters, sample rate, filters, and output sinks
 * before calling {@link #build()} to create the emitter.
 *
 * <pre>{@code
 * WideEventEmitter emitter = WideEventEmitter.builder("http-requests", Path.of("logs"))
 *     .parameter("service", "my-api")
 *     .parameter("environment", "production")
 *     .sampleRate(0.1)
 *     .filter((em, event) -> {
 *         if (event.hasError()) return WideEventOutcome.KEEP;
 *         return WideEventOutcome.SAMPLE;
 *     })
 *     .addConsoleSink()
 *     .build();
 * }</pre>
 *
 * <p>By default, the builder automatically adds a file sink that writes to
 * {@code {loggingDirectory}/{name}.log}.
 *
 * @see WideEventEmitter#builder(String, Path)
 */
public class WideEventEmitterBuilder {
    private final String name;
    private final JsonObject parameters;
    private WideEventFilterFunction filterFunction;
    private double sampleRate;
    private final List<WideEventSink> sinks;

    /**
     * Creates a new builder with the specified name and logging directory.
     *
     * <p>A file sink is automatically added that writes to
     * {@code {loggingDirectory}/{name}.log}.
     *
     * @param name0 the name for the emitter
     * @param loggingDirectory the directory for log files
     */
    public WideEventEmitterBuilder(String name0, Path loggingDirectory) {
        this.name = name0;
        this.parameters = new JsonObject();
        this.sampleRate = 1.0d;
        this.sinks = new ArrayList<>();
        Path logFile = loggingDirectory.resolve(name0 + ".log");
        addFileSink(logFile);
    }

    /**
     * Adds a string parameter to the emitter configuration.
     *
     * <p>Parameters are accessible via {@link WideEventEmitter#getParameters()}
     * and can be used in filter functions to make decisions based on emitter metadata.
     *
     * @param parameterName the parameter name
     * @param value the string value
     * @return this builder for method chaining
     */
    public WideEventEmitterBuilder parameter(String parameterName, String value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    /**
     * Adds a numeric parameter to the emitter configuration.
     *
     * @param parameterName the parameter name
     * @param value the numeric value
     * @return this builder for method chaining
     */
    public WideEventEmitterBuilder parameter(String parameterName, Number value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    /**
     * Adds a boolean parameter to the emitter configuration.
     *
     * @param parameterName the parameter name
     * @param value the boolean value
     * @return this builder for method chaining
     */
    public WideEventEmitterBuilder parameter(String parameterName, Boolean value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    /**
     * Adds a character parameter to the emitter configuration.
     *
     * @param parameterName the parameter name
     * @param value the character value
     * @return this builder for method chaining
     */
    public WideEventEmitterBuilder parameter(String parameterName, Character value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    /**
     * Sets the sample rate for events with {@link WideEventOutcome#SAMPLE} outcome.
     *
     * <p>The sample rate is the probability (0.0 to 1.0) that an event marked
     * as SAMPLE by the filter function will be stored. Events marked as KEEP
     * are always stored; events marked as DISCARD are never stored.
     *
     * @param sampleRate0 the sample rate (0.0 to 1.0)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if the sample rate is not between 0.0 and 1.0
     */
    public WideEventEmitterBuilder sampleRate(double sampleRate0) {
        if (sampleRate0 < 0.0d || sampleRate0 > 1.0d) {
            throw new IllegalArgumentException("Sample rate must be between 0.0 and 1.0");
        }
        this.sampleRate = sampleRate0;
        return this;
    }

    /**
     * Sets the filter function for determining event outcomes.
     *
     * <p>The filter function is called for each event before emission and returns
     * a {@link WideEventOutcome} that determines how the event is processed:
     * <ul>
     *   <li>{@link WideEventOutcome#KEEP} - always store the event</li>
     *   <li>{@link WideEventOutcome#SAMPLE} - store with probability equal to sample rate</li>
     *   <li>{@link WideEventOutcome#DISCARD} - never store the event</li>
     * </ul>
     *
     * @param filter the filter function
     * @return this builder for method chaining
     * @see WideEventFilterFunction
     */
    public WideEventEmitterBuilder filter(WideEventFilterFunction filter) {
        filterFunction = filter;
        return this;
    }

    /**
     * Adds a file sink that writes events to the specified path.
     *
     * <p>The sink writes newline-delimited JSON to the file. Parent directories
     * are created automatically if they don't exist. The file is opened in
     * append mode with UTF-8 encoding.
     *
     * @param pathToFile the path to the log file
     * @return this builder for method chaining
     * @throws UncheckedIOException if the file cannot be created or opened
     */
    public WideEventEmitterBuilder addFileSink(Path pathToFile) {
        try {
            FileSink fileSink = new FileSink(pathToFile);
            this.sinks.add(fileSink);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create file sink at " + pathToFile, e);
        }
        return this;
    }

    /**
     * Adds a console sink that writes events to stdout or stderr.
     *
     * <p>Events without errors are written to stdout; events with errors
     * are written to stderr.
     *
     * @return this builder for method chaining
     */
    public WideEventEmitterBuilder addConsoleSink() {
        this.sinks.add(new ConsoleSink());
        return this;
    }

    /**
     * Adds a custom sink implementation.
     *
     * @param sink the sink to add
     * @return this builder for method chaining
     * @see WideEventSink
     */
    public WideEventEmitterBuilder addSink(WideEventSink sink) {
        this.sinks.add(sink);
        return this;
    }

    /**
     * Builds and returns the configured {@link WideEventEmitter}.
     *
     * @return the configured emitter
     */
    public WideEventEmitter build() {
        return new WideEventEmitter(name, parameters, sampleRate, filterFunction, sinks);
    }
}
