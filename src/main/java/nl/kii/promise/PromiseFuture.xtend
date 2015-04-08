package nl.kii.promise

import com.google.common.util.concurrent.AbstractFuture

/**
 * Converts a promise into a future for legacy code.
 */
class PromiseFuture<T> extends AbstractFuture<T> {
	
	new(IPromise<?, T> promise) {
		promise
			.onError [ this.setException(it) ]
			.then [ this.set(it) ]
	}
	
}
