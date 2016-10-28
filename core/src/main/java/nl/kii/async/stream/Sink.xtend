package nl.kii.async.stream

import co.paralleluniverse.fibers.Suspendable

/** 
 * A sink is a stream source.
 * You use push to push values and errors into the sink.
 * <p>
 * Sinks can be optionally flow controlled. You create flow
 * control by only pushing in new values when onNext() is
 * called.
 */
@Suspendable
abstract class Sink<IN> extends Source<IN, IN> {

	/** What to do when the stream is asking for a next value */
	abstract override void onNext()

	/** What to do when the stream is being closed */	
	abstract override void onClose()

	/** Send a value into the stream */
	def push(IN value) {
		value(value, value)
	}
	
	/** Send an error into the stream */
	def push(Throwable t) {
		error(null, t)
	}
	
}
