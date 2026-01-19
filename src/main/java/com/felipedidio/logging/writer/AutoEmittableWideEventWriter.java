package com.felipedidio.logging.writer;

import java.time.Instant;
import java.util.Map;

import com.felipedidio.logging.WideEvent;
import com.felipedidio.logging.WideEventEmitter;
import com.felipedidio.logging.WideEventGroup;
import com.google.gson.JsonObject;

public class AutoEmittableWideEventWriter extends WideEventWriter {
    private final WideEventEmitter emitter;

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
