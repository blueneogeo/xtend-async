package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Stream methods annotated with NoBackpressure mean that you lose backpressure handling by using this stream.
 * <p>
 * Backpressure means you can pause and resume a stream. Streams without backpressure cannot be paused and resumed.
 */
@Target(METHOD)
annotation NoBackpressure {
	
}
