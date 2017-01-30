package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Stream methods annotated with uncontrolled do not support you asking next for the next value from the stream,
 * and will push values at the listener at their own leisure.
 */
@Target(METHOD)
annotation Uncontrolled {
	
}
