package nl.kii.async.promise

import nl.kii.async.observable.Observer

/** 
 * A deferred promises a result, based on an incoming request.
 * A Deferred is thread-safe.
 */
class Deferred<IN, OUT> implements Observer<IN, OUT>, Promise<IN, OUT> {

	protected boolean fulfilled = false
	protected boolean rejected = false

	protected Observer<IN, OUT> observer

	protected Pair<IN, OUT> cachedValue
	protected Pair<IN, Throwable> cachedError

	override synchronized value(IN in, OUT value) {
		if(!pending) return;
		fulfilled = true
		if(observer != null) {
			observer.value(in, value)
			onCompleted
		} else {
			cachedValue = in -> value
		}
	}

	override synchronized error(IN in, Throwable t) {
		if(!pending) return;
		rejected = true
		if(observer != null) {
			observer.error(in, t)
			onCompleted
		} else {
			cachedError = in -> t
		}
	}
	
	override complete() {
		// do nothing, because only value and error can complete a promise!
	}
	
	/** Called internally, so the observer.complete is called, and we can clean up a bit */
	protected def onCompleted() {
		observer?.complete
		// clean up after we are done
		observer = null
		cachedValue = null
		cachedError = null
	}

	override synchronized isPending() {
		!fulfilled && !rejected
	}
	
	override synchronized isFulfilled() {
		this.fulfilled
	}
	
	override synchronized isRejected() {
		this.rejected
	}

	override synchronized setObserver(Observer<IN, OUT> observer) {
		this.observer = observer
		// if we already have a value or error, push it through immediately
		if(cachedValue != null) {
			fulfilled = true
			observer.value(cachedValue.key, cachedValue.value)
			onCompleted
		} else if(cachedError != null) {
			rejected = true
			observer.error(cachedError.key, cachedError.value)
			onCompleted
		}
	}
	override next() {
		// do nothing by default, since promises have no flow control
	}
	
	override synchronized toString() '''Promise { status: «IF fulfilled»fulfilled«ELSEIF rejected»rejected«ELSE»unfulfilled«ENDIF», «IF cachedValue!=null»value: «cachedValue»«ELSEIF cachedError != null»error: «cachedError»«ENDIF», observed: «observer!=null» }'''
	
}
