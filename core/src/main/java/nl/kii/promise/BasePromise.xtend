package nl.kii.promise

import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Publisher
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static extension nl.kii.util.OptExtensions.*
import nl.kii.async.options.AsyncOptions

/**
 * Base implementation of IPromise. 
 */
abstract class BasePromise<I, O> implements IPromise<I, O> {
	
	@Accessors(PUBLIC_GETTER) val AsyncOptions options
	val Publisher<Entry<I, O>> publisher
	
	new(AsyncOptions options) {
		this.options = options.copy
		this.publisher = new Publisher(options.newPromiseActorQueue, true, options.actorMaxCallDepth)
	}
	
	/** Property to see if the promise has an error handler assigned */
	@Atomic public val boolean hasErrorHandler = false

	/** Property to see if the promise has a value handler assigned */
	@Atomic public val boolean hasValueHandler = false

	/** The result of the promise, if any, otherwise null */
	@Atomic protected val Entry<I, O> entry
	
	// GETTERS AND SETTERS ////////////////////////////////////////////////////
	
	override apply(Entry<I, O> it) {
		if(it == null) throw new NullPointerException('cannot promise a null entry')
		val allowed = switch it { 
			case !fulfilled: true
			Error<?, O> case fulfilled: true
			default: false
		}
		if(!allowed) return;
		entry = it
		publisher.apply(it)
	}
	
	/** has Some(value) when fulfilled, otherwise None */
	override get() { entry.option }
	
	override getFulfilled() { entry != null	}
	
	package def getPublisher() { publisher }
	
	// LISTENING //////////////////////////////////////////////////////////////
	
	override onChange((Entry<I, O>)=>void observeFn) {
		val unregisterFn = new AtomicReference<Procedure0>
		unregisterFn.set(publisher.onChange [
			if(unregisterFn.get != null) {
				unregisterFn.get.apply // unregister automatically after the entry was applied
				observeFn.apply(it) // call the observeFn with the entry
			}
		])
		// if there is an entry, push it so this handler will get it
		if(entry != null) publisher.apply(entry)
		unregisterFn.get
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure1<O> valueFn) {
		this.then [ r, it | valueFn.apply(it) ]
	}

	/** Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening. */
	override then(Procedure2<I, O> valueFn) {
		val newPromise = new SubPromise(this)
		onChange [
			switch it { 
				Value<I, O>: { 
					try {
						valueFn.apply(from, value)
						newPromise.set(from, value)
					} catch(Throwable e) {
						newPromise.error(from, e)
					}
				}
				Error<I, O>: newPromise.error(from, error)
			}
		]
		hasValueHandler = true
		newPromise
	}

	/** 
	 * If the promise recieved or recieves an error of the passed errorType, or a subclass of the errorType,
	 * errorFn is called with the error. Returns a promise that simply passes through the incoming value or error.
	 */
	override <T extends Throwable> on(Class<T> errorType, (T)=>void errorFn) {
		on(errorType, false) [ from, e | errorFn.apply(e) ]
	}

	/** 
	 * If the promise recieved or recieves an error of the passed errorType, or a subclass of the errorType,
	 * errorFn is called with the error. If swallow is true, the error will be swallowed by this handler and
	 * not passed on. WARNING: normally it is dangerous to swallow an error, since there will be no result
	 * from the promise anymore.
	 */
	override <T extends Throwable> on(Class<T> errorType, boolean swallow, (I, T)=>void errorFn) {
		val subPromise = new SubPromise(this)
		onChange [
			switch it {
				Value<I, O>: subPromise.set(from, value)
				Error<I, O>: {
					try {
						if(errorType.isAssignableFrom(error.class)) {
							errorFn.apply(from, error as T)
							if(!swallow) subPromise.error(from, error)
						} else subPromise.error(from, error)
					} catch(Exception e) {
						 subPromise.error(from, e)
					}
				} 
			}
		]
		hasErrorHandler = true
		subPromise
	}

	override toString() '''Promise { fulfilled: «fulfilled», entry: «get», options: «options» }'''
	
}
