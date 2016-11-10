package nl.kii.async.promise

import co.paralleluniverse.fibers.Suspendable

/**
 * An input promises a value at some point in time.
 * An input is a Deferred, meaning it is thread-safe.
 */
class Input<IN> extends Deferred<IN, IN> implements Promise<IN, IN> {
	
	/** Create a new unfulfilled promise */
	new() { }
	
	/** Create an already completed promise */
	new(IN value) {
		cachedValue = value -> value
	}
	
	/** Create an already failed promise */
	new(Throwable t) {
		cachedError = null -> t
	}
	
	/** Set the value of the input */
	@Suspendable
	def set(IN value) {
		value(value, value)
	}
	
	/** Tell the input that the promise has failed to deliver, and why */
	@Suspendable
	def error(Throwable t) {
		error(null, t)
	}
	
}
