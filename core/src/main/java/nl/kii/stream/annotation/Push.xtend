package nl.kii.stream.annotation

import java.lang.annotation.Target

/**
 * Streams from methods annotated with Push will push automatically, without next needing to be called.
 */

@Target(METHOD)
annotation Push {
	
}
