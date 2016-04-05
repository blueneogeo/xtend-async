package nl.kii.stream

import nl.kii.async.options.AsyncDefault
import nl.kii.async.options.AsyncOptions
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

class Stream<T> extends BaseStream<T, T> {

	new() { super(AsyncDefault.options.copy) }
	
	new(AsyncOptions options) { super(options.copy) }
	
	/** Queue a value on the stream for pushing to the listener */
	def push(T value) { apply(new Value(value, value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	def error(Throwable error) { apply(new Error(null, error)) }
	 	
}
