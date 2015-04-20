package nl.kii.stream.internal

interface StreamObserver<I, O> {
	
//	/** can be called to set the stream that is observed */
//	def void setStream(IStream<I, O> stream)
	
	/** handle an incoming value */
	def void onValue(I from, O value)
	
	/** handle an incoming error */
	def void onError(I from, Throwable t)
	
	/** handle an imcoming finish of a given level */
	def void onFinish(I from, int level)
	
	/** handle the stream being closed */
	def void onClosed()
	
}
