package nl.kii.async.promise

import com.google.common.util.concurrent.AbstractFuture

import static extension nl.kii.async.promise.PromiseExtensions.*

/**
 * Converts any promise into a Future for legacy and blocking code
 */
class PromisedFuture<T> extends AbstractFuture<T> {
	
	new(Promise<?, T> promise) {
		
		promise
			.then [ this.set(it) ]
			.on(Throwable) [ this.setException(it) ]
	}
	
}
