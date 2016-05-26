package nl.kii.async.promise

import java.util.List
import java.util.Map
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import nl.kii.async.ObservableOperation
import nl.kii.async.Observer
import nl.kii.async.annotation.Blocking
import nl.kii.async.annotation.MultiThreaded
import nl.kii.util.Period

import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.util.DateExtensions.*
import nl.kii.util.Opt

final class PromiseExtensions {

	// CREATION ///////////////////////////////////////////////////////////////////////
	
	/** Create a promise of the given type */
	def static <T> promise(Class<T> type) {
		new Input<T>
	}

	/** Create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Input<Pair<K, V>>
	}

	/** Create a promise of a list of the given type */
	def static <OUT> promiseList(Class<OUT> type) {
		new Input<List<OUT>>
	}

	/** Create a promise of a map of the given key and value types */
	def static <K, V> promiseMap(Pair<Class<K>, Class<V>> type) {
		new Input<Map<K, V>>
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
		promises.stream.map[it.asTask].resolve(0).start
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

	def static <IN, OUT, MAP> Promise<IN, MAP> map(Promise<IN, OUT> promise, (IN, OUT)=>MAP mapFn) {
		val newPromise = new Deferred<IN, MAP>
		ObservableOperation.map(promise, newPromise, mapFn)
		newPromise
	}
	
	def static <IN, OUT, MAP> Promise<IN, MAP> map(Promise<IN, OUT> promise, (OUT)=>MAP mapFn) {
		promise.map [ in, out | mapFn.apply(out) ]
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

	def static <IN, OUT, MAP> Promise<IN, MAP> call(Promise<IN, OUT> promise, (IN, OUT)=>Promise<?, MAP> mapFn) {
		promise.map(mapFn).flatten
	}

	def static <IN, OUT, MAP> Promise<IN, MAP> call(Promise<IN, OUT> promise, (OUT)=>Promise<?, MAP> mapFn) {
		promise.map(mapFn).flatten
	}

	def static <IN, OUT> Promise<IN, OUT> perform(Promise<IN, OUT> promise, (IN, OUT)=>Task mapFn) {
		promise.call [ in, value | mapFn.apply(in, value).map [ value ] ]
	}

	def static <IN, OUT> Promise<IN, OUT> perform(Promise<IN, OUT> promise, (OUT)=>Task mapFn) {
		promise.call [ in, value | mapFn.apply(value).map [ value ] ]
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
		ObservableOperation.flatten(promise, newPromise, 1)
		newPromise
	}
	
	// FORWARDING /////////////////////////////////////////////////////////////////////

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

	def static <IN, OUT, IN2> void forward(Promise<IN, OUT> promise, Deferred<IN2, OUT> deferred) {
		promise.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				deferred.value(null, value)
			}
			
			override error(IN in, Throwable t) {
				deferred.error(null, t)
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
	
	// BLOCKING ///////////////////////////////////////////////////////////////////////
	
	/** Wrap the promise into a future that can block. */
	@Blocking
	def static <IN, OUT> Future<OUT> asFuture(Promise<IN, OUT> promise) {
		new PromisedFuture(promise)
	}

	/** 
	 * Blocks the current thread to wait for the value for a maximum specified period.
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Blocking
	def static <IN, OUT> await(Promise<IN, OUT> promise, Period timeout) throws TimeoutException {
		new PromisedFuture(promise).get(timeout.ms, TimeUnit.MILLISECONDS)
	}

	/** 
	 * Blocks the current thread to wait for the value for at most one second
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Blocking
	def static <IN, OUT> await(Promise<IN, OUT> promise) throws TimeoutException {
		promise.await(1.sec)
	}

	// MULTITHREADED //////////////////////////////////////////////////////////////////

	/** 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	@MultiThreaded
	def static <IN, OUT> Promise<OUT, OUT> promise(ExecutorService service, Callable<OUT> callable) {
		val promise = new Input<OUT>
		val Runnable processor = [|
			try {
				val result = callable.call
				promise.set(result)
			} catch(Throwable t) {
				promise.error(t)
			}
		]
		service.submit(processor)
		promise
	}	

	/** 
	 * Execute the runnable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| doSomeHeavyLifting ].then [ println('done!') ]
	 */
	@MultiThreaded
	def static Task task(ExecutorService service, Runnable runnable) {
		val task = new Task
		val Runnable processor = [|
			try {
				runnable.run
				task.complete
			} catch(Throwable t) {
				task.error(t)
			}
		]
		service.submit(processor)
		task
	}

	/** 
	 * Create a timer function to be used in delay methods.
	 * A timer function takes a period and returns a task that completes when that period has expired.
	 */
	@MultiThreaded
	def static (Period)=>Task timerFn(ScheduledExecutorService executor) {
		[ period |
			val task = new Task
			executor.schedule([ task.complete ], period.ms, TimeUnit.MILLISECONDS)
			task
		]
	}
		
}
