package nl.kii.stream

import nl.kii.async.annotation.Atomic
import nl.kii.stream.message.Entry
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface StreamEventHandler<I, O> {
	
	def void onNext()
	
	def void onSkip()
	
	def void onClose()

	def void onPause()

	def void onResume()
	
	def void onOverflow(Entry<I, O> entry)
	
}

class StreamEventResponder<I, O> implements StreamEventHandler<I, O> {
	
	@Atomic Procedure1<Void> nextFn
	@Atomic Procedure1<Void> skipFn
	@Atomic Procedure1<Void> closeFn
	@Atomic Procedure1<Void> pauseFn
	@Atomic Procedure1<Void> resumeFn
	@Atomic Procedure1<Entry<I, O>> overflowFn
	
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

	def overflow((Entry<I, O>)=>void handler) {
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
	
	override onSkip() {
		skipFn?.apply(null)
	}
	
	override onClose() {
		closeFn?.apply(null)
	}
	
	override onOverflow(Entry<I, O> entry) {
		overflowFn?.apply(entry)
	}
	
	override onPause() {
		pauseFn.apply(null)
	}

	override onResume() {
		resumeFn.apply(null)
	}
	
}
