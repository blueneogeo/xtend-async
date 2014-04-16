package nl.kii.stream

import com.google.common.util.concurrent.AbstractFuture
import java.util.concurrent.atomic.AtomicBoolean

class PromiseFuture<T> extends AbstractFuture<T> {
	
	val _cancelled = new AtomicBoolean(false)
	
	new(Promise<T> promise) {
		promise
			.onError [ this.setException(it) ]
			.then [ this.set(it) ]
	}
	
}