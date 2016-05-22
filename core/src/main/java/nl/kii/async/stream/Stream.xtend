package nl.kii.async.stream

import nl.kii.async.Observable

interface Stream<IN, OUT> extends Controllable, Observable<IN, OUT> {
	
	def boolean isOpen()
	
}
