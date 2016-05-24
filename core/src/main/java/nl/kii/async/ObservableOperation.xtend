package nl.kii.async

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import nl.kii.async.annotation.Backpressure
import nl.kii.async.annotation.Cold
import nl.kii.async.annotation.Hot
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Unsorted
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import nl.kii.util.Opt
import nl.kii.util.Period

import static extension nl.kii.util.OptExtensions.*
import static extension nl.kii.util.ThrowableExtensions.*
import static extension nl.kii.async.promise.PromiseExtensions.*

final class ObservableOperation {

	// OBSERVATION /////////////////////////////////////////////////////////////////////////////

	/** 
	 * Lets you observe with multiple observers at the same time.
	 */
	@Cold @NoBackpressure
	def static <IN, OUT> observeWith(Observable<IN, OUT> observable, Observer<IN, OUT>... observers) {
		observable.observer = new Observer<IN, OUT> {
			override value(IN in, OUT value) {
				for(observer : observers) {
					try {
						observer.value(in, value)
					} catch(Throwable t) {
						observer.error(in, t)
					}
				}
			}
			override error(IN in, Throwable t) {
				for(observer : observers) {
					try {
						observer.error(in, t)
					} catch(Throwable t2) {
						// let errors of error handling die quietly
					}
				}
			}
			override complete() {
				for(observer : observers) {
					try {
						observer.complete
					} catch(Throwable t) {
						observer.error(null, t)
					}
				}
			}
		}
	}
	
	// COMBINE /////////////////////////////////////////////////////////////////////////////////

	@Hot @Backpressure @Unsorted
	def static <IN, OUT> void merge(Observer<IN, OUT> observer, Observable<IN, OUT>... observables) {
		val completed = new AtomicInteger(0)
		for(observable : observables) {
			observable.observer = new Observer<IN, OUT> {
				
				override value(IN in, OUT value) {
					observer.value(in, value)
					observable.next
				}
				
				override error(IN in, Throwable t) {
					observer.error(in, t)
					observable.next
				}
				
				override complete() {
					// complete when all observables are complete
					if(completed.incrementAndGet >= observables.size) {
						observer.complete
					}
				}
				
			}
			observable.next
		}
	}

	// UNTIL ///////////////////////////////////////////////////////////////////////////////////

