package nl.kii.promise

import static extension nl.kii.promise.PromiseExtensions.*
import com.google.common.util.concurrent.AbstractFuture

/**
 * Converts a promise into a future for legacy code.
 */
class PromiseFuture<T> extends AbstractFuture<T> {
	
	new(IPromise<?, T> promise) {
		promise
			.on(Throwable) [ this.setException(it) ]
			.then [ this.set(it) ]
	}
	
}
