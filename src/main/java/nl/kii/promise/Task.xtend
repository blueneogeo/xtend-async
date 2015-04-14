package nl.kii.promise

import nl.kii.stream.message.Error

/** A Task is a promise that some task gets done. It has no result, it can just be completed or have an error. */
class Task extends Promise<Boolean> {
	
	def Task complete() {
		set(true)
		this
	}

	override toString() '''Task { fulfilled: «fulfilled» «IF get instanceof Error<?, ?>», error: «(get as Error<?, ?>).error»«ENDIF» }'''
	
}
