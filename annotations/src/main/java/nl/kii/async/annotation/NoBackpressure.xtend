package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Stream methods annotated with uncontrolled mean that you lose backpressure handling by using this stream.
 * <p>
 * This means that while normally streams will wait until you ask 'next' to get the next item from the stream,
 * streams annotated with Uncontrolled may push items to you at their own discretion. Uncontrolled streams
 * do not mind how busy the handlers are (the pressure the handlers are under), and may overload the handlers.
 */
@Target(METHOD)
annotation NoBackpressure {
	
}
