package nl.kii.stream;

import nl.kii.stream.StreamMessage;

/**
 * A command given to a stream.
 * Commands travel upwards towards the source of a stream, to control the stream.
 */
@SuppressWarnings("all")
public interface StreamNotification extends StreamMessage {
}
