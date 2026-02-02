package com.felipedidio.logging.sink;

import com.felipedidio.logging.WideEvent;

/**
 * A functional interface for writing wide events to an output destination.
 *
 * <p>Implement this interface to create custom event output destinations such as
 * databases, message queues, or remote services.
 *
 * <h2>Implementation Requirements</h2>
 * <ul>
 *   <li>Implementations should be thread-safe if the emitter may be used concurrently</li>
 *   <li>The {@link #write(WideEvent)} method should not throw exceptions; errors should be handled internally</li>
 *   <li>Implementations may implement {@link java.io.Closeable} if they hold resources</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class DatabaseSink implements WideEventSink {
 *     private final DataSource dataSource;
 *
 *     public DatabaseSink(DataSource dataSource) {
 *         this.dataSource = dataSource;
 *     }
 *
 *     @Override
 *     public void write(WideEvent wideEvent) {
 *         try (Connection conn = dataSource.getConnection()) {
 *             // Insert event into database
 *         } catch (SQLException e) {
 *             // Handle error
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see FileSink
 * @see ConsoleSink
 * @see com.felipedidio.logging.builder.WideEventEmitterBuilder#addSink(WideEventSink)
 */
@FunctionalInterface
public interface WideEventSink {
    /**
     * Writes an event to this sink's output destination.
     *
     * <p>This method is called for each event that passes the filter and sampling checks.
     * Implementations should handle errors internally and not throw exceptions.
     *
     * @param wideEvent the event to write
     */
    void write(WideEvent wideEvent);
}
