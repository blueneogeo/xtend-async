package nl.kii.promise

import nl.kii.stream.message.Error

/** A Task is a promise that some task gets done. It has no result, it can just be completed or have an error. */
class Task extends Promise<Boolean> {
	
	def complete() {
		set(true)
	}

	override toString() {
		val error = switch it : get.head {
			Error<?, ?>: it
			default: null
		}
		'''Task { fulfilled: «fulfilled» «IF error != null», error: «error»«ENDIF» }'''
	}
	
}
