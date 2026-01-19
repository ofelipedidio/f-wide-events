package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;

public interface WideEventSink {
    void write(WideEvent wideEvent);
}
