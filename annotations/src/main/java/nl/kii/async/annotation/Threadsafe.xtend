package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Methods annotated with Threadsafe are guaranteed to return a threadsafe 
 * stream, and make safe a non-threadsafe stream and/or parameters.
 */
@Target(METHOD)
annotation Threadsafe {
	
}
