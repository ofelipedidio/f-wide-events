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

public class WideEventWriter implements AutoCloseable {
    private final JsonObject fields;
    private final Map<String, WideEventWriter> groups;

    private final Instant startTime;
    private Instant endTime;

    private @Nullable Throwable error;

    public WideEventWriter() {
        this.fields = new JsonObject();
        this.groups = new HashMap<>();

        this.startTime = Instant.now();
    }

    public WideEventWriter set(String fieldName, String fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    public WideEventWriter set(String fieldName, Number fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    public WideEventWriter set(String fieldName, Boolean fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    public WideEventWriter set(String fieldName, Character fieldValue) {
        fields.addProperty(fieldName, fieldValue);
        return this;
    }

    public WideEventWriter set(String fieldName, JsonElement fieldValue) {
        fields.add(fieldName, fieldValue);
        return this;
    }

    public WideEventWriter group(String groupName) {
        return groups.computeIfAbsent(groupName, key -> new WideEventWriter());
    }

    public WideEventWriter group(String groupName, Consumer<WideEventWriter> consumer) {
        try (WideEventWriter group = group(groupName)) {
            consumer.accept(group);
        }
        return this;
    }

    public WideEventWriter error(@NotNull Throwable error0) {
        this.error = error0;
        return this;
    }

    @Override
    public void close() {
        endTime = Instant.now();
    }

    public @NotNull JsonObject getFields() {
        return fields;
    }

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

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public boolean hasError() {
        return error != null;
    }

    public @Nullable Throwable getError() {
        return error;
    }

    private @NotNull WideEventGroup toWideEventGroup() {
        Map<String, WideEventGroup> groups0 = this.getGroups();
        return new WideEventGroup(fields, groups0, startTime, endTime, error);
    }
}
