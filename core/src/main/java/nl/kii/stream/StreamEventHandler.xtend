package nl.kii.stream

interface StreamEventHandler<I> {
	
	def void onNext()
	
	def void onClose()

	def void onPause()

	def void onResume()
	
	def void onOverflow(I input)
	
}
