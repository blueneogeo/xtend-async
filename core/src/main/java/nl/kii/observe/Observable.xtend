package nl.kii.observe

interface Observable<T> {
	
	/** 
	 * Observe changes on the observable.
	 * @return a function that can be called to stop observing
	 */
	def =>void onChange((T)=>void observeFn)
	
}
