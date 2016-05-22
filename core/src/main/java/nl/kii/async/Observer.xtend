package nl.kii.async

/** 
 * An observer observes a process. 
 * It can take in values, errors and when a process has completed.
 */
interface Observer<IN, OUT> {
	def void value(IN in, OUT value)
	def void error(IN in, Throwable t)
	def void complete()
}
