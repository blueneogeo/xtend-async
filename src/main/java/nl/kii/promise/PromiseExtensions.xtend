package nl.kii.promise

import java.util.List
import java.util.Map
import nl.kii.stream.Entry
import nl.kii.stream.Error
import nl.kii.stream.Stream
import nl.kii.stream.Value

import static extension nl.kii.stream.StreamExtensions.*

class PromiseExtensions {
	
	// CREATING PROMISES AND TASKS ////////////////////////////////////////////
	
	/** Create a promise of the given type */
	def static <T> promise(Class<T> type) {
		new Promise<T>
	}

	/** Create a promise of a list of the given type */
	def static <T> promiseList(Class<T> type) {
		new Promise<List<T>>
	}

	/** Create a promise of a map of the given key and value types */
	def static <K, V> promiseMap(Pair<Class<K>, Class<V>> type) {
		new Promise<Map<K, V>>
	}
	
	/** 
	 * Create a promise that immediately resolves to the passed value. 
	 */
	def static <T> promise(T value) {
		new Promise<T>(value)
	}
	
	/** Create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Promise<Pair<K, V>>
	}

//	/** Distribute work using an asynchronous method */	
//	def static <T, R, P extends IPromise<R>> IPromise<List<R>> call(List<T> data, int concurrency, (T)=>P operationFn) {
//		data.stream
//			.map(operationFn) // put each of them
//			.resolve(concurrency) // we get back a pair of the key->value used, and the done result
//			.collect // see it as a list of results
//			.first
//			=> [ operation = 'call(concurrency=' + concurrency + ')' ]
//	}

	/** Shortcut for quickly creating a completed task */	
	def static Task complete() {
		new Task => [ complete ]
	}

	/** Shortcut for quickly creating a promise with an error */	
	def static <T> Promise<T> error(String message) {
		new Promise<T> => [ error(message) ]
	}
	
	/** 
	 * Create a new Task that completes when all wrapped tasks are completed.
	 * Errors created by the tasks are propagated into the resulting task.
	 */
	def static Task all(IPromise<?, ?>... promises) {
		all(promises.toList)
	}

