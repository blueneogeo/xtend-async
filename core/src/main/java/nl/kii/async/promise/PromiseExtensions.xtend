package nl.kii.async.promise

import co.paralleluniverse.fibers.Suspendable
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.SuspendableFunctions.Function1
import nl.kii.async.SuspendableFunctions.Function2
import nl.kii.async.SuspendableProcedures.Procedure1
import nl.kii.async.SuspendableProcedures.Procedure2
import nl.kii.async.annotation.Suspending
import nl.kii.async.observable.Observable
import nl.kii.async.observable.ObservableOperation
import nl.kii.async.observable.Observer
import nl.kii.util.Opt
import nl.kii.util.Period

@Suspendable
final class PromiseExtensions {

	// CREATION ///////////////////////////////////////////////////////////////////////
	
	/**
	 * Deprecated: use Promises.newInput().
	 * Create a new input, letting you use type inference from the rest of your code to find the type.
	 */
	@Deprecated
	def static <OUT> newInput() {
		Promises.newInput
	}
	
	/** Create a fulfilled promise of the passed value */
	def static <OUT> Promise<OUT, OUT> promise(OUT value) {
		Promises.newPromise(value)
	}

	/**
	 * Deprecated: use Promises.newPromise(from, value). 
	 * Create a fulfilled promise of the passed input and value
	 */
	@Deprecated
	def static <IN, OUT> promise(IN from, OUT value) {
		Promises.newPromise(from, value)
	}
	
	/** Shortcut for quickly creating a completed task. Also useful for setting up a promise chain. */
	def static Task complete() {
		new Task => [ 
			complete
		]
	}

	/** Shortcut for quickly creating a completed task. Also useful for setting up a promise chain. */	
	def static <IN> complete(IN from) {
		new Deferred<IN, Boolean> => [
			value(from, true)
		]
	}

	/** Creates a task that completes when the promise completes, and that gives an error when the promise has an error. */	
	def static <IN, OUT> Task asTask(Promise<IN, OUT> promise) {
		val task = new Task {
			
			override next() {
				promise.next
			}
			
		}
		promise.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				task.complete
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				task.error(t)
			}
			
