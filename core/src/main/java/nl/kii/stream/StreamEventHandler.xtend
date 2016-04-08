package nl.kii.stream

interface StreamEventHandler {
	
	def void onNext()
	
	def void onClose()

	def void onPause()

	def void onResume()
	
	def void onOverflow()
	
}
