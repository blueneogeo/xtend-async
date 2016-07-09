package nl.kii.async.promise

import java.util.List
import nl.kii.async.observable.ObservableOperation
import nl.kii.async.observable.Observer
import nl.kii.util.Opt
import nl.kii.util.Period

import static extension nl.kii.async.stream.StreamExtensions.*

final class PromiseExtensions {

	// CREATION ///////////////////////////////////////////////////////////////////////
	
	/** Create a new input, letting you use type inference from the rest of your code to find the type. */
	def static <OUT> newInput() {
		new Input<OUT>
	}
	
	/** Create a fulfilled promise of the passed value */
	def static <OUT> promise(OUT value) {
		new Input<OUT>(value)
	}

	/** Create a fulfilled promise of the passed input and value */
	def static <IN, OUT> promise(IN from, OUT value) {
		new Deferred<IN, OUT> => [ value(from, value) ]
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
			
			override value(IN in, OUT value) {
				task.complete
			}
			
			override error(IN in, Throwable t) {
				task.error(t)
			}
			
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
		promises.stream.parallel(0).map[it.asTask].resolve.start
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
	def static <IN, OUT> Task any(List<? extends Promise<IN, OUT>> promises) {
		val Task task = new Task
		for(promise : promises) {
			promise.observer = new Observer<IN, OUT> {
				
				override value(IN in, OUT value) {
					task.complete
				}
				
				override error(IN in, Throwable t) {
					task.error(null, t)
				}
				
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
	
	def static <IN, OUT> Task start(Promise<IN, OUT> promise) {
		promise.next
		promise.asTask
	}
	
	// ERROR HANDLING /////////////////////////////////////////////////////////////////	

	def static <IN, OUT, E extends Throwable> Promise<IN, OUT> on(Promise<IN, OUT> promise, Class<E> errorClass, (E)=>void onErrorFn) {
		promise.on(errorClass) [ in, out | onErrorFn.apply(out) ]
	}

	def static <IN, OUT, E extends Throwable> Promise<IN, OUT> on(Promise<IN, OUT> promise, Class<E> errorClass, (IN, E)=>void onErrorFn) {
		val newPromise = new Deferred<IN, OUT>
		ObservableOperation.onError(promise, newPromise, errorClass, false, onErrorFn)
		newPromise
	}

	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT> map(Promise<IN, OUT> promise, Class<ERROR> errorType, (ERROR)=>OUT onErrorMapFn) {
		promise.map(errorType) [ input, err | onErrorMapFn.apply(err) ]
	}

	/** Map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT> Promise<IN, OUT> map(Promise<IN, OUT> promise, Class<ERROR> errorClass, (IN, ERROR)=>OUT onErrorMapFn) {
		val newPromise = new Deferred<IN, OUT>
		ObservableOperation.onErrorMap(promise, newPromise, errorClass, true, onErrorMapFn)
		newPromise
	}

	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT> Promise<IN, OUT> call(Promise<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>Promise<Object, OUT> onErrorPromiseFn) {
		stream.call(errorType) [ IN in, ERROR err | onErrorPromiseFn.apply(err) ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <ERROR extends Throwable, IN, OUT, IN2, PROMISE extends Promise<IN2, OUT>> Promise<IN, OUT> call(Promise<IN, OUT> promise, Class<ERROR> errorClass, (IN, ERROR)=>PROMISE onErrorPromiseFn) {
		val newPromise = new Deferred<IN, OUT>
		ObservableOperation.onErrorCall(promise, newPromise, errorClass, onErrorPromiseFn)
		newPromise
	}
	
	// MAPPING AND EFFECTS ///////////////////////////////////////////////////////////
	
	def static <IN, OUT, MAP> Promise<IN, MAP> map(Promise<IN, OUT> promise, (OUT)=>MAP mapFn) {
		promise.map [ in, out | mapFn.apply(out) ]
	}

	def static <IN, OUT, MAP> Promise<IN, MAP> map(Promise<IN, OUT> promise, (IN, OUT)=>MAP mapFn) {
		val newPromise = new Deferred<IN, MAP>
		ObservableOperation.map(promise, newPromise, mapFn)
		newPromise
	}

	def static <IN, OUT> Promise<IN, OUT> effect(Promise<IN, OUT> promise, (OUT)=>void effectFn) {
		promise.map [ in, out | effectFn.apply(out) return out ]
	}

	def static <IN, OUT> Promise<IN, OUT> effect(Promise<IN, OUT> promise, (IN, OUT)=>void effectFn) {
		promise.map [ in, out | effectFn.apply(in, out) return out ]
	}

	def static <IN, OUT> Task then(Promise<IN, OUT> promise, (OUT)=>void effectFn) {
		promise.effect(effectFn).asTask
	}

	def static <IN, OUT> Task then(Promise<IN, OUT> promise, (IN, OUT)=>void effectFn) {
		promise.effect(effectFn).asTask
	}

	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Promise<IN, MAP> call(Promise<IN, OUT> promise, (OUT)=>PROMISE mapFn) {
		promise.map(mapFn).flatten
	}

	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Promise<IN, MAP> call(Promise<IN, OUT> promise, (IN, OUT)=>PROMISE mapFn) {
		promise.map(mapFn).flatten
	}

	def static <IN, OUT> Promise<IN, OUT> perform(Promise<IN, OUT> promise, (OUT)=>Promise<?, ?> mapFn) {
		promise.call [ in, value | mapFn.apply(value).map [ value ] ]
	}

	def static <IN, OUT> Promise<IN, OUT> perform(Promise<IN, OUT> promise, (IN, OUT)=>Promise<?, ?> mapFn) {
		promise.call [ in, value | mapFn.apply(in, value).map [ value ] ]
	}
	
	/** 
	 * Transform the input of a promise based on the existing input and output.
	 * <p>
	 * The input mapping function is passed both an input and an output parameter. The output
	 * is only available when a normal value comes in, and not for errors or finishes, in which
	 * case it is none.
	 */	
	def static <IN1, IN2, OUT> Promise<IN2, OUT> mapInput(Promise<IN1, OUT> promise, (IN1, Opt<OUT>)=>IN2 inputMapFn) {
		val newPromise = new Deferred<IN2, OUT>
		ObservableOperation.mapInput(promise, newPromise, inputMapFn)
		newPromise
	}
	
	/**  Transform the input of a promise based on the existing input. */	
	def static <IN1, IN2, OUT> Promise<IN2, OUT> mapInput(Promise<IN1, OUT> promise, (IN1)=>IN2 inputMapFn) {
		promise.mapInput [ in1 | inputMapFn.apply(in1) ]
	}

	// TIME AND RETENTION /////////////////////////////////////////////////////////////
	
	def static <IN, OUT> Promise<IN, OUT> delay(Promise<IN, OUT> promise, Period delay, (Period)=>Task timerFn) {
		val newPromise = new Deferred<IN, OUT>
		ObservableOperation.delay(promise, newPromise, delay, timerFn)
		newPromise
	}

	// REDUCTION //////////////////////////////////////////////////////////////////////
	
	def static <IN, OUT> Promise<IN, OUT> flatten(Promise<IN, ? extends Promise<?, OUT>> promise) {
		val newPromise = new Deferred<IN, OUT>
		ObservableOperation.flatten(promise, newPromise)
		newPromise
	}
	
	// FORWARDING /////////////////////////////////////////////////////////////////////

	/** Complete a task when a promise is fulfilled */
	def static <IN, OUT> void completes(Promise<IN, OUT> promise, Task task) {
		promise.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				task.complete
			}
			
			override error(IN in, Throwable t) {
				task.error(t)
			}
			
			override complete() {
				task.complete
			}
			
		}
	}

	/** Chain the input and output of a promise to a deferred. */
	def static <IN, OUT> void pipe(Promise<IN, OUT> promise, Deferred<IN, OUT> deferred) {
		promise.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				deferred.value(in, value)
			}
			
			override error(IN in, Throwable t) {
				deferred.error(in, t)
			}
			
			override complete() {
				// do nothing
			}
			
		}
	}

	/** Take the output value of a promise and set it to an input when it is fulfilled */
	def static <IN, OUT> void pipe(Promise<IN, OUT> promise, Input<OUT> input) {
		promise.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				input.value(null, value)
			}
			
			override error(IN in, Throwable t) {
				input.error(null, t)
			}
			
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
	def static <IN, OUT> check(Promise<IN, OUT> stream, String checkDescription, (OUT)=>boolean checkFn) {
		stream.check(checkDescription) [ in, out | checkFn.apply(out) ]
	}

	/** 
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <IN, OUT> check(Promise<IN, OUT> stream, String checkDescription, (IN, OUT)=>boolean checkFn) {
		stream.effect [ in, out |
			if(!checkFn.apply(in, out)) throw new Exception(
			'stream.check ("' + checkDescription + '") failed for checked value: ' + out + '. Input was: ' + in)
		]
	}
		
}
