package nl.kii.async.fibers.annotation

import java.lang.annotation.Target
import nl.kii.async.fibers.annotation.processors.SyncVersionOfProcessor
import org.eclipse.xtend.lib.macro.Active

/** Create a wrapper version of the class that replaces methods annotated with Async with synchronous ones. */
@Active(SyncVersionOfProcessor)
@Target(TYPE)
annotation SyncVersionOf {
	
	/** The class to be made a synchronous version of */
	Class<?> value
	
}
