package com.felipedidio.logging.writer;

import com.felipedidio.logging.WideEventArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A mutable builder for constructing arrays of wide event items.
 *
 * <p>Use this class within a try-with-resources block to ensure proper timing capture:
 *
 * <pre>{@code
 * try (var arr = event.array("items")) {
 *     try (var item = arr.item()) {
 *         item.set("id", 1);
 *         item.set("status", "success");
 *     }
 *     try (var item = arr.itemUntimed()) {
 *         item.set("id", 2);
 *     }
 * }
 * }</pre>
 *
 * <h2>Timed vs Untimed Items</h2>
 * <ul>
 *   <li>{@link #item()} / {@link #itemTimed()} - Items include timing fields in JSON output</li>
 *   <li>{@link #itemUntimed()} - Items omit timing fields in JSON output</li>
 * </ul>
 *
 * @see WideEventWriter#array(String)
 * @see WideEventArray
 */
public class ArrayEventWriter implements AutoCloseable {
    private final Object lock = new Object();

    private final List<ArrayItem> items;
    private final Instant startTime;
    private volatile Instant endTime;

    /**
     * Creates a new array writer with the current time as the start time.
     */
    public ArrayEventWriter() {
        this.items = new ArrayList<>();
        this.startTime = Instant.now();
    }

    /**
     * Creates a new timed item in this array.
     *
     * <p>The returned writer should be used within a try-with-resources block.
     * When closed, timing information will be included in the item's JSON output.
     *
     * @return a new event writer for the item
     */
    public WideEventWriter itemTimed() {
        WideEventWriter writer = new WideEventWriter();
        synchronized (lock) {
            items.add(new ArrayItem(writer, true));
        }
        return writer;
    }

    /**
     * Creates a new untimed item in this array.
     *
     * <p>The returned writer should be used within a try-with-resources block.
     * Timing information will be omitted from the item's JSON output.
     *
     * @return a new event writer for the item
     */
    public WideEventWriter itemUntimed() {
        WideEventWriter writer = new WideEventWriter();
        synchronized (lock) {
            items.add(new ArrayItem(writer, false));
        }
        return writer;
    }

    /**
     * Creates a new timed item in this array.
     *
     * <p>This is an alias for {@link #itemTimed()}.
     *
     * @return a new event writer for the item
     */
    public WideEventWriter item() {
        return itemTimed();
    }

    /**
     * Closes this array writer and captures the end time.
     */
    @Override
    public void close() {
        endTime = Instant.now();
    }

    /**
     * Converts this writer to an immutable {@link WideEventArray}.
     *
     * @return the array data
     */
    @NotNull WideEventArray toWideEventArray() {
        List<JsonObject> serializedItems;
        synchronized (lock) {
            serializedItems = new ArrayList<>(items.size());
            for (ArrayItem item : items) {
                JsonObject json = item.toJson();
                serializedItems.add(json);
            }
        }

        Instant endTime0 = this.endTime;
        if (endTime0 == null) {
            endTime0 = Instant.now();
        }

        return new WideEventArray(serializedItems, startTime, endTime0);
    }

    /**
     * Internal record to track items and their timing preference.
     */
    private record ArrayItem(WideEventWriter writer, boolean timed) {
        JsonObject toJson() {
            if (timed) {
                return writer.toWideEventGroup().toJson();
            } else {
                return writer.getFields();
            }
        }
    }
}
