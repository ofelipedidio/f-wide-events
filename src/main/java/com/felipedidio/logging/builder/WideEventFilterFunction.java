package com.felipedidio.logging.builder;

import com.felipedidio.logging.WideEvent;
import com.felipedidio.logging.WideEventEmitter;

/**
 * A functional interface for filtering events before they are written to sinks.
 *
 * <p>Implement this interface to create custom filtering logic that determines
 * whether events should be kept, sampled, or discarded.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * WideEventFilterFunction filter = (emitter, event) -> {
 *     // Always keep events with errors
 *     if (event.hasError()) {
 *         return WideEventOutcome.KEEP;
 *     }
 *
 *     // Always keep slow requests (over 500ms)
 *     if (event.getDuration().toMillis() > 500) {
 *         return WideEventOutcome.KEEP;
 *     }
 *
 *     // Sample everything else based on configured sample rate
 *     return WideEventOutcome.SAMPLE;
 * };
 * }</pre>
 *
 * @see WideEventOutcome
 * @see WideEventEmitterBuilder#filter(WideEventFilterFunction)
 */
@FunctionalInterface
public interface WideEventFilterFunction {
    /**
     * Determines the outcome for a completed event.
     *
     * <p>This method is called for each event after it has been closed and
     * before it is written to sinks.
     *
     * @param emitter the emitter that created the event (provides access to parameters)
     * @param wideEvent the completed event (provides access to fields, groups, duration, and error status)
     * @return the outcome determining how the event should be processed
     */
    WideEventOutcome filter(WideEventEmitter emitter, WideEvent wideEvent);
}
