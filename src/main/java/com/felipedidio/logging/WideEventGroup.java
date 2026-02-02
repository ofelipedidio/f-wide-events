package com.felipedidio.logging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Represents a hierarchical grouping of event data with fields, nested groups, timing, and error information.
 *
 * <p>A {@code WideEventGroup} is the base container for event data, supporting:
 * <ul>
 *   <li>Key-value fields (accessible via {@link #getFields()} or typed accessors)</li>
 *   <li>Nested sub-groups (accessible via {@link #getGroups()})</li>
 *   <li>Timing information ({@link #getStartTime()}, {@link #getEndTime()}, {@link #getDuration()})</li>
 *   <li>Error tracking ({@link #hasError()})</li>
 * </ul>
 *
 * <h2>JSON Structure</h2>
 * <p>When serialized via {@link #toJson()}, the group produces:
 *
 * <pre>{@code
 * {
 *   "field1": "value1",
 *   "field2": 123,
 *   "nested_group": {
 *     "inner_field": "inner_value",
 *     "start_time": "...",
 *     "end_time": "...",
 *     "duration_ms": 5,
 *     "error": false
 *   },
 *   "start_time": "2026-01-19T10:00:00Z",
 *   "end_time": "2026-01-19T10:00:00.050Z",
 *   "duration_ms": 50,
 *   "error": false
 * }
 * }</pre>
 *
 * <h2>Field Access</h2>
 * <p>Use the typed accessors to retrieve fields by path:
 * <ul>
 *   <li>{@link #getFieldAsString(String)} - for string values</li>
 *   <li>{@link #getFieldAsNumber(String)} - for numeric values</li>
 *   <li>{@link #getFieldAsBoolean(String)} - for boolean values</li>
 *   <li>{@link #getField(String)} - for raw JSON elements</li>
 * </ul>
 *
 * @see WideEvent
 * @see com.felipedidio.logging.writer.WideEventWriter
 */
public class WideEventGroup {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private final @NotNull JsonObject fields;
    private final @NotNull Map<String, WideEventGroup> groups;

    private final @NotNull Instant startTime;
    private final @NotNull Instant endTime;

    private final @NotNull Duration duration;

    private final @Nullable Throwable error;

    /**
     * Creates a new event group with the specified data.
     *
     * @param fields0 the fields for this group
     * @param groups0 the nested sub-groups
     * @param startTime0 the start time
     * @param endTime0 the end time
     * @param error0 the error, if any
     */
    public WideEventGroup(@NotNull JsonObject fields0, @NotNull Map<String, @NotNull WideEventGroup> groups0, @NotNull Instant startTime0,
                          @NotNull Instant endTime0, @Nullable Throwable error0) {
        this.fields = fields0;
        this.groups = groups0;
        this.startTime = startTime0;
        this.endTime = endTime0;
        this.duration = Duration.between(startTime0, endTime0);
        this.error = error0;
    }

    /**
     * Serializes this group to a JSON object.
     *
     * <p>The JSON includes all fields, nested groups (recursively serialized),
     * timing information, and error status.
     *
     * @return the group as a JSON object
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        for (var fieldEntry : fields.entrySet()) {
            String key = fieldEntry.getKey();
            JsonElement value = fieldEntry.getValue();
            json.add(key, value);
        }

        for (var groupEntry : groups.entrySet()) {
            String groupName = groupEntry.getKey();
            WideEventGroup group = groupEntry.getValue();
            JsonObject groupJson = group.toJson();
            json.add(groupName, groupJson);
        }

        json.addProperty("start_time", ISO_FORMATTER.format(startTime));
        json.addProperty("end_time", ISO_FORMATTER.format(endTime));
        json.addProperty("duration_ms", duration.toMillis());

        if (error != null) {
            JsonArray error = getErrorCauses(this.error);
            json.addProperty("error", true);
            json.add("error_cause", error);
        } else {
            json.addProperty("error", false);
        }

        return json;
    }

    /**
     * Returns the fields set on this group.
     *
     * @return the fields as a JSON object
     */
    public JsonObject getFields() {
        return fields;
    }

    /**
     * Returns the nested sub-groups.
     *
     * @return a map of group names to their group data
     */
    public Map<String, WideEventGroup> getGroups() {
        return groups;
    }

    /**
     * Returns the start time of this group.
     *
     * @return the start time
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of this group.
     *
     * @return the end time
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Returns the duration of this group (end time minus start time).
     *
     * @return the duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Returns whether an error was recorded on this group.
     *
     * @return {@code true} if an error was recorded
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * Retrieves a field by dot-separated path.
     *
     * <p>For example, {@code getField("user.name")} would retrieve the "name" field
     * from a nested "user" object within the fields.
     *
     * @param path the dot-separated path to the field
     * @return the field value, or {@code null} if not found
     */
    public @Nullable JsonElement getField(String path) {
        String[] parts = path.split("\\.");
        JsonElement current = fields;

        for (String part : parts) {
            if (current == null || !current.isJsonObject()) {
                return null;
            }
            current = current.getAsJsonObject().get(part);
        }

        return current;
    }

    /**
     * Retrieves a string field by dot-separated path.
     *
     * @param path the dot-separated path to the field
     * @return the string value, or {@code null} if not found or not a string
     */
    public @Nullable String getFieldAsString(String path) {
        JsonElement element = getField(path);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        return null;
    }

    /**
     * Retrieves a numeric field by dot-separated path.
     *
     * @param path the dot-separated path to the field
     * @return the numeric value, or {@code null} if not found or not a number
     */
    public @Nullable Number getFieldAsNumber(String path) {
        JsonElement element = getField(path);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsNumber();
        }
        return null;
    }

    /**
     * Retrieves a boolean field by dot-separated path.
     *
     * @param path the dot-separated path to the field
     * @return the boolean value, or {@code null} if not found or not a boolean
     */
    public @Nullable Boolean getFieldAsBoolean(String path) {
        JsonElement element = getField(path);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        return null;
    }

    private static @NotNull JsonArray getErrorCauses(@NotNull Throwable error) {
        JsonArray causes = new JsonArray();
        while (error != null) {
            JsonObject causeJson = getErrorJson(error);
            causes.add(causeJson);
            error = error.getCause();
        }
        return causes;
    }

    private static @NotNull JsonObject getErrorJson(@NotNull Throwable error) {
        String type = error.getClass().getCanonicalName();
        String message = error.getMessage();

        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("error_type", type);
        errorJson.addProperty("error_message", message);
        return errorJson;
    }
}
