package nl.kii.stream

/**
 * Lets you register listeners that respond to an incoming event of type T,
 * and lets you unsubscribe by calling the returned procedure.
 * @param T : the type of change that this observable can report 
 */
interface Observable<T> {
	
	/**
	 * Listen for changes in this observable. Every change triggers a call of the
	 * passed observer function.
	 * @param observer : function to be called when a change occurs
	 * @return a function you can call to end the subscription.
	 */
	def =>void onChange(Procedures.Procedure1<T> observer)
	
}
