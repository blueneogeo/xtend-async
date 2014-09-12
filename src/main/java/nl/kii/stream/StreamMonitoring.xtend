package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface StreamMonitor {
	
	def void onNext()
	
	def void onSkip()
	
	def void onClose()
	
}

interface StreamResponder {
	
	def void next((Void)=>void handler)
	
	def void skip((Void)=>void handler)

	def void close((Void)=>void handler)

}

class StreamResponderBuilder implements StreamMonitor, StreamResponder {
	
	@Atomic Procedure1<Void> nextFn
	@Atomic Procedure1<Void> skipFn
	@Atomic Procedure1<Void> closeFn
	
	// STREAMRESPONDER IMPLEMENTATION /////////////////////////////////////////

	override next((Void)=>void handler) {
		nextFn = handler
	}
	
	override skip((Void)=>void handler) {
		skipFn = handler
	}

	override close((Void)=>void handler) {
		closeFn = handler
	}
	
	// STREAMMONITOR IMPLEMENTATION ///////////////////////////////////////////
	
	override onNext() {
		nextFn?.apply(null)
	}
	
	override onSkip() {
		skipFn?.apply(null)
	}
	
	override onClose() {
		closeFn?.apply(null)
	}
	
}
