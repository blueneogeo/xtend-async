package nl.kii.promise

import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Publisher
import nl.kii.stream.Entry
import nl.kii.stream.Error
import nl.kii.stream.Value
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface IPromise<T> extends Procedure1<Entry<T>> {
	
	def Boolean getFulfilled()
	def Entry<T> get()
	def Promise<T> set(T value)
	def Promise<T> error(Throwable t)
	
	def Promise<T> onError(Procedure1<Throwable> errorFn)
	def Promise<T> then(Procedure1<T> valueFn)
	
}

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
class Promise<T> implements IPromise<T> {
	
	val publisher = new Publisher<Entry<T>>
	
	
	/** Property to see if the promise is fulfulled */
	@Atomic public val boolean fulfilled = false

	/** The result of the promise, if any, otherwise null */
	@Atomic protected val Entry<T> entry

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
		if(it == null) throw new NullPointerException('cannot promise a null entry')
		val allowed = switch it { 
			case !fulfilled: true
			Error<T> case fulfilled: true
			default: false
		}
		if(!allowed) return;
		fulfilled = true
		entry = it
		publisher.apply(it)
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/** If the promise recieved or recieves an error, onError is called with the throwable */
	override onError(Procedure1<Throwable> errorFn) {
		// register for a new value being applied
		val sub = new AtomicReference<Procedure0>
		sub.set(publisher.onChange [
			switch it { 
				Error<T>: { 
					sub.get.apply // unsubscribe, so this handler will not be called again
					errorFn.apply(error)
				} 
			}
		])
		// if there is an entry, push it so this handler will get it
		if(entry != null) publisher.apply(entry)
		this
	}

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure1<T> valueFn) {
		// register for a new value being applied
		val sub = new AtomicReference<Procedure0>
		sub.set(publisher.onChange [
			try {
				switch it { 
					Value<T>: { 
						sub.get.apply // unsubscribe, so this handler will not be called again
						valueFn.apply(value) 
					}
				}
			} catch(Exception e) {
				error(e)
			}
		])
		// if there is an entry, push it so this handler will get it
		if(entry != null) publisher.apply(entry)
		this
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
	
	override toString() '''Task { fulfilled: «fulfilled» «IF get instanceof Error<?>», error: «(get as Error<?>).error»«ENDIF» }'''

}

/** Thrown when some error occurred during a promise */
class PromiseException extends Exception {
	
	new(String msg) { super(msg) }
	new(String msg, Exception e) { super(msg, e) }
	
}