	/** 
	 * Create a new Task that completes when all wrapped tasks are completed.
	 * Errors created by the tasks are propagated into the resulting task.
	 */
	def static Task all(Iterable<? extends IPromise<?, ?>> promises) {
		promises.map[asTask].stream.call[it].collect.first.asTask
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <R, T, P extends IPromise<R, T>> Task any(P... promises) {
		any(promises.toList)
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <R, T> Task any(List<? extends IPromise<R, T>> promises) {
		val Task task = new Task
		for(promise : promises) {
			promise
				.onError [ task.error(it) ]
				.then [ task.complete ]
		}
		task
	}

	// COMPLETING TASKS ///////////////////////////////////////////////////////

	/** Always call onResult, whether the promise has been either fulfilled or had an error. */
	def static <R, T> always(IPromise<R, T> promise, Procedures.Procedure1<Entry<?, T>> resultFn) {
		promise.onError [ resultFn.apply(new Error(null, it)) ]
		promise.then [ resultFn.apply(new Value(null, it)) ]
		promise
	}
	
	/** Tell the promise it went wrong */
	def static <R, T> error(IPromise<R, T> promise, String message) {
		promise.error(new Exception(message))
	}

	/** Tell the promise it went wrong, with the cause throwable */
	def static <R, T> error(IPromise<R, T> promise, String message, Throwable cause) {
		promise.error(new Exception(message, cause))
	}

	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	def static <R, T> >> (R value, IPromise<R, T> promise) {
		promise.set(value)
		promise
	}
	
	/** Fulfill a promise */
	def static <R, T> << (IPromise<R, T> promise, R value) {
		promise.set(value)
		promise
	}
	
	/** All/And */
	def static Task && (IPromise<?, ?> p1, IPromise<?, ?> p2) {
		all(p1, p2)
	}
	
	/** Any/Or */
	def static <R, T> Task || (IPromise<R, T> p1, IPromise<R, T> p2) {
		any(p1, p2)
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////

	/** 
	 * Create a new promise from an existing promise, 
	 * that transforms the value of the promise
	 * once the existing promise is resolved.
	 */
	def static <R, T, M> map(IPromise<R, T> promise, (T)=>M mappingFn) {
		promise.map [ r, it | mappingFn.apply(it) ]
	}

	/** 
	 * Create a new promise from an existing promise, 
	 * that transforms the value of the promise
	 * once the existing promise is resolved.
	 */
	def static <R, T, M> map(IPromise<R, T> promise, (R, T)=>M mappingFn) {
		val newPromise = new SubPromise<R, M>(promise)
		promise.then [ r, it | newPromise.set(r, mappingFn.apply(r, it)) ]
		newPromise
			=> [ operation = 'map' ]
	}
	
	/**
	 * Maps errors back into values. 
	 * Good for alternative path resolving and providing defaults.
	 */
	def static <R, T> onErrorMap(IPromise<R, T> promise, (Throwable)=>T mappingFn) {
		val newPromise = new SubPromise<R, T>(promise)
		promise
			.onError [ r, it |
				try {
					newPromise.set(r, mappingFn.apply(it))
				} catch(Exception e) {
					newPromise.error(e)
				}
			]
			.then [ r, it | newPromise.set(r, it) ]
		newPromise
			=> [ operation = 'onErrorMap' ]
	}

	/**
	 * Maps errors back into values. 
	 * Good for alternative path resolving and providing defaults.
	 */
	def static <R, T> onErrorCall(IPromise<R, T> promise, (Throwable)=>Promise<T> mappingFn) {
		val newPromise = new Promise<T>
		promise
			.onError [
				try {
					mappingFn.apply(it)
						.onError [ newPromise.error(it) ]
						.then [ newPromise.set(it) ]
				} catch(Exception e) {
					newPromise.error(e)
				}
			]
			.then [ newPromise.set(it) ]
		newPromise
			=> [ operation = 'onErrorCall' ]
	}

	/** Flattens a promise of a promise to directly a promise. */
	def static <R, T, P extends IPromise<R, T>> flatten(IPromise<R, P> promise) {
		promise.resolve
			=> [ operation = 'flatten' ]
	}

	/** Create a stream out of a promise of a stream. */
	def static <R, P extends IPromise<R, Stream<T>>, T> toStream(P promise) {
		val newStream = new Stream<T>
		promise
			.onError [ newStream.error(it) ]
			.then [ s | s.pipe(newStream) ] 
		newStream
	}

	/** 
	 * Resolve a promise of a promise to directly a promise.
	 * Alias for Promise.flatten, added for consistent syntax with streams 
	 * */
	def static <R, T, P extends IPromise<R, T>> resolve(IPromise<R, P> promise) {
		val newPromise = new SubPromise<R, T>(promise)
		promise.then [
			onError [ r, it | newPromise.error(r, it) ] 
			.then [ r, it | newPromise.set(r, it) ]
		]
		newPromise
			=> [ operation = 'resolve' ]
	}	

	/** Performs a flatmap, which is a combination of map and flatten/resolve */	
	def static <R, T, M, P extends IPromise<R, M>> IPromise<R, M> flatMap(IPromise<R, T> promise, (T)=>P promiseFn) {
		promise.map(promiseFn).flatten
			=> [ operation = 'flatMap' ]
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////
	
	/**
	 * Perform some side-effect action based on the promise. It should not affect
	 * the promise itself however if an error is thrown, this is propagated to
	 * the new generated promise.
	 */
	def static <R, T> effect(IPromise<R, T> promise, (T)=>void listener) {
		promise.effect [ r, it | listener.apply(it) ]
	}

	/**
	 * Perform some side-effect action based on the promise. It should not affect
	 * the promise itself however if an error is thrown, this is propagated to
	 * the new generated promise.
	 */
	def static <R, T> effect(IPromise<R, T> promise, (R, T)=>void listener) {
		promise.map [ r, it | listener.apply(r, it) return it ]
			=> [ operation = 'effect' ]
	}
	
	// ASYNC MAPPING //////////////////////////////////////////////////////////
	
	// Note: these are just aliases of flatmap, but used for nicer syntax and to indicate that the operations
	// may have sideeffects. Flatmap operations should not have sideeffects.

	/** 
	 * When the promise gives a result, call the function that returns another promise and 
	 * return that promise so you can chain and continue. Any thrown errors will be caught 
	 * and passed down the chain so you can catch them at the bottom.
	 * 
	 * Internally, this method calls flatMap. However you use this method call to indicate
	 * that the promiseFn will create sideeffects.
	 * <p>
	 * Example:
	 * <pre>
	 * loadUser
	 *   .thenAsync [ checkCredentialsAsync ]
	 *   .thenAsync [ signinUser ]
	 *   .onError [ setErrorMessage('could not sign you in') ]
	 *   .then [ println('success!') ]
	 * </pre>
	 */
	def static <R, T, M, P extends IPromise<R, M>> IPromise<R, M> call(IPromise<R, T> promise, (T)=>P promiseFn) {
		promise.map(promiseFn).resolve
			=> [ operation = 'call' ]
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	def static <R, T> onErrorThrow(IPromise<R, T> promise, (Throwable, T)=>Exception exceptionFn) {
		promise.onError [ t |
			throw switch t {
				PromiseException case t.value != null: exceptionFn.apply(t, t.value as T)
				default: exceptionFn.apply(t, null)
			}
		]
	}
	
	/** Convert or forward a promise to a task */	
	def static asTask(IPromise<?, ?> promise) {
		val task = new Task
		promise.completes(task)
		task
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <T> pipe(IPromise<?, T> promise, IPromise<T, ?> target) {
		promise.always [
			switch it {
				Value<?, T>: target.set(value)
				Error<?, T>: target.error(error)
			}
		]
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <R, T> completes(IPromise<R, T> promise, Task task) {
		promise
			.onError[ task.error(it) ]
			.then [ task.complete ]
	}

}
