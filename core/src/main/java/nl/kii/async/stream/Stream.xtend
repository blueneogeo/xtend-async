package nl.kii.async.stream

import nl.kii.async.observable.Observable
import co.paralleluniverse.fibers.Suspendable

@Suspendable
interface Stream<IN, OUT> extends Controllable, Observable<IN, OUT> {
	
	def boolean isOpen()
	
}
