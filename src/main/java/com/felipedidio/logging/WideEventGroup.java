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

public class WideEventGroup {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private final @NotNull JsonObject fields;
    private final @NotNull Map<String, WideEventGroup> groups;

    private final @NotNull Instant startTime;
    private final @NotNull Instant endTime;

    private final @NotNull Duration duration;

    private final @Nullable Throwable error;

    public WideEventGroup(@NotNull JsonObject fields0, @NotNull Map<String, @NotNull WideEventGroup> groups0, @NotNull Instant startTime0,
                          @NotNull Instant endTime0, @Nullable Throwable error0) {
        this.fields = fields0;
        this.groups = groups0;
        this.startTime = startTime0;
        this.endTime = endTime0;
        this.duration = Duration.between(startTime0, endTime0);
        this.error = error0;
    }

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

    public JsonObject getFields() {
        return fields;
    }

    public Map<String, WideEventGroup> getGroups() {
        return groups;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean hasError() {
        return error != null;
    }

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

    public @Nullable String getFieldAsString(String path) {
        JsonElement element = getField(path);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        return null;
    }

    public @Nullable Number getFieldAsNumber(String path) {
        JsonElement element = getField(path);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsNumber();
        }
        return null;
    }

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
