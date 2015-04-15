package nl.kii.promise.internal

import nl.kii.promise.IPromise
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

/** 
 * A Task is a promise that some task gets done. 
 * It has no result, it can just be completed or have an error.
 * A SubTask is a task based on a promise/task.
 */
class SubTask<I> extends SubPromise<I, Boolean> {
	
	new() { super() }
	
	new(IPromise<I, ?> parentPromise) {
		super(parentPromise)
	}
	
	def complete(I from) {
		apply(new Value(from, true))
	}
	
	override toString() '''Task { fulfilled: «fulfilled» «IF get instanceof Error<?, ?>», error: «(get as Error<?, ?>).error»«ENDIF» }'''

}
