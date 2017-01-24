package nl.kii.async.observable

import co.paralleluniverse.fibers.Suspendable
import nl.kii.async.annotation.Suspending

/** An observable lets you observe it with an observer, for values, errors, and completion. */
interface Observable<IN, OUT> {

	@Suspendable
	def void next()

	def void setObserver(@Suspending Observer<IN, OUT> observer)	
}
