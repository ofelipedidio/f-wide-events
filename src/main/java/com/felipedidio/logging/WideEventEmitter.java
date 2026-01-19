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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class WideEventEmitter {
    private final String name;
    private final JsonObject parameters;
    private final double sampleRate;
    private final WideEventFilterFunction filterFunction;
    private final List<WideEventSink> sinks;

    private final AtomicInteger nextLocalId = new AtomicInteger(0);

    private final Random random = new Random();

    public WideEventEmitter(String name0, JsonObject parameters0, double sampleRate0, WideEventFilterFunction filterFunction0,
                            List<WideEventSink> sinks0) {
        this.name = name0;
        this.parameters = parameters0;
        this.sampleRate = sampleRate0;
        this.filterFunction = filterFunction0;
        this.sinks = sinks0;
    }

    public String getName() {
        return name;
    }

    public JsonObject getParameters() {
        return parameters;
    }

    public WideEventWriter begin() {
        return new AutoEmittableWideEventWriter(this);
    }

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
            return random.nextDouble() <= sampleRate;
        } else {
            return true;
        }
    }

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
