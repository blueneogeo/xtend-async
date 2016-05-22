package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Stream methods annotated with backpressure will wait to send you items until you are ready to handle them.
 * You ask for the next item by calling stream.next. (this is done automatically for you when you use stream.start)
 */
@Target(METHOD)
annotation Backpressure {
	
}
