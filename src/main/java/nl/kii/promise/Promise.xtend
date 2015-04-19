package nl.kii.promise

import nl.kii.promise.internal.FixedBasePromise
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

class Promise<T> extends FixedBasePromise<T, T> {
	
	override getInput() { this }
	
	new() { }
	
	new(T t) { set(t) }
	
	/** set the promised value */
	override set(T value) {
		if(value == null) throw new NullPointerException('cannot promise a null value')
		apply(new Value(value, value))
	}

	/** report an error to the listener of the promise. */
	override error(Throwable t) {
		apply(new Error(null, t))
		this
	}
	
}
