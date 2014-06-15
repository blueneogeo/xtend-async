package nl.kii.promise

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import nl.kii.stream.Entry
import nl.kii.stream.Value
import nl.kii.stream.Error
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
class Promise<T> implements Procedure1<Entry<T>> {
	
	val _entry = new AtomicReference<Entry<T>>
	var _fulfilled = new AtomicBoolean(false)

	/** Lets others listen for the arrival of a value */
	val _onValue = new AtomicReference<Procedure1<T>>

	/** Lets others listen for errors occurring in the onValue listener */
	val _onError = new AtomicReference<Procedure1<Throwable>>
	
	new() { }
	
	new(T value) { set(value) }
	
	new(Promise<?> parentPromise) {
		parentPromise.onError [ error(it) ]
	}
	
	// GETTERS AND SETTERS ////////////////////////////////////////////////////
	
	def isFulfilled() {
		_fulfilled.get
	}
	
	/** only has a value when finished, otherwise null */
	def get() {
		_entry.get
	}
	
	// PUSH ///////////////////////////////////////////////////////////////////

	/** set the promised value */
	def set(T value) {
		if(value == null) throw new NullPointerException('cannot promise a null value')
		apply(new Value(value))
		this
	}

	/** report an error to the listener of the promise. */
	def error(Throwable t) {
		apply(new Error<T>(t))
		this
	}
		
	override apply(Entry<T> it) {
		if(it == null) throw new NullPointerException('cannot promise a null entry')
		if(fulfilled) throw new PromiseException('cannot apply an entry to a completed promise. entry was: ' + it)
		_fulfilled.set(true)
		if(_onValue.get != null) publish(it) else _entry.set(it)
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	def then(Procedure1<T> onValue) {
		if(_onValue.get != null) throw new PromiseException('cannot listen to a promise more than once')
		_onValue.set(onValue)
		if(fulfilled) publish(_entry.get)
	}
	
	def onError(Procedure1<Throwable> onError) {
		_onError.set(onError)
		this
	}
	
	// OTHER //////////////////////////////////////////////////////////////////
	
	protected def buffer(Entry<T> value) {
		if(_entry.get == null) _entry.set(value)
	}
	
	/** 
	 * Send an entry directly (no queue) to the listeners
	 * (onValue, onError, onFinish). If a value was processed,
	 * ready is set to false again, since the value was published.
	 */
	protected def publish(Entry<T> it) {
		switch it {
			Value<T>: {
				// duplicate code on purpose, so that without an error, it is thrown without a wrapper
				if(_onError.get != null) {
					try {
						_onValue.get.apply(value)
					} catch(Throwable t) {
						_onError.get.apply(t)
					} 
				} else {
					_onValue.get.apply(value)
				} 
				
			}	
			Error<T>: if(_onError.get != null) _onError.get.apply(error)
			// we do not process Finish<T>
		}
	}
	
}

class Task extends Promise<Boolean> {
	
	new() { }
	
	new(Promise<?> parentPromise) {
		parentPromise.onError [ error(it) ]
	}
	
	def complete() {
		set(true)
		this
	}

}

class PromiseException extends Exception {
	
	new(String msg) { super(msg) }
	new(String msg, Exception e) { super(msg, e) }
	
}
