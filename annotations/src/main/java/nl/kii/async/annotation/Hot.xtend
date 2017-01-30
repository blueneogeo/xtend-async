package nl.kii.async.annotation

import java.lang.annotation.Target

/** Stream and promise methods annotated with Hot can start yielding values immediately, even before you start them or listen for them.  */
@Target(METHOD)
annotation Hot {
	
}
