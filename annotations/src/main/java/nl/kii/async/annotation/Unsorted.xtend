package nl.kii.async.annotation

import java.lang.annotation.Target

/**
 * Streams from methods annotated with Unsorted will not guarantee the sorting of their elements.
 */

@Target(METHOD)
annotation Unsorted {
	
}
