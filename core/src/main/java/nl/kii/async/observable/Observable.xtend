package nl.kii.async.observable

import co.paralleluniverse.fibers.Suspendable

/** An observable lets you observe it with an observer, for values, errors, and completion. */
interface Observable<IN, OUT> {

	@Suspendable
	def void next()

	def void setObserver(Observer<IN, OUT> observer)	
}
