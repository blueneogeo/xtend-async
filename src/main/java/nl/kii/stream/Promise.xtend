package nl.kii.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
class Promise<T> implements Procedure1<T> {
	
	val _onValue = new AtomicReference<Procedure1<T>>
	val _value = new AtomicReference<T>
	var _finished = new AtomicBoolean(false)
	
	new() { }
	
	new(T value) { apply(value) }
	
	// GETTERS AND SETTERS ////////////////////////////////////////////////////
	
	def isStarted() {
		_onValue.get != null
	}
	
	def isFinished() {
		_finished.get
	}
	
	/** only has a value when finished, otherwise null */
	def get() {
		_value.get
	}
	
	// PUSH ///////////////////////////////////////////////////////////////////
	
	override apply(T value) {
		if(finished) throw new PromiseException('cannot apply value to a finished promise. value was: ' + value)
		if(value == null) throw new NullPointerException('cannot promise a null value')
		_finished.set(true)
		if(started) 
			_onValue.get.apply(value) 
		else 
			_value.set(value)
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	def void then(Procedure1<T> listener) {
		if(started) throw new PromiseException('cannot listen to a promise more than once')
		val value = _value.get
		if(value != null) listener.apply(value) else _onValue.set(listener)
	}
	
}

class PromiseException extends Exception {
	
	new(String msg) { super(msg) }
	new(String msg, Exception e) { super(msg, e) }
	
}
