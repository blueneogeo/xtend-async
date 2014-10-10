package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

interface StreamObserver<R, T> {
	
	/** handle an incoming value */
	def void onValue(R from, T value)
	
	/** handle an incoming error */
	def void onError(R from, Throwable t)
	
	/** handle an imcoming finish of a given level */
	def void onFinish(R from, int level)
	
	/** handle the stream being closed */
	def void onClosed()
	
}

/** Lets you create builders for handling the entries coming from a stream */
interface StreamHandler<R, T> {
	
	/** handle each incoming value. remember to call stream.next after handling a value! */
	def void each((R, T)=>void handler)
	
	/** handle each incoming error. remember to call stream.next after handling an error! */
	def void error((R, Throwable)=>void handler)
	
	/** handle each incoming finish. remember to call stream.next after handling a finish! */
	def void finish((R, Integer)=>void handler)
	
	/** handled that the stream has closed. */
	def void closed((Void)=>void stream)
	
}

/**
 * A basic builder for asynchronous stream listening.
 * Combine with StreamExtensions.on like this:
 * <p>
 * <pre>
 * stream.on [
 *    each [ ... stream.next ]
 *    finish [ ... ]
 *    error [ ... ]
 * ]
 * stream.next
 * </pre>
 * <p>
 * Remember to call stream.next to start the stream!
 */
class StreamHandlerBuilder<R, T> implements StreamHandler<R, T>, StreamObserver<R, T> {
	
	public val IStream<R, T> stream
	
	@Atomic Procedure2<R, T> valueFn
	@Atomic Procedure2<R, Throwable> errorFn
	@Atomic Procedure2<R, Integer> finishFn
	@Atomic Procedure1<Void> closedFn

	new(IStream<R, T> stream) {
		this.stream = stream
	}

	// BUILDER METHODS ////////////////////////////////////////////////////////

	/** listen for each incoming value */
	override each((R, T)=>void handler) {
		this.valueFn = handler
	}
	
	/** listen for any finish */
	override finish((R, Integer)=>void handler) {
		this.finishFn = handler
	}

	/** listen for any uncaught errors */
	override error((R, Throwable)=>void handler) {
		this.errorFn = handler
	}
	
	/** listen for when the stream closes */
	override closed((Void)=>void handler) {
		this.closedFn = handler
	}
	
	// STREAMOBSERVER IMPLEMENTATION //////////////////////////////////////////
	
	override onValue(R from, T value) {
		valueFn?.apply(from, value)
	}
	
	override onError(R from, Throwable t) {
		errorFn?.apply(from, t)
	}
	
	override onFinish(R from, int level) {
		finishFn?.apply(from, level)
	}
	
	override onClosed() {
		closedFn?.apply(null)
	}
	
}
