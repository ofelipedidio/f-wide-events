package com.felipedidio.logging.writer;

import java.time.Instant;
import java.util.Map;

import com.felipedidio.logging.WideEvent;
import com.felipedidio.logging.WideEventEmitter;
import com.felipedidio.logging.WideEventGroup;
import com.google.gson.JsonObject;

/**
 * A {@link WideEventWriter} that automatically emits the event when closed.
 *
 * <p>This class is returned by {@link WideEventEmitter#begin()} and ensures
 * that events are automatically emitted to all configured sinks when the
 * writer is closed (typically via try-with-resources).
 *
 * @see WideEventWriter
 * @see WideEventEmitter#begin()
 */
public class AutoEmittableWideEventWriter extends WideEventWriter {
    private final WideEventEmitter emitter;

    /**
     * Creates a new auto-emittable writer associated with the specified emitter.
     *
     * @param emitter the emitter that will receive the event when this writer is closed
     */
    public AutoEmittableWideEventWriter(WideEventEmitter emitter)
    {
        this.emitter = emitter;
    }

    @Override
    public void close()
    {
        super.close();

        // Build wide event
        JsonObject fields = this.getFields();
        Map<String, WideEventGroup> groups = this.getGroups();
        Instant startTime = this.getStartTime();
        Instant endTime = this.getEndTime();
        Throwable error = this.getError();
        WideEvent wideEvent = new WideEvent(emitter, fields, groups, startTime, endTime, error);

        // Emit wide event
        emitter.emit(wideEvent);
    }
}
