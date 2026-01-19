package temp;

import com.felipedidio.logging.WideEvent;
import com.felipedidio.logging.WideEventEmitter;
import com.felipedidio.logging.builder.WideEventOutcome;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.Objects;

public class MyWideEvent {
    // Parameters
    private static final double SAMPLE_RATE = 1.0;
    private static final int KEEP_AT_STARTUP_COUNT = 0;

    // Emitter
    public static final Path LOGGING_DIRECTORY = Path.of("logs");
    public static final WideEventEmitter EMITTER = WideEventEmitter.builder("my-emitter", LOGGING_DIRECTORY)
            .parameter("long-events", 500L)
            .parameter("keep-at-startup", KEEP_AT_STARTUP_COUNT)
            .sampleRate(SAMPLE_RATE)
            .filter(MyWideEvent::filter)
            .build();

    // Filter function
    private static WideEventOutcome filter(WideEventEmitter emitter, WideEvent event) {
        // Keep the first 100 events after the service starts.
        int keepAtStartupCount = emitter.getParameters().get("keep-at-startup").getAsInt();
        if (event.getLocalId() < keepAtStartupCount) {
            return WideEventOutcome.KEEP;
        }

        // If the duration is higher than 500ms, always keep it.
        long longEvents = emitter.getParameters().get("long-events").getAsLong();
        if (event.getDuration().toMillis() > longEvents) {
            return WideEventOutcome.KEEP;
        }

        // Keep all errors.
        if (event.hasError()) {
            return WideEventOutcome.KEEP;
        }

        JsonObject fields = event.getFields();
        JsonElement user = fields.get("user");
        if (user != null && user.isJsonObject()) {
            JsonElement subscription = user.getAsJsonObject().get("subscription");
            if (subscription != null && subscription.isJsonPrimitive() && subscription.getAsJsonPrimitive().isString()) {
                String userSubscription = subscription.getAsString();
                if (Objects.equals(userSubscription, "premium")) {
                    return WideEventOutcome.KEEP;
                }
            }
        }

        // Sample all the other events
        return WideEventOutcome.SAMPLE;
    }
}
