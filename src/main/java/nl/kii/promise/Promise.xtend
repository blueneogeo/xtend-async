package nl.kii.promise

import nl.kii.async.annotation.Atomic
import nl.kii.stream.Entry
import nl.kii.stream.Error
import nl.kii.stream.Value
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface IPromise<T> extends Procedure1<Entry<T>> {
	
	def Boolean getFulfilled()
	def Entry<T> get()
	def Promise<T> set(T value)
	def Promise<T> error(Throwable t)
	
	def Promise<T> onError(Procedure1<Throwable> errorFn)
	def Promise<T> always(Procedure1<Entry<T>> resultFn)
	def void then(Procedure1<T> valueFn)
	
}

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
class Promise<T> implements IPromise<T> {
	
	/** Property to see if the promise is fulfulled */
	@Atomic public val boolean fulfilled = false

	/** The result of the promise, if any, otherwise null */
	@Atomic protected val Entry<T> entry

	/** Lets others listen for the arrival of a value */
	@Atomic protected val Procedure1<T> valueFn
	
	/** Always called, both when there is a value and when there is an error */
	@Atomic protected val Procedure1<Entry<T>> resultFn

	/** Lets others listen for errors occurring in the onValue listener */
	@Atomic protected val Procedure1<Throwable> errorFn
	
	/** Create a new unfulfilled promise */
	new() { }
	
	/** Create a fulfilled promise */
	new(T value) { set(value) }
	
	/** Constructor for easily creating a child promise */
	new(IPromise<?> parentPromise) {
		parentPromise.onError [ error(it) ]
	}
	
	// GETTERS AND SETTERS ////////////////////////////////////////////////////
	
	/** only has a value when finished, otherwise null */
	override get() {
		entry
	}
	
	// PUSH ///////////////////////////////////////////////////////////////////

	/** set the promised value */
	override set(T value) {
		if(value == null) throw new NullPointerException('cannot promise a null value')
		apply(new Value(value))
		this
	}

	/** report an error to the listener of the promise. */
	override error(Throwable t) {
		apply(new Error<T>(t))
		this
	}
	
	override apply(Entry<T> it) {
		if(fulfilled) return;
		if(it == null) throw new NullPointerException('cannot promise a null entry')
		fulfilled = true
		if(valueFn != null) publish(it) else entry = it
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/** If the promise recieved or recieves an error, onError is called with the throwable */
	override Promise<T> onError(Procedure1<Throwable> errorFn) {
		this.errorFn = errorFn
		this
	}
	
	/** Always call onResult, whether the promise has been either fulfilled or had an error. */
	override always(Procedure1<Entry<T>> resultFn) {
		if(this.resultFn != null) throw new PromiseException('cannot listen to promise.always more than once')
		this.resultFn = resultFn
		this
	}
	
	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override void then(Procedure1<T> valueFn) {
		if(this.valueFn != null) throw new PromiseException('cannot listen to promise.then more than once')
		this.valueFn = valueFn
		if(fulfilled) publish(entry)
	}
	
	// OTHER //////////////////////////////////////////////////////////////////
	
	protected def buffer(Entry<T> value) {
		if(entry == null) entry = value
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
				if(errorFn != null) {
					try {
						valueFn.apply(value)
					} catch(Throwable t) {
						errorFn.apply(t)
					} 
				} else {
					valueFn.apply(value)
				} 
				
			}	
			Error<T>: 
				if(errorFn != null) errorFn.apply(error) 
				else error.printStackTrace
			// we do not process Finish<T>
		}
		if(resultFn != null) {
			try {
				resultFn.apply(it)
			} catch(Throwable t) {
				errorFn.apply(t)
			}
		}
	}
	
	override toString() '''Promise { fulfilled: «fulfilled», entry: «get» }'''
	
}

/** A Task is a promise that some task gets done. It has no result, it can just be completed or have an error. */
class Task extends Promise<Boolean> {
	
	new() { }
	
	new(IPromise<?> parentPromise) {
		parentPromise.onError [ error(it) ]
	}
	
	def complete() {
		set(true)
		this
	}
	
	override toString() '''Task { fulfilled: «fulfilled» }'''

}

/** Thrown when some error occurred during a promise */
class PromiseException extends Exception {
	
	new(String msg) { super(msg) }
	new(String msg, Exception e) { super(msg, e) }
	
}
