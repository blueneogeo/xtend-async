package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Functions.Function1

interface StreamObserver<T> {
	
	/** handle an incoming value */
	def void onValue(T value)
	
	/**
	 * handle an incoming error
	 * @return if the error should be escalated/thrown
	 */
	def boolean onError(Throwable t)
	
	/** handle an imcoming finish of a given level */
	def void onFinish(int level)
	
	/** handle the stream being closed */
	def void onClosed()
	
}

/** Lets you create builders for handling the entries coming from a stream */
interface StreamHandler<T> {
	
	/** handle each incoming value. remember to call stream.next after handling a value! */
	def void each((T)=>void handler)
	
	/** handle each incoming error. remember to call stream.next after handling an error! */
	def void error((Throwable)=>boolean handler)
	
	/** handle each incoming finish. remember to call stream.next after handling a finish! */
	def void finish((Integer)=>void handler)
	
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
class StreamHandlerBuilder<T> implements StreamHandler<T>, StreamObserver<T> {
	
	public val Stream<T> stream
	
	@Atomic Procedure1<T> valueFn
	@Atomic Function1<Throwable, Boolean> errorFn
	@Atomic Procedure0 finish0Fn
	@Atomic Procedure1<Integer> finishFn
	@Atomic Procedure1<Void> closedFn

	new(Stream<T> stream) {
		this.stream = stream
	}

	// BUILDER METHODS ////////////////////////////////////////////////////////

	/** listen for each incoming value */
	override each((T)=>void handler) {
		this.valueFn = handler
	}
	
	/** listen for a finish of level 0 */
	def finish(=>void handler) {
		this.finish0Fn = handler
	}
	
	/** listen for any finish */
	override finish((Integer)=>void handler) {
		this.finishFn = handler
	}

	/** listen for any uncaught errors */
	override error((Throwable)=>boolean handler) {
		this.errorFn = handler
	}
	
	/** listen for when the stream closes */
	override closed((Void)=>void handler) {
		this.closedFn = handler
	}
	
	// STREAMOBSERVER IMPLEMENTATION //////////////////////////////////////////
	
	override onValue(T value) {
		valueFn?.apply(value)
	}
	
	override onError(Throwable t) {
		errorFn?.apply(t)
	}
	
	override onFinish(int level) {
		finishFn?.apply(level)
		if(level == 0) finish0Fn?.apply
	}
	
	override onClosed() {
		closedFn?.apply(null)
	}
	
}
