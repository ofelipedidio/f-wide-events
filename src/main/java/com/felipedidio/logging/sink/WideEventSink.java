package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;

@FunctionalInterface
public interface WideEventSink {
    void write(WideEvent wideEvent);
}
