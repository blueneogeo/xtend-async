package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Methods annotated with multithreaded run their work on another thread.
 */
@Target(METHOD)
annotation MultiThreaded {
	
}
