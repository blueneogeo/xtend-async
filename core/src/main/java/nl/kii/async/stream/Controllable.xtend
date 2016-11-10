package nl.kii.async.stream

import co.paralleluniverse.fibers.Suspendable

/** Making a something controllable indicates that you can control when values can be emitted. */
interface Controllable {

	/** Ask for the next value */
	@Suspendable
	def void next()

	/** Ask to pause value output */
	@Suspendable
	def void pause()

	/** Ask to resume value output */
	@Suspendable
	def void resume()

	/** Ask to close the output (cannot be resumed) */
	@Suspendable
	def void close()

}
