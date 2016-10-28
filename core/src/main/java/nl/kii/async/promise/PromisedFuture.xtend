package nl.kii.async.promise

import com.google.common.util.concurrent.AbstractFuture

import static extension nl.kii.async.promise.PromiseExtensions.*
import co.paralleluniverse.fibers.instrument.DontInstrument

/**
 * Converts any promise into a Future for legacy and blocking code
 */
@DontInstrument
class PromisedFuture<T> extends AbstractFuture<T> {
	
	new(Promise<?, T> promise) {
		
		promise
			.then [ this.set(it) ]
			.on(Throwable) [ this.setException(it) ]
	}
	
}
