package nl.kii.promise.internal

import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Publisher
import nl.kii.promise.IPromise
import nl.kii.promise.Task
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

/**
 * Base implementation of IPromise. 
 */
abstract class BasePromise<R, T> implements IPromise<R, T> {
	
	val publisher = new Publisher<Entry<R, T>>
	
	/** Property to see if the promise is fulfulled */
	@Atomic public val boolean fulfilled = false

	/** Property to see if the promise has an error handler assigned */
	@Atomic public val boolean hasErrorHandler = false

	/** Property to see if the promise has a value handler assigned */
	@Atomic public val boolean hasValueHandler = false

	/** The result of the promise, if any, otherwise null */
	@Atomic protected val Entry<R, T> entry

	/** name of the operation the listener is performing */
	@Atomic val String _operation 
	
	// GETTERS AND SETTERS ////////////////////////////////////////////////////
	
	override apply(Entry<R, T> it) {
		if(it == null) throw new NullPointerException('cannot promise a null entry')
		val allowed = switch it { 
			case !fulfilled: true
			Error<?, T> case fulfilled: true
			default: false
		}
		if(!allowed) return;
		fulfilled = true
		entry = it
		publisher.apply(it)
	}
	
	/** only has a value when finished, otherwise null */
	override get() { entry }
	
	def getPublisher() {
		publisher
	}
	
	override getOperation() {
		_operation
	}
	
	override setOperation(String name) {
		_operation = name
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////

	override on(Class<? extends Throwable> errorType, Procedure1<Throwable> errorFn) {
		this.on(errorType) [ r, t | errorFn.apply(t) ]
	}	
	/** 
	 * If the promise recieved or recieves an error, onError is called with the throwable.
	 * Removes the error from the chain, so the returned promise no longer receives the error.
	 * 
	 * FIX: this method should return a subpromise with the error filtered out, but it returns this,
	 * since there is a generics problem trying to assign the values.
	 */
	override on(Class<? extends Throwable> errorType, Procedure2<R, Throwable> errorFn) {
		// create a subpromise to return that should pass a value but not the matching exceptions		
		val subPromise = new SubPromise(this, false)
		
		// register for a new value being applied
		val unregisterFn = new AtomicReference<Procedure0>
		unregisterFn.set(publisher.onChange [
			switch it {
				Error<R, T>: { 
					try {
						unregisterFn.get.apply // unsubscribe, so this handler will not be called again
						// if an incoming error matches the passed errorType
						if(errorType.isAssignableFrom(error.class)) {
							// call the handler with the error and the causing value
							errorFn.apply(from, error)
						} else {
							// otherwise, pass the error along to the subpromise
							// FIX: next line gives Xtend error! Language problem (does work in Java)
							// subPromise.error(from, error)
						}
					} catch(Exception e) {
						// if the handler has an error, pass it along to the subpromise so the user can handle it
						// FIX: next line gives Xtend error! Language problem (does work in Java)
						// subPromise.error(from, e)
					}
				} 
			}
		])
		hasErrorHandler = true
		// if there is an entry, push it so this handler will get it
		if(entry != null) publisher.apply(entry)
		// listen for a value from this promise and pass it to the subpromise
		// FIX: next line gives Xtend error! Language problem (does work in Java)
		// this.then [ from, value | subPromise.apply(new Value(f, value)) ]
		subPromise
	}

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure1<T> valueFn) {
		this.then [ r, it | valueFn.apply(it) ]
	}

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure2<R, T> valueFn) {
		val newTask = new Task
		// register for a new value being applied
		val unregisterFn = new AtomicReference<Procedure0>
		unregisterFn.set(publisher.onChange [
			try {
				switch it { 
					Value<R, T>: { 
						unregisterFn.get.apply // unsubscribe, so this handler will not be called again
						valueFn.apply(from, value)
						newTask.complete
					}
					Error<R, T>: newTask.error(error)
				}
			} catch(Exception e) {
				error(new PromiseException('Promise.then gave error for', it, e))
				newTask.error(e)
			}
		])
		hasValueHandler = true
		// if there is an entry, push it so this handler will get it
		if(entry != null) publisher.apply(entry)
		newTask
	}

	override toString() '''Promise { fulfilled: «fulfilled», entry: «get» }'''
	
}
