package com.felipedidio.logging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

public final class WideEvent extends WideEventGroup {
    private final WideEventEmitter emitter;
    private final UUID id;
    private final int localId;

    public WideEvent(WideEventEmitter emitter0, JsonObject fields0, Map<String, WideEventGroup> groups0, Instant startTime0,
            Instant endTime0, @Nullable Throwable error0)
    {
        super(fields0, groups0, startTime0, endTime0, error0);
        this.emitter = emitter0;
        this.id = emitter0.getId();
        this.localId = emitter0.getNextLocalId();
    }

    public JsonObject toJson()
    {
        JsonObject json = super.toJson();
        json.addProperty("event_name", emitter.getName());
        json.addProperty("local_id", localId);
        json.addProperty("id", id.toString());
        return json;
    }

    public UUID getId()
    {
        return id;
    }

    public int getLocalId()
    {
        return localId;
    }
}
