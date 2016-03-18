package nl.kii.promise

import nl.kii.async.options.AsyncOptions
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import nl.kii.async.options.AsyncDefault

class Promise<T> extends BasePromise<T, T> {

	new(AsyncOptions options) {
		super(options.copy)
	}

	new() {
		super(AsyncDefault.options)
	}

	new(T t) {
		super(AsyncDefault.options)
		set(t)
	}

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
