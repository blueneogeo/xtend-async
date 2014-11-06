package nl.kii.stream.internal

import nl.kii.async.annotation.Atomic
import nl.kii.stream.message.Entry
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface StreamEventHandler {
	
	def void onNext()
	
	def void onSkip()
	
	def void onClose()
	
	def void onOverflow(Entry<?, ?> entry)
	
}

class StreamEventResponder implements StreamEventHandler {
	
	@Atomic Procedure1<Void> nextFn
	@Atomic Procedure1<Void> skipFn
	@Atomic Procedure1<Void> closeFn
	@Atomic Procedure1<Entry<?, ?>> overflowFn
	
	// BUILDER METHODS /////////////////////////////////////////////////////////

	def next((Void)=>void handler) {
		nextFn = handler
	}
	
	def skip((Void)=>void handler) {
		skipFn = handler
	}

	def close((Void)=>void handler) {
		closeFn = handler
	}

	def overflow((Entry<?, ?>)=>void handler) {
		overflowFn = handler
	}
	
	// STREAMLISTENER IMPLEMENTATION ///////////////////////////////////////////
	
	override onNext() {
		nextFn?.apply(null)
	}
	
	override onSkip() {
		skipFn?.apply(null)
	}
	
	override onClose() {
		closeFn?.apply(null)
	}
	
	override onOverflow(Entry<?, ?> entry) {
		overflowFn?.apply(entry)
	}
	
}
