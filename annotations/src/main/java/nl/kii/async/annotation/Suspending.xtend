package nl.kii.async.annotation

import java.lang.annotation.Documented
import java.lang.annotation.Target

/**
 * Indicates that the methods inside the class that you pass are / have to be marked Suspendable,
 * and that if this method uses a Fiber, you can run fiber-blocking code on it.
 * <p>
 * This annotation does not add any functionality but simply exists to assure you that you may
 * use suspendable code.
 * <p>
 * Note: If you pass a (anonymous) class instance here, make sure that the methods are marked Suspendable.
 */
@Documented
@Target(PARAMETER)
annotation Suspending {
	
}
