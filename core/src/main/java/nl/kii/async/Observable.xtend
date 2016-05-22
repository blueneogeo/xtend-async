package nl.kii.async

/** An observable lets you observe it with an observer, for values, errors, and completion. */
interface Observable<IN, OUT> {
	def void next()
	def void setObserver(Observer<IN, OUT> observer)	
}
