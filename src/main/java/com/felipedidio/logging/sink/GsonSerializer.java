package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

/**
 * Utility class for serializing {@link WideEvent} instances to JSON.
 *
 * <p>Uses GSON with HTML escaping disabled for compact JSON output.
 */
public class GsonSerializer {
    private static final Gson COMPACT_JSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /**
     * Serializes a wide event to a JSON string.
     *
     * @param event the event to serialize
     * @return the JSON string representation
     * @throws JsonIOException if serialization fails
     */
    public static String serializeWideEvent(WideEvent event) throws JsonIOException {
        JsonObject json = event.toJson();
        return COMPACT_JSON.toJson(json);
    }

    /**
     * Serializes a wide event to JSON, writing to the specified appendable.
     *
     * @param event the event to serialize
     * @param appendable the destination for the JSON output
     * @throws JsonIOException if serialization fails
     */
    public static void serializeWideEvent(WideEvent event, Appendable appendable) throws JsonIOException {
        JsonObject json = event.toJson();
        COMPACT_JSON.toJson(json, appendable);
    }
}