			@Suspendable
			override complete() {
				task.complete
			}
			
		}
		task
	}
	
	/** 
	 * Create a new Task that completes when all wrapped tasks are completed.
	 * Errors created by the tasks are propagated into the resulting task.
	 */
	def static Task all(Promise<?, ?>... promises) {
		all(promises.toList)
	}

	/** 
	 * Create a new Task that completes when all wrapped promises are completed.
	 * Errors created by the tasks are propagated into the resulting task.
	 */
	def static Task all(Iterable<? extends Promise<?, ?>> promises) {
		// promises.stream.parallel(0).map[it.asTask].resolve.start // needs @Suspendable, below does not
		val Task task = new Task
		val count = new AtomicInteger(promises.size)
		for(promise : promises) {
			(promise as Promise<Object, Object>).observer = new Observer<Object, Object> {
				
				override value(Object in, Object value) {
					if(count.decrementAndGet == 0) task.complete
				}
				
				override error(Object in, Throwable t) {
					task.error(null, t)
				}
				
				override complete() {
				}
				
			}
			promise.next
		}
		task
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <IN, OUT, P extends Promise<IN, OUT>> Task any(P... promises) {
		any(promises.toList)
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <IN, OUT> Task any(Iterable<? extends Promise<IN, OUT>> promises) {
		val Task task = new Task
		for(promise : promises) {
			promise.observer = new Observer<IN, OUT> {
				
				@Suspendable
				override value(IN in, OUT value) {
					task.complete
				}
				
				@Suspendable
				override error(IN in, Throwable t) {
					task.error(null, t)
				}
				
				@Suspendable
				override complete() {
					task.complete
				}
				
			}
			promise.next
		}
		task
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	@Suspendable
	def static <T> << (Input<T> promise, T value) {
		promise.set(value)
		promise
	}
	
	/** All/And */
	def static Task && (Promise<?, ?> p1, Promise<?, ?> p2) {
		all(p1, p2)
	}
	
	/** Any/Or */
	def static <I, O> Task || (Promise<I, O> p1, Promise<I, O> p2) {
		any(p1, p2)
	}
	
	// STARTING ///////////////////////////////////////////////////////////////////////
	
	@Suspendable
	def static <IN, OUT> Task start(Promise<IN, OUT> promise) {
		promise.next
		promise.asTask
	}
	
	// SYNCHRONIZATION /////////////////////////////////////////////////////////////////////////
	
	/** 
	 * Synchronizes the output of a promise to some object you provide.
	 * <p>
	 * This lets you synchronize the output of multiple promises around a single object (for example, a stream).  
	 */
	def static <IN, OUT> Promise<IN, OUT> synchronize(Promise<IN, OUT> promise, Object mutex) {
		val newPromise = new Deferred<IN, OUT>
		
		promise.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				synchronized (mutex) {
					newPromise.value(in, value)
				}
				
			}
			
			override error(IN in, Throwable t) {
				synchronized (mutex) {
					newPromise.error(in, t)
				}
			}
			
			override complete() {
				synchronized (mutex) {
					newPromise.complete
				}
			}
			
		}
		
		newPromise
	}
	
	// ERROR HANDLING /////////////////////////////////////////////////////////////////	

	def static <IN, OUT, E extends Throwable> Promise<IN, OUT> on(Promise<IN, OUT> promise, Class<E> errorClass, @Suspending Procedure1<E> onErrorFn) {
		promise.on(errorClass) [ in, out | onErrorFn.apply(out) ]
	}

	def static <IN, OUT, E extends Throwable> Promise<IN, OUT> on(Promise<IN, OUT> promise, Class<E> errorClass, @Suspending Procedure2<IN, E> onErrorFn) {
		val newPromise = new Deferred<IN, OUT>
		ObservableOperation.onError(promise, newPromise, errorClass, false, onErrorFn)
		newPromise
	}

	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT> Promise<IN, OUT> map(Promise<IN, OUT> promise, Class<ERROR> errorType, @Suspending Function1<ERROR, OUT> onErrorMapFn) {
		promise.map(errorType) [ input, err | onErrorMapFn.apply(err) ]
	}

	/** Map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT> Promise<IN, OUT> map(Promise<IN, OUT> promise, Class<ERROR> errorClass, @Suspending Function2<IN, ERROR, OUT> onErrorMapFn) {
		new Deferred<IN, OUT> => [ ObservableOperation.onErrorMap(promise, it, errorClass, true, onErrorMapFn) ]
	}

	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT> Promise<IN, OUT> call(Promise<IN, OUT> stream, Class<ERROR> errorType, @Suspending Function1<ERROR, Promise<Object, OUT>> onErrorPromiseFn) {
		stream.call(errorType) [ IN in, ERROR err | onErrorPromiseFn.apply(err) ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT, IN2> Promise<IN, OUT> call(Promise<IN, OUT> promise, Class<ERROR> errorClass, @Suspending Function2<IN, ERROR, Promise<IN2, OUT>> onErrorPromiseFn) {
		new Deferred<IN, OUT> => [ ObservableOperation.onErrorCall(promise, it, errorClass, onErrorPromiseFn) ]
	}
	
	// MAPPING AND EFFECTS ///////////////////////////////////////////////////////////
	
	def static <IN, OUT, MAP> Promise<IN, MAP> map(Promise<IN, OUT> promise, @Suspending Function1<OUT, MAP> mapFn) {
		promise.map [ in, out | mapFn.apply(out) ]
	}

	def static <IN, OUT, MAP> Promise<IN, MAP> map(Promise<IN, OUT> promise, @Suspending Function2<IN, OUT, MAP> mapFn) {
		val d = new Deferred<IN, MAP>
		ObservableOperation.map(promise, d, mapFn)
		d
		// new Deferred<IN, MAP> => [ ObservableOperation.map(promise, it, mapFn) ]
	}

	def static <IN, OUT> Promise<IN, OUT> effect(Promise<IN, OUT> promise, @Suspending Procedure1<OUT> effectFn) {
		promise.map [ in, out | effectFn.apply(out) return out ]
	}

	def static <IN, OUT> Promise<IN, OUT> effect(Promise<IN, OUT> promise, @Suspending Procedure2<IN, OUT> effectFn) {
		promise.map [ in, out | effectFn.apply(in, out) return out ]
	}

	def static <IN, OUT> Task then(Promise<IN, OUT> promise, @Suspending Procedure1<OUT> effectFn) {
		promise.effect(effectFn).asTask
	}

	def static <IN, OUT> Task then(Promise<IN, OUT> promise, @Suspending Procedure2<IN, OUT> effectFn) {
		promise.effect(effectFn).asTask
	}

	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Promise<IN, MAP> call(Promise<IN, OUT> promise, @Suspending Function1<OUT, PROMISE> mapFn) {
		promise.map(mapFn).flatten
	}

	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Promise<IN, MAP> call(Promise<IN, OUT> promise, @Suspending Function2<IN, OUT, PROMISE> mapFn) {
		promise.map(mapFn).flatten
	}

	def static <IN, OUT> Promise<IN, OUT> perform(Promise<IN, OUT> promise, @Suspending Function1<OUT, Promise<?, ?>> mapFn) {
		promise.call [ in, value |  mapFn.apply(value).asTask.map [ value ] ]
	}

	def static <IN, OUT> Promise<IN, OUT> perform(Promise<IN, OUT> promise, @Suspending Function2<IN, OUT, Promise<?, ?>> mapFn) {
		promise.call [ in, value | mapFn.apply(in, value).asTask.map [ value ] ]
	}
	
	/** 
	 * Transform the input of a promise based on the existing input and output.
	 * <p>
	 * The input mapping function is passed both an input and an output parameter. The output
	 * is only available when a normal value comes in, and not for errors or finishes, in which
	 * case it is none.
	 */	
	def static <IN1, IN2, OUT> Promise<IN2, OUT> mapInput(Promise<IN1, OUT> promise, @Suspending Function2<IN1, Opt<OUT>, IN2> inputMapFn) {
		new Deferred<IN2, OUT> => [ ObservableOperation.mapInput(promise, it, inputMapFn) ]
	}
	
	/**  Transform the input of a promise based on the existing input. */	
	def static <IN1, IN2, OUT> Promise<IN2, OUT> mapInput(Promise<IN1, OUT> promise, @Suspending Function1<IN1, IN2> inputMapFn) {
		promise.mapInput [ in1 | inputMapFn.apply(in1) ]
	}

	// TIME AND RETENTION /////////////////////////////////////////////////////////////
	
	def static <IN, OUT> Promise<IN, OUT> delay(Promise<IN, OUT> promise, Period delay, @Suspending Function1<Period, Task> timerFn) {
		new Deferred<IN, OUT> => [ ObservableOperation.delay(promise, it, delay, timerFn) ]
	}

	// REDUCTION //////////////////////////////////////////////////////////////////////
	
	def static <IN, OUT, PROMISE extends Promise<?, OUT>> Promise<IN, OUT> flatten(Promise<IN, PROMISE> promise) {
		new Deferred<IN, OUT> => [ ObservableOperation.flatten(promise as Observable<IN, Promise<Object, OUT>>, it) ]
	}
	
	// FORWARDING /////////////////////////////////////////////////////////////////////

	/** Complete a task when a promise is fulfilled */
	def static <IN, OUT> void completes(Promise<IN, OUT> promise, Task task) {
		promise.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				task.complete
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				task.error(t)
			}
			
			@Suspendable
			override complete() {
				task.complete
			}
			
		}
	}

	/** Chain the input and output of a promise to a deferred. */
	def static <IN, OUT> void pipe(Promise<IN, OUT> promise, Deferred<IN, OUT> deferred) {
		promise.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				deferred.value(in, value)
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				deferred.error(in, t)
			}
			
			@Suspendable
			override complete() {
				// do nothing
			}
			
		}
	}

	/** Take the output value of a promise and set it to an input when it is fulfilled */
	def static <IN, OUT> void pipe(Promise<IN, OUT> promise, Input<OUT> input) {
		promise.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				input.value(null, value)
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				input.error(null, t)
			}
			
			@Suspendable
			override complete() {
				// do nothing
			}
			
		}
	}
	

	// ASSERTION ////////////////////////////////////////////////////////////////
	
	/**
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <IN, OUT> check(Promise<IN, OUT> stream, String checkDescription, @Suspending Function1<OUT, Boolean> checkFn) {
		stream.check(checkDescription) [ in, out | checkFn.apply(out) ]
	}

	/** 
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	@Suspendable
	def static <IN, OUT> check(Promise<IN, OUT> stream, String checkDescription, @Suspending Function2<IN, OUT, Boolean> checkFn) {
		stream.effect [ in, out |
			if(!checkFn.apply(in, out)) throw new Exception(
			'stream.check ("' + checkDescription + '") failed for checked value: ' + out + '. Input was: ' + in)
		]
	}
		
}
