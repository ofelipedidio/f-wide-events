package com.felipedidio.logging.writer;

import com.felipedidio.logging.WideEventGroup;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A mutable builder for constructing wide events with fields, nested groups, and error information.
 *
 * <p>Use this class within a try-with-resources block to ensure proper timing capture
 * and automatic event emission:
 *
 * <pre>{@code
 * try (var event = emitter.begin()) {
 *     event.set("field", "value");
 *     event.set("count", 42);
 *     event.group("nested", g -> g.set("inner", true));
 * }
 * }</pre>
 *
 * <h2>Fields</h2>
 * <p>Use the {@link #set} methods to add key-value pairs to the event. Supported types
 * include {@link String}, {@link Number}, {@link Boolean}, {@link Character}, and
 * {@link com.google.gson.JsonElement} for complex objects.
 *
 * <h2>Groups</h2>
 * <p>Use {@link #group(String)} or {@link #group(String, Consumer)} to create nested
 * structures within the event. Groups can contain their own fields and sub-groups.
 *
 * <h2>Error Handling</h2>
 * <p>Use {@link #error(Throwable)} to record exceptions. The full cause chain is captured
 * and serialized as a JSON array.
 *
 * <h2>Timing</h2>
 * <p>Start time is captured when the writer is created. End time is captured when
 * {@link #close()} is called. Duration is calculated automatically.
 *
 * @see com.felipedidio.logging.WideEventEmitter#begin()
 * @see WideEventGroup
 */
public class WideEventWriter implements AutoCloseable {
    private final JsonObject fields;
    private final Map<String, WideEventWriter> groups;

    private final Instant startTime;
    private Instant endTime;

    private @Nullable Throwable error;

    /**
     * Creates a new event writer with the current time as the start time.
     */
    public WideEventWriter() {
        this.fields = new JsonObject();
        this.groups = new HashMap<>();

        this.startTime = Instant.now();
    }

    /**
     * Sets a string field on this event.
     *
     * @param fieldName the field name
     * @param fieldValue the string value
     * @return this writer for method chaining
     */
    public WideEventWriter set(String fieldName, String fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    /**
     * Sets a numeric field on this event.
     *
     * @param fieldName the field name
     * @param fieldValue the numeric value (int, long, double, etc.)
     * @return this writer for method chaining
     */
    public WideEventWriter set(String fieldName, Number fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    /**
     * Sets a boolean field on this event.
     *
     * @param fieldName the field name
     * @param fieldValue the boolean value
     * @return this writer for method chaining
     */
    public WideEventWriter set(String fieldName, Boolean fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    /**
     * Sets a character field on this event.
     *
     * @param fieldName the field name
     * @param fieldValue the character value
     * @return this writer for method chaining
     */
    public WideEventWriter set(String fieldName, Character fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    /**
     * Sets a JSON element field on this event.
     *
     * <p>Use this for complex objects that are already represented as JSON.
     *
     * @param fieldName the field name
     * @param fieldValue the JSON element value
     * @return this writer for method chaining
     */
    public WideEventWriter set(String fieldName, JsonElement fieldValue) {
        fields.add(fieldName, fieldValue);
        return this;
    }

    /**
     * Creates or retrieves a nested group with the specified name.
     *
     * <p>If a group with this name already exists, it is returned.
     * Otherwise, a new group is created.
     *
     * <p>When using this method directly, remember to close the group
     * or use it within a try-with-resources block to capture timing:
     *
     * <pre>{@code
     * try (var response = event.group("response")) {
     *     response.set("status", 200);
     *     response.set("body", jsonBody);
     * }
     * }</pre>
     *
     * @param groupName the name for the nested group
     * @return the event writer for the group
     * @see #group(String, Consumer)
     */
    public WideEventWriter group(String groupName) {
        return groups.computeIfAbsent(groupName, key -> new WideEventWriter());
    }

    /**
     * Creates a nested group and populates it using a callback.
     *
     * <p>The group is automatically closed after the consumer completes,
     * capturing the end time for the group.
     *
     * <pre>{@code
     * event.group("request", req -> req
     *     .set("method", "POST")
     *     .set("path", "/api/users")
     *     .set("body_size", 1024)
     * );
     * }</pre>
     *
     * @param groupName the name for the nested group
     * @param consumer the callback to populate the group
     * @return this writer for method chaining
     */
    public WideEventWriter group(String groupName, Consumer<WideEventWriter> consumer) {
        try (WideEventWriter group = group(groupName)) {
            consumer.accept(group);
        }
        return this;
    }

    /**
     * Records an error on this event.
     *
     * <p>The full cause chain of the throwable is captured and serialized
     * as a JSON array when the event is emitted. Each cause includes
     * the exception type and message.
     *
     * @param error0 the throwable to record
     * @return this writer for method chaining
     */
    public WideEventWriter error(@NotNull Throwable error0) {
        this.error = error0;
        return this;
    }

    /**
     * Closes this writer and captures the end time.
     *
     * <p>For writers obtained from {@link com.felipedidio.logging.WideEventEmitter#begin()},
     * closing also triggers event emission to all configured sinks.
     */
    @Override
    public void close() {
        endTime = Instant.now();
    }

    /**
     * Returns the fields set on this writer.
     *
     * @return the fields as a JSON object
     */
    public @NotNull JsonObject getFields() {
        return fields;
    }

    /**
     * Returns the nested groups as immutable {@link WideEventGroup} instances.
     *
     * @return a map of group names to their completed groups
     */
    public Map<String, WideEventGroup> getGroups() {
        Map<String, WideEventGroup> finalGroups = new HashMap<>();
        for (var entry : groups.entrySet()) {
            String key = entry.getKey();
            WideEventWriter value = entry.getValue();
            WideEventGroup group = value.toWideEventGroup();
            finalGroups.put(key, group);
        }
        return finalGroups;
    }

    /**
     * Returns the time when this writer was created.
     *
     * @return the start time
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Returns the time when this writer was closed.
     *
     * @return the end time, or {@code null} if not yet closed
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Returns whether an error has been recorded on this writer.
     *
     * @return {@code true} if an error was recorded
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * Returns the recorded error, if any.
     *
     * @return the error, or {@code null} if none was recorded
     */
    public @Nullable Throwable getError() {
        return error;
    }

    private @NotNull WideEventGroup toWideEventGroup() {
        Map<String, WideEventGroup> groups0 = this.getGroups();
        return new WideEventGroup(fields, groups0, startTime, endTime, error);
    }
}
