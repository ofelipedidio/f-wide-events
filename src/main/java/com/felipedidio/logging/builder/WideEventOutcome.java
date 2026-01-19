package com.felipedidio.logging.builder;

public enum WideEventOutcome {
    KEEP, SAMPLE, DISCARD;

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
