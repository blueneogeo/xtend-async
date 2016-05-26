package nl.kii.async.stream

import nl.kii.async.observable.Observable

interface Stream<IN, OUT> extends Controllable, Observable<IN, OUT> {
	
	def boolean isOpen()
	
}
