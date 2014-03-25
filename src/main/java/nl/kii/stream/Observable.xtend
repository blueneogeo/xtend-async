package nl.kii.stream

/**
 * Lets you register listeners that respond to an incoming event of type T
 */
interface Observable<T> {
	
	def void onChange(Procedures.Procedure1<T> observer)
	
}
