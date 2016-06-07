package nl.kii.async.promise

/**
 * An input promises a value at some point in time.
 * An input is a Deferred, meaning it is thread-safe.
 */
class Input<IN> extends Deferred<IN, IN> implements Promise<IN, IN> {
	
	/** Create a new unfulfilled promise */
	new() { }
	
	/** Create an already completed promise */
	new(IN value) {
		set(value)
	}
	
	/** Create an already failed promise */
	new(Throwable t) {
		error(t)
	}
	
	/** Set the value of the input */
	def set(IN value) {
		value(value, value)
	}
	
	/** Tell the input that the promise has failed to deliver, and why */
	def error(Throwable t) {
		error(null, t)
	}
	
}
