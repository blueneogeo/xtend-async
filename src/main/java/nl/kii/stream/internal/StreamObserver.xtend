package nl.kii.stream.internal

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

interface StreamObserver<I, O> {
	
//	/** can be called to set the stream that is observed */
//	def void setStream(IStream<I, O> stream)
	
	/** handle an incoming value */
	def void onValue(I from, O value)
	
	/** handle an incoming error */
	def void onError(I from, Throwable t)
	
	/** handle an imcoming finish of a given level */
	def void onFinish(I from, int level)
	
	/** handle the stream being closed */
	def void onClosed()
	
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
class StreamResponder<I, O> implements StreamObserver<I, O> {
	
	@Atomic public val nl.kii.stream.IStream<I, O> stream
	@Atomic Procedure2<I, O> valueFn
	@Atomic Procedure2<I, Throwable> errorFn
	@Atomic Procedure2<I, Integer> finishFn
	@Atomic Procedure1<Void> closedFn

	// BUILDER METHODS ////////////////////////////////////////////////////////

	/** listen for each incoming value */
	def each((I, O)=>void handler) {
		this.valueFn = handler
	}
	
	/** listen for any finish */
	def finish((I, Integer)=>void handler) {
		this.finishFn = handler
	}

	/** listen for any uncaught errors */
	def error((I, Throwable)=>void handler) {
		this.errorFn = handler
	}
	
	/** listen for when the stream closes */
	def closed((Void)=>void handler) {
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
