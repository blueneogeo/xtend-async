package nl.kii.stream

import com.google.common.util.concurrent.AbstractFuture

class PromiseFuture<T> extends AbstractFuture<T> {
	
	new(Promise<T> promise) {
		promise
			.onError [ this.setException(it) ]
			.then [ this.set(it) ]
	}
	
}
