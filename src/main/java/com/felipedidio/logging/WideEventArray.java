package com.felipedidio.logging;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents an immutable array of event items with timing information.
 *
 * <p>A {@code WideEventArray} contains an ordered list of items, each serialized as a JSON object.
 * The array itself is timed, capturing when it was created and closed.
 *
 * <h2>JSON Structure</h2>
 * <p>When serialized via {@link #toJson()}, the array produces:
 *
 * <pre>{@code
 * {
 *   "items": [
 *     { "field1": "value1", "start_time": "...", "end_time": "...", "duration_ms": 10 },
 *     { "field2": "value2" }
 *   ],
 *   "start_time": "2026-01-19T10:00:00Z",
 *   "end_time": "2026-01-19T10:00:00.050Z",
 *   "duration_ms": 50
 * }
 * }</pre>
 *
 * @see com.felipedidio.logging.writer.ArrayEventWriter
 */
public class WideEventArray {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private final @NotNull List<JsonObject> items;
    private final @NotNull Instant startTime;
    private final @NotNull Instant endTime;
    private final @NotNull Duration duration;

    /**
     * Creates a new event array with the specified data.
     *
     * @param items0 the pre-serialized item JSON objects
     * @param startTime0 the start time
     * @param endTime0 the end time
     */
    public WideEventArray(@NotNull List<JsonObject> items0, @NotNull Instant startTime0, @NotNull Instant endTime0) {
        this.items = List.copyOf(items0);
        this.startTime = startTime0;
        this.endTime = endTime0;
        this.duration = Duration.between(startTime0, endTime0);
    }

    /**
     * Serializes this array to a JSON object.
     *
     * @return the array as a JSON object
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        JsonArray itemsArray = new JsonArray();
        for (JsonObject item : items) {
            itemsArray.add(item);
        }
        json.add("items", itemsArray);

        json.addProperty("start_time", ISO_FORMATTER.format(startTime));
        json.addProperty("end_time", ISO_FORMATTER.format(endTime));
        json.addProperty("duration_ms", duration.toMillis());

        return json;
    }

    /**
     * Returns the items in this array.
     *
     * @return the items as JSON objects
     */
    public @NotNull List<JsonObject> getItems() {
        return items;
    }

    /**
     * Returns the start time of this array.
     *
     * @return the start time
     */
    public @NotNull Instant getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of this array.
     *
     * @return the end time
     */
    public @NotNull Instant getEndTime() {
        return endTime;
    }

    /**
     * Returns the duration of this array.
     *
     * @return the duration
     */
    public @NotNull Duration getDuration() {
        return duration;
    }
}
