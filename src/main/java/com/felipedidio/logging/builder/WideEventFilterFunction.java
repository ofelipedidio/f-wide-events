package com.felipedidio.logging.builder;

import com.felipedidio.logging.WideEvent;
import com.felipedidio.logging.WideEventEmitter;

@FunctionalInterface
public interface WideEventFilterFunction {
    WideEventOutcome filter(WideEventEmitter emitter, WideEvent wideEvent);
}
