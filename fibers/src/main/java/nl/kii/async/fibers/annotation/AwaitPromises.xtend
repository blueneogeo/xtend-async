package nl.kii.async.fibers.annotation

import java.lang.annotation.Repeatable
import java.lang.annotation.Target
import org.eclipse.xtend.lib.macro.Active
import nl.kii.async.fibers.annotation.processors.AwaitPromisesProcessor

/** 
 * Copy methods from the value class that return promises as 
 * methods that await using a Fiber, into the annotated class.
 */
@Target(TYPE)
@Repeatable(AwaitPromisesValues)
@Active(AwaitPromisesProcessor)
annotation AwaitPromises {

	/** The class to copy methods from */
	Class<?> value
	
	/** Do we also pick up methods from supertypes? */
	boolean includeSuperTypes = false

	/** Do we create extension methods for instance methods? */
	boolean createExtensionMethods = true
	
	/** Also copy any other static methods into the new class? */
	boolean copyOtherStaticMethods = true
	
	/** 
	 * Method signatures to ignore.
	 * Use the format "[methodname]([fulltypename],...)" (from MethodSignature.toString())
	 * For example: "equals(java.lang.Object)"
	 */
	String[] ignoredMethods = #[]

	/** Add this text in front of new promise returning methods */
	String promiseMethodNameBegin = 'do'

	/** Add this text in front of new stream returning methods */
	String streamMethodNameBegin = 'each'

	/** Remove any text from method names that match this regular expression */
	String methodNameStrip = 'Async|HandlerStream|Stream'
	
}

@Target(TYPE)
annotation AwaitPromisesValues {
	
	AwaitPromises[] value
	
}
