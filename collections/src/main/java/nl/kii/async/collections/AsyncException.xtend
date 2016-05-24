package nl.kii.async.collections

/** 
 * An error when processing async code. This error will break out of streams and promises,
 * instead of being caught by the error handlers of those streams and promises.
 * <b>
 * Use when things go wrong and you need to inform code outside the stream-and promise handlers.
 */
class AsyncException extends RuntimeException {

	val String operation

	new(String operation) {
		super()
		this.operation = operation
	}
	
	new(String operation, Throwable cause) {
		super(cause)
		this.operation = operation
	}
	
}
