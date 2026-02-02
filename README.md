# f-wide-events

A structured event logging library for Java applications that provides a fluent API for capturing rich contextual event data with hierarchical structure, timing information, error tracking, and filtering/sampling capabilities.

## Features

- **Fluent API** - Chain method calls for clean, readable event construction
- **Hierarchical Events** - Organize data into nested groups for complex event structures
- **Automatic Timing** - Start and end times captured automatically with duration calculation
- **Error Tracking** - Capture exceptions with full cause chain as structured JSON
- **Filtering & Sampling** - Control which events are stored with custom filter functions and sample rates
- **Multiple Sinks** - Output to files, console, or custom destinations
- **Thread-Safe** - File sinks use synchronization for safe concurrent writes

## Installation

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.felipedidio:f-wide-events:1.0.0'
}
```

## Quick Start

### 1. Create an Emitter

Create an emitter once (typically as a static singleton):

```java
import com.felipedidio.logging.WideEventEmitter;
import java.nio.file.Path;

WideEventEmitter emitter = WideEventEmitter.builder("http-requests", Path.of("logs"))
    .parameter("service", "my-api")
    .parameter("version", "1.0.0")
    .sampleRate(1.0)  // 100% of events
    .addConsoleSink() // Also output to console
    .build();
```

### 2. Write Events

Use try-with-resources to automatically emit events when the block closes:

```java
try (var event = emitter.begin()) {
    event.set("method", "GET");
    event.set("path", "/api/users");
    event.set("status", 200);
    event.set("user_id", userId);
}
// Event is automatically emitted here
```

### 3. Use Nested Groups

Organize related fields into hierarchical groups:

```java
try (var event = emitter.begin()) {
    event.set("method", request.getMethod());
    event.set("path", request.getPath());

    // Using callback style (auto-closes group)
    event.group("request", req -> req
        .set("content_type", request.getContentType())
        .set("body_size", request.getBodySize())
    );

    // Using direct style
    try (var response = event.group("response")) {
        response.set("status", 200);
        response.set("body", responseBody);
    }
}
```

### 4. Handle Errors

Capture exceptions with full cause chain:

```java
try (var event = emitter.begin()) {
    event.set("operation", "process_payment");
    try {
        processPayment(paymentId);
        event.set("success", true);
    } catch (PaymentException e) {
        event.error(e);
        event.set("success", false);
    }
}
```

## Configuration

### Builder Options

| Method | Description | Default |
|--------|-------------|---------|
| `parameter(name, value)` | Add metadata accessible via `emitter.getParameters()` | Empty |
| `sampleRate(double)` | Probability (0.0-1.0) for sampled events | 1.0 |
| `filter(function)` | Custom filter function for event decisions | None |
| `addFileSink(path)` | Add file output destination | Auto-added to `{logsDir}/{name}.log` |
| `addConsoleSink()` | Add console output (stdout/stderr) | Not added |
| `addSink(sink)` | Add custom sink implementation | None |

### Custom Filtering

Create a filter function to control which events are kept, sampled, or discarded:

```java
WideEventEmitter emitter = WideEventEmitter.builder("requests", Path.of("logs"))
    .sampleRate(0.1)  // Sample 10% of SAMPLE events
    .filter((em, event) -> {
        // Always keep errors
        if (event.hasError()) {
            return WideEventOutcome.KEEP;
        }

        // Always keep slow requests
        if (event.getDuration().toMillis() > 500) {
            return WideEventOutcome.KEEP;
        }

        // Sample everything else
        return WideEventOutcome.SAMPLE;
    })
    .build();
```

### Filter Outcomes

| Outcome | Behavior |
|---------|----------|
| `KEEP` | Always store the event |
| `SAMPLE` | Store with probability equal to `sampleRate` |
| `DISCARD` | Never store the event |

## Event JSON Structure

Events are serialized to newline-delimited JSON:

```json
{
  "method": "GET",
  "path": "/api/users",
  "status": 200,
  "request": {
    "content_type": "application/json",
    "start_time": "2026-01-19T10:00:00.000Z",
    "end_time": "2026-01-19T10:00:00.005Z",
    "duration_ms": 5,
    "error": false
  },
  "start_time": "2026-01-19T10:00:00.000Z",
  "end_time": "2026-01-19T10:00:00.050Z",
  "duration_ms": 50,
  "error": false,
  "event_name": "http-requests",
  "local_id": 0,
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Error Structure

When an error is recorded, the cause chain is captured:

```json
{
  "error": true,
  "error_cause": [
    {
      "error_type": "com.example.PaymentException",
      "error_message": "Payment declined"
    },
    {
      "error_type": "java.io.IOException",
      "error_message": "Connection timeout"
    }
  ]
}
```

## Examples

See the [f-wide-events-examples](https://github.com/ofelipedidio/f-wide-events-examples) project for complete usage examples.

## License

MIT License - see [LICENSE](LICENSE) for details.
