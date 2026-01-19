package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

public class GsonSerializer {
    private static final Gson COMPACT_JSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static String serializeWideEvent(WideEvent event) throws JsonIOException {
        JsonObject json = event.toJson();
        return COMPACT_JSON.toJson(json);
    }

    public static void serializeWideEvent(WideEvent event, Appendable appendable) throws JsonIOException {
        JsonObject json = event.toJson();
        COMPACT_JSON.toJson(json, appendable);
    }
}
