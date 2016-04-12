package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class StreamEventResponder<I> implements StreamEventHandler<I> {
	
	@Atomic Procedure1<Void> nextFn
	@Atomic Procedure1<Void> skipFn
	@Atomic Procedure1<Void> closeFn
	@Atomic Procedure1<Void> pauseFn
	@Atomic Procedure1<Void> resumeFn
	@Atomic Procedure1<I> overflowFn
	
	// BUILDER METHODS /////////////////////////////////////////////////////////

	def next((Void)=>void handler) {
		nextFn = handler
	}
	
	def close((Void)=>void handler) {
		closeFn = handler
	}

	def overflow((I)=>void handler) {
		overflowFn = handler
	}
	
	def pause((Void)=>void handler) {
		pauseFn = handler
	}

	def resume((Void)=>void handler) {
		resumeFn = handler
	}
	
	// STREAMLISTENER IMPLEMENTATION ///////////////////////////////////////////
	
	override onNext() {
		nextFn?.apply(null)
	}
	
	override onClose() {
		closeFn?.apply(null)
	}
	
	override onOverflow(I from) {
		overflowFn?.apply(from)
	}
	
	override onPause() {
		pauseFn.apply(null)
	}

	override onResume() {
		resumeFn.apply(null)
	}
	
}
