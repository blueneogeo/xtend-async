package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

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
class StreamEntryResponder<I, O> implements StreamEntryHandler<I, O> {
	
	@Atomic public val IStream<I, O> stream
	@Atomic Procedure2<I, O> valueFn
	@Atomic Procedure2<I, Throwable> errorFn
	@Atomic Procedure1<Void> finishFn

	// BUILDER METHODS ////////////////////////////////////////////////////////

	/** listen for each incoming value */
	def each((I, O)=>void handler) {
		this.valueFn = handler
	}
	
	/** listen for any uncaught errors */
	def error((I, Throwable)=>void handler) {
		this.errorFn = handler
	}
	
	/** listen for when the stream closes */
	def closed((Void)=>void handler) {
		this.finishFn = handler
	}
	
	// STREAMOBSERVER IMPLEMENTATION //////////////////////////////////////////
	
	override onValue(I from, O value) {
		valueFn?.apply(from, value)
	}
	
	override onError(I from, Throwable t) {
		errorFn?.apply(from, t)
	}
	
	override onClosed() {
		finishFn?.apply(null)
	}
	
}
