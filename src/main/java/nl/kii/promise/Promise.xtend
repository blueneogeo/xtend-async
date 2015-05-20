package nl.kii.promise

import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

class Promise<T> extends BasePromise<T, T> {
	
//	override getInput() { this }
	
	new() { }
	
	new(T t) { set(t) }
	
	/** set the promised value */
	def set(T value) {
		if(value == null) throw new NullPointerException('cannot promise a null value')
		apply(new Value(value, value))
	}

	/** report an error to the listener of the promise. */
	def error(Throwable t) {
		apply(new Error(null, t))
	}
	
}
