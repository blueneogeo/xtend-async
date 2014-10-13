package nl.kii.promise

import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Publisher
import nl.kii.stream.Entry
import nl.kii.stream.Error
import nl.kii.stream.Value
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2
import nl.kii.async.AsyncException

interface IPromise<R, T> extends Procedure1<Entry<R, T>> {
	
	def IPromise<R, ?> getRoot()
	
	def Boolean getFulfilled()
	def Entry<R, T> get()
	def void set(R value)
	def IPromise<R, T> error(Throwable t)
	
	def IPromise<R, T> onError(Procedure1<Throwable> errorFn)
	def IPromise<R, T> onError(Procedure2<R, Throwable> errorFn)
	def Task then(Procedure1<T> valueFn)
	def Task then(Procedure2<R, T> valueFn)
	
	def void setOperation(String operation)
	def String getOperation()
	
}

class Promise<T> extends BasePromise<T, T> {
	
	override getRoot() { this }
	
	new() { }
	
	new(T t) { set(t) }
	
	/** set the promised value */
	override set(T value) {
		if(value == null) throw new NullPointerException('cannot promise a null value')
		apply(new Value(value, value))
	}

	/** report an error to the listener of the promise. */
	override error(Throwable t) {
		apply(new Error(null, t))
		this
	}
	
}

class SubPromise<R, T> extends BasePromise<R, T> {
	
	val protected IPromise<R, ?> root

	/** Constructor for easily creating a child promise */
	new(IPromise<R, ?> parentPromise) {
		this.root = parentPromise.root
		parentPromise.onError [ error(it) ]
	}

	override getRoot() { root }

	/** set the promised value */
	override set(R value) { root.set(value) }

	/** report an error to the listener of the promise. */
	override error(Throwable t) { root.error(t) this }
	
	/** set the promised value */
	package def void set(R from, T value) { apply(new Value(from, value)) }

	package def error(R from, Throwable t) { apply(new Error(from, t)) }

}

/**
 * A Promise is a publisher of a value. The value may arrive later.
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

	override onError(Procedure1<Throwable> errorFn) {
		this.onError [ r, t | errorFn.apply(t) ]
	}	
	/** 
	 * If the promise recieved or recieves an error, onError is called with the throwable.
	 */
	override onError(Procedure2<R, Throwable> errorFn) {
		// register for a new value being applied
		val sub = new AtomicReference<Procedure0>
		sub.set(publisher.onChange [
			switch it { 
				Error<R, T>: { 
					sub.get.apply // unsubscribe, so this handler will not be called again
					errorFn.apply(from, error)
				} 
			}
		])
		hasErrorHandler = true
		// if there is an entry, push it so this handler will get it
		if(entry != null) publisher.apply(entry)
		this
	}

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure1<T> valueFn) {
		this.then [ r, it | valueFn.apply(it) ]
	}

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure2<R, T> valueFn) {
		val newTask = new Task
		// register for a new value being applied
		val sub = new AtomicReference<Procedure0>
		sub.set(publisher.onChange [
			try {
				switch it { 
					Value<R, T>: { 
						sub.get.apply // unsubscribe, so this handler will not be called again
						valueFn.apply(from, value)
						newTask.complete
					}
					Error<R, T>: newTask.error(error)
				}
			} catch(Exception e) {
				error(new AsyncException('Promise.then gave error for', it, e))
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

/** A Task is a promise that some task gets done. It has no result, it can just be completed or have an error. */
class Task extends Promise<Boolean> {
	
	def complete() {
		set(true)
		this
	}

	override toString() '''Task { fulfilled: «fulfilled» «IF get instanceof Error<?, ?>», error: «(get as Error<?, ?>).error»«ENDIF» }'''
	
}

/** A Task is a promise that some task gets done. It has no result, it can just be completed or have an error. */
class SubTask<R> extends SubPromise<R, Boolean> {
	
	new(IPromise<R, ?> parentPromise) {
		super(parentPromise)
	}
	
	def complete(R from) {
		apply(new Value(from, true))
	}
	
	override toString() '''Task { fulfilled: «fulfilled» «IF get instanceof Error<?, ?>», error: «(get as Error<?, ?>).error»«ENDIF» }'''

}
