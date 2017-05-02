package nl.kii.async.promise

import nl.kii.async.observable.Observer
import co.paralleluniverse.fibers.Suspendable
import nl.kii.async.annotation.Suspending

/** 
 * A deferred promises a result, based on an incoming request.
 */
class Deferred<IN, OUT> implements Observer<IN, OUT>, Promise<IN, OUT> {

	protected boolean fulfilled = false
	protected boolean rejected = false

	protected Observer<IN, OUT> observer

	protected Pair<IN, OUT> cachedValue
	protected Pair<IN, Throwable> cachedError

	@Suspendable
	override value(IN in, OUT value) {
		if(!pending) return;
		fulfilled = true
		if(observer !== null) {
			observer.value(in, value)
			onCompleted
		} else {
			cachedValue = in -> value
		}
	}
	
	@Suspendable
	override error(IN in, Throwable t) {
		if(!pending) return;
		rejected = true
		if(observer !== null) {
			observer.error(in, t)
			onCompleted
		} else {
			cachedError = in -> t
		}
	}
	
	@Suspendable
	override complete() {
		// do nothing, because only value and error can complete a promise!
	}
	
	/** Called internally, so the observer.complete is called, and we can clean up a bit */
	@Suspendable
	protected def onCompleted() {
		observer?.complete
		// clean up after we are done
		observer = null
		cachedValue = null
		cachedError = null
	}

	override isPending() {
		!fulfilled && !rejected
	}
	
	override isFulfilled() {
		this.fulfilled
	}
	
	override isRejected() {
		this.rejected
	}

	override setObserver(@Suspending Observer<IN, OUT> observer) {
		this.observer = observer
		// if we already have a value or error, push it through immediately
		if(cachedValue !== null) {
			fulfilled = true
			observer.value(cachedValue.key, cachedValue.value)
			onCompleted
		} else if(cachedError !== null) {
			rejected = true
			observer.error(cachedError.key, cachedError.value)
			onCompleted
		}
	}
	
	@Suspendable
	override next() {
		// do nothing by default, since promises have no flow control
	}
	
	override toString() '''Promise { status: «IF fulfilled»fulfilled«ELSEIF rejected»rejected«ELSE»unfulfilled«ENDIF», «IF cachedValue !== null»value: «cachedValue»«ELSEIF cachedError !== null»error: «cachedError»«ENDIF», observed: «observer !== null» }'''
	
}
