package nl.kii.async.promise

import com.google.common.util.concurrent.AbstractFuture
import nl.kii.async.observable.Observer

/**
 * Converts any promise into a Future for legacy and blocking code
 */
class PromisedFuture<IN, OUT> extends AbstractFuture<OUT> {
	
	new(Promise<IN, OUT> promise) {
		promise.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				set(value)
			}
			
			override error(IN in, Throwable t) {
				setException(t)
			}
			
			override complete() {
				// do nothing
			}
			
		}
	}
	
}
