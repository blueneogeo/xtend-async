package nl.kii.stream.message;

/**
 * Stream events travel up from the consumer of the stream to control and inform the stream.
 */
public enum StreamEvents implements StreamEvent {

	next, pause, resume, close;
	
}
