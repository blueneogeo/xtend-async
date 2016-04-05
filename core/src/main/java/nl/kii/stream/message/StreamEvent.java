package nl.kii.stream.message;

/**
 * Stream events travel up from the consumer of the stream to control and inform the stream.
 */
public enum StreamEvent implements StreamMessage {

	next, overflow, pause, resume, close;
	
}
