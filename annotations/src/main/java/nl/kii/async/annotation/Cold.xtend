package nl.kii.async.annotation

import java.lang.annotation.Target

/** Stream and promise methods annotated with Cold will not yield values until next or start is called on them.  */
@Target(METHOD)
annotation Cold {
	
}
