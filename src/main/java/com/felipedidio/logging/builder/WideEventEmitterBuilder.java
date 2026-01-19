package com.felipedidio.logging.builder;

import com.felipedidio.logging.WideEventEmitter;
import com.felipedidio.logging.sink.ConsoleSink;
import com.felipedidio.logging.sink.FileSink;
import com.felipedidio.logging.sink.WideEventSink;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WideEventEmitterBuilder {
    private final String name;
    private final JsonObject parameters;
    private WideEventFilterFunction filterFunction;
    private double sampleRate;
    private final List<WideEventSink> sinks;

    public WideEventEmitterBuilder(String name0, Path loggingDirectory) {
        this.name = name0;
        this.parameters = new JsonObject();
        this.sampleRate = 1.0d;
        this.sinks = new ArrayList<>();
        Path logFile = loggingDirectory.resolve(name0 + ".log");
        addFileSink(logFile);
    }

    public WideEventEmitterBuilder parameter(String parameterName, String value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    public WideEventEmitterBuilder parameter(String parameterName, Number value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    public WideEventEmitterBuilder parameter(String parameterName, Boolean value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    public WideEventEmitterBuilder parameter(String parameterName, Character value) {
        parameters.addProperty(parameterName, value);
        return this;
    }

    public WideEventEmitterBuilder sampleRate(double sampleRate0) {
        if (sampleRate0 < 0.0d || sampleRate0 > 1.0d) {
            throw new IllegalArgumentException("The sample rage must fall within (0.0, 1.0)");
        }
        this.sampleRate = sampleRate0;
        return this;
    }

    public WideEventEmitterBuilder filter(WideEventFilterFunction filter) {
        filterFunction = filter;
        return this;
    }

    public WideEventEmitterBuilder addFileSink(Path pathToFile) {
        try {
            FileSink defaultSink = new FileSink(pathToFile);
            this.sinks.add(defaultSink);
        } catch (IOException ignored) {
        }
        return this;
    }

    public WideEventEmitterBuilder addConsoleSink() {
        this.sinks.add(new ConsoleSink());
        return this;
    }

    public WideEventEmitter build() {
        return new WideEventEmitter(name, parameters, sampleRate, filterFunction, sinks);
    }
}
