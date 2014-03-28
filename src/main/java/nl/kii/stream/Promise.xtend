package nl.kii.stream

import nl.kii.stream.impl.ThreadSafePublisher
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
class Promise<T> implements Publisher<T> {
	
	var Publisher<T> onNext
	
	var T buffer

	var boolean isStarted = false
	var boolean isFinished = false
	
	new() {
		this.onNext = new ThreadSafePublisher<T>
	}
	
	new(Publisher<T> onNext, Publisher<Throwable> onError) {
		this.onNext = onNext
	}
	
	// PUSH ///////////////////////////////////////////////////////////////////
	
	override apply(T value) {
		if(isFinished) throw new PromiseException('cannot apply value to a finished promise. value was: ' + value)
		if(value == null) throw new NullPointerException('cannot promise a null value')
		if(isStarted) {
			onNext.apply(value)
			isFinished = true
		} else buffer = value
	}
	
	def isStarted() {
		isStarted
	}
	
	def isFinished() {
		isFinished
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	def void then(Procedure1<T> listener) {
		onNext.onChange(listener)
		if(!isStarted) {
			isStarted = true
			if(buffer != null)
				apply(buffer)
		}
	}
	
	override void onChange(Procedure1<T> listener) {
		then(listener)
	}
	
}

class PromiseException extends Exception {
	
	new(String msg) { super(msg) }
	new(String msg, Exception e) { super(msg, e) }
	
}
