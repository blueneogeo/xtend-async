package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

interface StreamObserver<I, O> {
	
	/** handle an incoming value */
	def void onValue(I from, O value)
	
	/** handle an incoming error */
	def void onError(I from, Throwable t)
	
	/** handle an imcoming finish of a given level */
	def void onFinish(I from, int level)
	
	/** handle the stream being closed */
	def void onClosed()
	
}

/** Lets you create builders for handling the entries coming from a stream */
interface StreamHandler<I, O> {
	
	/** handle each incoming value. remember to call stream.next after handling a value! */
	def void each((I, O)=>void handler)
	
	/** handle each incoming error. remember to call stream.next after handling an error! */
	def void error((I, Throwable)=>void handler)
	
	/** handle each incoming finish. remember to call stream.next after handling a finish! */
	def void finish((I, Integer)=>void handler)
	
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
class StreamObserverBuilder<I, O> implements StreamHandler<I, O>, StreamObserver<I, O> {
	
	public val IStream<I, O> stream
	
	@Atomic Procedure2<I, O> valueFn
	@Atomic Procedure2<I, Throwable> errorFn
	@Atomic Procedure2<I, Integer> finishFn
	@Atomic Procedure1<Void> closedFn

	new(IStream<I, O> stream) {
		this.stream = stream
	}

	// BUILDER METHODS ////////////////////////////////////////////////////////

	/** listen for each incoming value */
	override each((I, O)=>void handler) {
		this.valueFn = handler
	}
	
	/** listen for any finish */
	override finish((I, Integer)=>void handler) {
		this.finishFn = handler
	}

	/** listen for any uncaught errors */
	override error((I, Throwable)=>void handler) {
		this.errorFn = handler
	}
	
	/** listen for when the stream closes */
	override closed((Void)=>void handler) {
		this.closedFn = handler
	}
	
	// STREAMOBSERVER IMPLEMENTATION //////////////////////////////////////////
	
	override onValue(I from, O value) {
		valueFn?.apply(from, value)
	}
	
	override onError(I from, Throwable t) {
		errorFn?.apply(from, t)
	}
	
	override onFinish(I from, int level) {
		finishFn?.apply(from, level)
	}
	
	override onClosed() {
		closedFn?.apply(null)
	}
	
}
