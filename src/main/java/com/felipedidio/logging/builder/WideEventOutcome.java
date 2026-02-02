package com.felipedidio.logging.builder;

/**
 * Represents the outcome of a filter function decision for an event.
 *
 * <p>Filter functions return one of these outcomes to indicate how an event
 * should be processed:
 *
 * <ul>
 *   <li>{@link #KEEP} - The event is always stored</li>
 *   <li>{@link #SAMPLE} - The event is stored with probability equal to the configured sample rate</li>
 *   <li>{@link #DISCARD} - The event is never stored</li>
 * </ul>
 *
 * <h2>Precedence</h2>
 * <p>When combining multiple filter decisions, outcomes have the following precedence:
 * <ul>
 *   <li>{@code DISCARD > SAMPLE > KEEP} (for {@link #min})</li>
 *   <li>{@code KEEP > SAMPLE > DISCARD} (for {@link #max})</li>
 * </ul>
 *
 * @see WideEventFilterFunction
 */
public enum WideEventOutcome {
    /**
     * The event should always be stored, regardless of sample rate.
     */
    KEEP,

    /**
     * The event should be stored with probability equal to the configured sample rate.
     */
    SAMPLE,

    /**
     * The event should never be stored.
     */
    DISCARD;

    /**
     * Returns the more restrictive of this outcome and another.
     *
     * <p>Precedence: {@code DISCARD > SAMPLE > KEEP}
     *
     * @param other the other outcome to compare
     * @return the more restrictive outcome
     */
    public WideEventOutcome min(WideEventOutcome other)
    {
        return switch (this) {
        case KEEP -> other;
        case SAMPLE -> {
            if (other == KEEP) {
                yield SAMPLE;
            } else {
                yield other;
            }
        }
        case DISCARD -> DISCARD;
        };
    }

    /**
     * Returns the less restrictive of this outcome and another.
     *
     * <p>Precedence: {@code KEEP > SAMPLE > DISCARD}
     *
     * @param other the other outcome to compare
     * @return the less restrictive outcome
     */
    public WideEventOutcome max(WideEventOutcome other)
    {
        return switch (this) {
        case KEEP -> KEEP;
        case SAMPLE -> {
            if (other == DISCARD) {
                yield SAMPLE;
            } else {
                yield other;
            }
        }
        case DISCARD -> other;
        };
    }
}
