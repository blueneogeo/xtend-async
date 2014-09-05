package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class StreamMonitor implements Procedure1<StreamCommand> {
	
	val Stream<?> stream
	
	@Atomic Procedure1<Void> onNextFn
	@Atomic Procedure1<Void> onSkipFn
	@Atomic Procedure1<Void> onCloseFn
	
	new(Stream<?> stream) {
		this.stream = stream
		stream.onNotification[ apply ]
	}

	override apply(StreamCommand it) {
		switch it {
			Next: onNextFn?.apply(null)
			Skip: onSkipFn?.apply(null)
			Close: onCloseFn?.apply(null)
		}
	}

	def onNext((Void)=>void onNextFn) {
		this.onNextFn = onNextFn
	}
	
	def onSkip((Void)=>void onSkipFn) {
		this.onSkipFn = onSkipFn
	}

	def onClose((Void)=>void onCloseFn) {
		this.onCloseFn = onCloseFn
	}
	
}
