package nl.kii.stream

interface StreamEntryHandler<I, O> {
		
	/** handle an incoming value */
	def void onValue(I from, O value)
	
	/** handle an incoming error */
	def void onError(I from, Throwable t)
	
	/** handle the stream being closed */
	def void onClosed()
	
}
