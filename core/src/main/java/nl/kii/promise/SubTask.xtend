package nl.kii.promise

import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import nl.kii.async.options.AsyncOptions

/** 
 * A Task is a promise that some task gets done. 
 * It has no result, it can just be completed or have an error.
 * A SubTask is a task based on a promise/task.
 */
class SubTask<I> extends SubPromise<I, Boolean> {
	
	new(AsyncOptions options) {
		super(options.copy)
	}
	
	def complete(I from) {
		apply(new Value(from, true))
	}
	
	override toString() {
		val error = switch it : get.head {
			Error<?, ?>: it
			default: null
		}
		'''Task { fulfilled: «fulfilled» «IF error != null», error: «error»«ENDIF» }'''
	}

}
