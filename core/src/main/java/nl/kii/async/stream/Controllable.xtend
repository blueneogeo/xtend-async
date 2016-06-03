package nl.kii.async.stream

/** Making a something controllable indicates that you can control when values can be emitted. */
interface Controllable {

	/** Ask for the next value */
	def void next()

	/** Ask to pause value output */
	def void pause()

	/** Ask to resume value output */
	def void resume()

	/** Ask to close the output (cannot be resumed) */
	def void close()

}
