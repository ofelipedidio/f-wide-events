package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;

public class ConsoleSink implements WideEventSink {
    @Override
    public void write(WideEvent wideEvent) {
        String eventJson = GsonSerializer.serializeWideEvent(wideEvent);
        boolean hasError = wideEvent.hasError();
        if (!hasError) {
            System.out.println(eventJson);
        } else {
            System.err.println(eventJson);
        }
    }
}
