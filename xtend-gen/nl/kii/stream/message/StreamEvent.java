package nl.kii.stream.message;

import nl.kii.stream.message.StreamMessage;

/**
 * A command given to a stream.
 * Commands travel upwards towards the source of a stream, to control the stream.
 */
public interface StreamEvent extends StreamMessage {
}
