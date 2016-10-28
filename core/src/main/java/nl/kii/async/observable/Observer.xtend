package nl.kii.async.observable

import co.paralleluniverse.fibers.Suspendable

/** 
 * An observer observes a process. 
 * It can take in values, errors and when a process has completed.
 */
@Suspendable
interface Observer<IN, OUT> {
	/** Tell the observer there is a new value, and what the input was that generated that value. */
	def void value(IN in, OUT value)
	/** Tell the observer an error occurred, and what the input was that caused that error. */
	def void error(IN in, Throwable t)
	/** Tell the observer that all data was sent, and no more is coming. */
	def void complete()
}
