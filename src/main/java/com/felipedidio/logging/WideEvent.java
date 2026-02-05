package com.felipedidio.logging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

/**
 * Represents a completed wide event with all its data, metadata, and identifiers.
 *
 * <p>A {@code WideEvent} is an immutable snapshot of event data created when a
 * {@link com.felipedidio.logging.writer.WideEventWriter} is closed. It extends
 * {@link WideEventGroup} with additional event-level metadata:
 *
 * <ul>
 *   <li>{@link #getId()} - A unique UUID for this event</li>
 *   <li>{@link #getLocalId()} - A sequential ID within the emitter (0, 1, 2, ...)</li>
 *   <li>{@link #getEmitterName()} - The name of the emitter that created this event</li>
 *   <li>{@link #getEventType()} - The user-provided event identifier</li>
 * </ul>
 *
 * <h2>JSON Representation</h2>
 * <p>When serialized to JSON via {@link #toJson()}, the event includes all fields
 * from the parent {@link WideEventGroup} plus the event-level identifiers:
 *
 * <pre>{@code
 * {
 *   "field1": "value1",
 *   "start_time": "2026-01-19T10:00:00Z",
 *   "end_time": "2026-01-19T10:00:00.050Z",
 *   "duration_ms": 50,
 *   "error": false,
 *   "emitter_name": "http-requests",
 *   "event_type": "request-123",
 *   "local_id": 0,
 *   "id": "550e8400-e29b-41d4-a716-446655440000"
 * }
 * }</pre>
 *
 * @see WideEventGroup
 * @see WideEventEmitter#emit(WideEvent)
 */
public final class WideEvent extends WideEventGroup {
    private final WideEventEmitter emitter;
    private final String eventType;
    private final UUID id;
    private final int localId;

    /**
     * Creates a new wide event with the specified data and emitter reference.
     *
     * @param emitter0 the emitter that created this event
     * @param eventType0 the user-provided event identifier
     * @param fields0 the event fields
     * @param groups0 the nested groups
     * @param startTime0 the event start time
     * @param endTime0 the event end time
     * @param error0 the error, if any
     */
    public WideEvent(WideEventEmitter emitter0, String eventType0, JsonObject fields0, Map<String, WideEventGroup> groups0, Instant startTime0,
            Instant endTime0, @Nullable Throwable error0)
    {
        super(fields0, groups0, startTime0, endTime0, error0);
        this.emitter = emitter0;
        this.eventType = eventType0;
        this.id = emitter0.getId();
        this.localId = emitter0.getNextLocalId();
    }

    /**
     * Serializes this event to a JSON object.
     *
     * <p>The JSON includes all fields and groups from the parent {@link WideEventGroup},
     * plus the event-level metadata: {@code emitter_name}, {@code local_id}, and {@code id}.
     *
     * @return the event as a JSON object
     */
    public JsonObject toJson()
    {
        JsonObject json = super.toJson();
        json.addProperty("emitter_name", emitter.getName());
        json.addProperty("event_type", eventType);
        json.addProperty("local_id", localId);
        json.addProperty("id", id.toString());
        return json;
    }

    /**
     * Returns the unique identifier for this event.
     *
     * @return the event UUID
     */
    public UUID getId()
    {
        return id;
    }

    /**
     * Returns the sequential local identifier within the emitter.
     *
     * <p>Local IDs start at 0 and increment for each event emitted by the same emitter.
     *
     * @return the local event ID
     */
    public int getLocalId()
    {
        return localId;
    }

    /**
     * Returns the name of the emitter that created this event.
     *
     * @return the emitter name
     */
    public String getEmitterName()
    {
        return emitter.getName();
    }

    /**
     * Returns the user-provided event identifier.
     *
     * @return the event identifier
     */
    public String getEventType()
    {
        return eventType;
    }
}
