package nl.kii.promise

import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import nl.kii.async.options.AsyncOptions

class SubPromise<I, O> extends BasePromise<I, O> {
	
	new(IPromise<?, ?> parent) {
		super(parent.options.copy)
	}
	
	new(AsyncOptions options) {
		super(options.copy)
	}
	
	def void set(I from, O value) { apply(new Value(from, value)) }

	def void error(I from, Throwable t) { apply(new Error(from, t)) }
	
}
