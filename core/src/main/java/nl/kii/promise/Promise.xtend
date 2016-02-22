package nl.kii.promise

import nl.kii.stream.Stream
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import nl.kii.stream.options.StreamOptions

class Promise<T> extends BasePromise<T, T> {

	new(StreamOptions options) {
		super(options.copy)
	}

	new() {
		super(Stream.DEFAULT_STREAM_OPTIONS.copy)
	}

	new(T t) {
		super(Stream.DEFAULT_STREAM_OPTIONS.copy)
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
