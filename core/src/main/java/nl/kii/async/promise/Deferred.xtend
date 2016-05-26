package nl.kii.async.promise

import nl.kii.async.observable.Observer

/** 
 * A deferred promises a result, based on an incoming request.
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
			observer.complete
		} else {
			cachedValue = in -> value
		}
	}

	override synchronized error(IN in, Throwable t) {
		if(!pending) return;
		rejected = true
		if(observer != null) {
			observer.error(in, t)
			observer.complete
		} else {
			cachedError = in -> t
		}
	}
	
	override synchronized complete() {
		// not supported
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
			observer.value(cachedValue.key, cachedValue.value)
			observer.complete
		} else if(cachedError != null) {
			observer.error(cachedError.key, cachedError.value)
			observer.complete
		}
	}
	override synchronized next() {
		// do nothing by default, since promises have no flow control
	}
	
	override synchronized toString() '''Promise { status: «IF fulfilled»fulfilled«ELSEIF rejected»rejected«ELSE»unfulfilled«ENDIF», «IF cachedValue!=null»value: «cachedValue»«ELSEIF cachedError != null»error: «cachedError»«ENDIF», observed: «observer!=null» }'''
	
}
