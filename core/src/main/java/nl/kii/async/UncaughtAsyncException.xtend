package nl.kii.async

/** 
 * An error that should break out of the stream or promise chain so surrounding code can catch
 * it, or it can be presented in the program output for debugging.
 */
class UncaughtAsyncException extends Exception {

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