	@Cold @Backpressure
	def static <IN, OUT> void until(Observable<IN, OUT> observable, Observer<IN, OUT> observer, (IN, OUT, Long, Long)=>boolean stopObservingFn) {
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)
		observable.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				if(stopObservingFn.apply(in, value, index.incrementAndGet, passed.get)) {
					observer.complete
				} else {
					passed.incrementAndGet
					observer.value(in, value)
				}
			}
			
			override error(IN in, Throwable t) {
				observer.error(in, t)
			}
			
			override complete() {
				observer.complete
			}
			
		}
	}

	// MAPPING /////////////////////////////////////////////////////////////////////////////////

	@Cold @Backpressure
	def static <IN, OUT, MAP> void map(Observable<IN, OUT> observable, Observer<IN, MAP> observer, (IN, OUT)=>MAP mapFn) {
		observable.observer = new Observer<IN, OUT> {
			override value(IN in, OUT value) {
				try {
					val mapped = mapFn.apply(in, value)
					observer.value(in, mapped)
				} catch(Throwable t) {
					observer.error(in, t)
				}
			}
			override error(IN in, Throwable t) {
				observer.error(in, t)
			}
			override complete() {
				observer.complete
			}
		}
	}
	
	@Cold @Unsorted @NoBackpressure
	def static <IN, OUT, IN2> flatten(Observable<IN, ? extends Observable<?, OUT>> observable, Observer<IN, OUT> observer, int maxConcurrency) {
		val isFinished = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)

		observable.observer = new Observer<IN, Observable<IN2, OUT>> {
			
			override value(IN in, Observable<IN2, OUT> innerObservable) {
				
				innerObservable.observer = new Observer<IN2, OUT> {
					
					override value(IN2 unused, OUT value) {
						observer.value(in, value)
						innerObservable.next
					}
					
					override error(IN2 unused, Throwable t) {
						observer.error(in, t)
						innerObservable.next
					}
					
					override complete() {
						// if we have space for more parallel processes, ask for the next value
						if(processes.decrementAndGet == 0 && isFinished.compareAndSet(true, false)) {
							observer.complete
						} 
					}
					
				}
				// we are starting to process this inner observable
				processes.incrementAndGet
				innerObservable.next
				// if we have space for more parallel processes, ask for the next value
				if(maxConcurrency > processes.get || maxConcurrency == 0) observable.next
			}
			
			override error(IN in, Throwable t) {
				observer.error(in, t)
			}
			
			override complete() {
				if(processes.get == 0) {
					// we are not parallel processing, you may inform the listening stream
					observer.complete
				} else {
					// we are still busy, so remember to call finish when we are done
					isFinished.set(true)
				}
			}
			
		}
	}

	@Cold @Backpressure	
	def static <IN1, IN2, OUT> mapInput(Observable<IN1, OUT> observable, Observer<IN2, OUT> observer, (IN1, Opt<OUT>)=>IN2 inputMapFn) {
		observable.observer = new Observer<IN1, OUT> {
			
			override value(IN1 in1, OUT value) {
				val in2 = inputMapFn.apply(in1, value.option)
				observer.value(in2, value)
			}
			
			override error(IN1 in1, Throwable t) {
				val in2 = inputMapFn.apply(in1, none)
				observer.error(in2, t)
			}
			
			override complete() {
				observer.complete
			}
			
		}
	}
	
	// ERROR HANDLING //////////////////////////////////////////////////////////////////////////
	
	@Cold @Backpressure
	def static <IN, OUT, E extends Throwable> void onError(Observable<IN, OUT> observable, Observer<IN, OUT> observer, Class<E> errorClass, boolean swallow, (IN, E)=>void onErrorFn) {
		observable.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				observer.value(in, value)
			}
			
			override error(IN in, Throwable t) {
				try {
					if(errorClass.isAssignableFrom(t.class)) {
						onErrorFn.apply(in, t as E)
						if(swallow) {
							observable.next
						} else {
							observer.error(in, t)
						}
					} else {
						observer.error(in, t)
					}
				} catch(Exception e) {
					 observer.error(in, t)
				}
			}
			
			override complete() {
				observer.complete
			}
			
		}
	}

	@Cold @Backpressure
	def static <IN, OUT, ERROR extends Throwable> void onErrorMap(Observable<IN, OUT> observable, Observer<IN, OUT> observer, Class<ERROR> errorClass, boolean swallow, (IN, ERROR)=>OUT onErrorMapFn) {
		observable.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				observer.value(in, value)
			}
			
			override error(IN in, Throwable error) {
				try {
					if(error.matches(errorClass)) {
						val value = onErrorMapFn.apply(in, error as ERROR)
						observer.value(in, value)
					} else {
						observer.error(in, error)
					}
				} catch(Throwable t) {
					observer.error(in, t)
				}
			}
			
			override complete() {
				observer.complete
			}
			
		}
	}
	
	/** Asynchronously map an error back to a value. Swallows the error. */
	@Cold @Unsorted @Backpressure
	def static <ERROR extends Throwable, IN, OUT, IN2> void onErrorCall(Observable<IN, OUT> observable, Observer<IN, OUT> observer, Class<ERROR> errorType, (IN, ERROR)=>Promise<IN2, OUT> onErrorCallFn) {
		val completed = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)
		observable.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				observer.value(in, value)
			}
			
			override error(IN in, Throwable error) {
				if(error.matches(errorType)) {
					// start a new async process by calling the mapFn
					try {
						processes.incrementAndGet
						val promise = onErrorCallFn.apply(in, error as ERROR)
						promise.observer = new Observer<IN2, OUT> {
							
							override value(IN2 unused, OUT value) {
								observer.value(in, value)
							}
							
							override error(IN2 unused, Throwable error) {
								observer.error(in, error)
							}
							
							override complete() {
								// if the stream completed and this was the last process, we are done
								if(processes.decrementAndGet <= 0 && completed.get) {
									observer.complete
								}
							}
							
						}
					} catch(Throwable t) {
						observer.error(in, t)
						// if the stream completed and this was the last process, we are done
						if(processes.decrementAndGet <= 0 && completed.get) {
							observer.complete
						}
					}
				} else {
					observer.error(in, error)
				}
			}
			
			override complete() {
				// the observable is complete
				completed.set(true)
				// if there are no more processes running, we are done
				if(processes.get <= 0) {
					observer.complete
				}
			}
			
		}
		
	}
	
	// RETENTION AND TIME //////////////////////////////////////////////////////////////////////

	/** 
	 * Adds delay to each observed value.
	 * If the observable is completed, it will only send complete to the observer once all delayed values have been sent.
	 */	
	@Cold @Backpressure	
	def static <IN1, IN, OUT> delay(Observable<IN, OUT> observable, Observer<IN, OUT> observer, Period delay, (Period)=>Task timerFn) {
		observable.observer = new Observer<IN, OUT> {
			
			val timers = new AtomicInteger
			val completed = new AtomicBoolean
			
			override value(IN in, OUT value) {
				timers.incrementAndGet
				timerFn.apply(delay).then [
					val openTimers = timers.decrementAndGet
					observer.value(in, value)
					if(completed.get || openTimers == 0) {
						observer.complete
					}
				]
			}
			
			override error(IN in, Throwable t) {
				observer.error(in, t)
			}
			
			override complete() {
				if(completed.compareAndSet(false, true)) {
					if(timers.get == 0) observer.complete
				}
			}
			
		}
	}
	
	
	
	
}
