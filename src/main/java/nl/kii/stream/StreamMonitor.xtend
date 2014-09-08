package nl.kii.stream

import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface StreamNotificationObserver extends Procedure1<StreamNotification> {
	
	def void next((Void)=>void handler)
	
	def void skip((Void)=>void handler)

	def void close((Void)=>void handler)

}

class StreamMonitor implements StreamNotificationObserver {
	
	val Stream<?> stream
	
	@Atomic Procedure1<Void> nextFn
	@Atomic Procedure1<Void> skipFn
	@Atomic Procedure1<Void> closeFn
	
	new(Stream<?> stream) {
		this.stream = stream
		stream.onNotification[ apply ]
	}

	override apply(StreamNotification it) {
		switch it {
			Next: nextFn?.apply(null)
			Skip: skipFn?.apply(null)
			Close: closeFn?.apply(null)
		}
	}

	override next((Void)=>void handler) {
		nextFn = handler
	}
	
	override skip((Void)=>void handler) {
		skipFn = handler
	}

	override close((Void)=>void handler) {
		closeFn = handler
	}
	
}
