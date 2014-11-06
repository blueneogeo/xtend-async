package nl.kii.promise

import java.util.List
import java.util.Map
import nl.kii.stream.Stream
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

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

	def static <I, O> promise(I from, O value) {
		new SubPromise<I, O> => [ set(from, value) ]
	}
	
	/** Create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Promise<Pair<K, V>>
	}

	/** Distribute work using an asynchronous method */	
	def static <I, I2, O, P extends IPromise<I2, O>> IPromise<I, List<O>> call(List<I> data, int concurrency, (I)=>P operationFn) {
		data.stream
			.call(concurrency, operationFn)
			.collect // see it as a list of results
			.first
			=> [ operation = 'call(concurrency=' + concurrency + ')' ]
	}

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
	def static <I, O, P extends IPromise<I, O>> Task any(P... promises) {
		any(promises.toList)
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <I, O> Task any(List<? extends IPromise<I, O>> promises) {
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
	def static <I, O> always(IPromise<I, O> promise, Procedures.Procedure1<Entry<?, O>> resultFn) {
		promise.onError [ resultFn.apply(new Error(null, it)) ]
		promise.then [ resultFn.apply(new Value(null, it)) ]
		promise
	}
	
	/** Tell the promise it went wrong */
	def static <I, O> error(IPromise<I, O> promise, String message) {
		promise.error(new Exception(message))
	}

	/** Tell the promise it went wrong, with the cause throwable */
	def static <I, O> error(IPromise<I, O> promise, String message, Throwable cause) {
		promise.error(new Exception(message, cause))
	}

	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	def static <I, O> >> (I value, IPromise<I, O> promise) {
		promise.set(value)
		promise
	}
	
	/** Fulfill a promise */
	def static <I, O> << (IPromise<I, O> promise, I value) {
		promise.set(value)
		promise
	}
	
	/** All/And */
	def static Task && (IPromise<?, ?> p1, IPromise<?, ?> p2) {
		all(p1, p2)
	}
	
	/** Any/Or */
	def static <I, O> Task || (IPromise<I, O> p1, IPromise<I, O> p2) {
		any(p1, p2)
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////

	/** 
	 * Create a new promise from an existing promise, 
	 * that transforms the value of the promise
	 * once the existing promise is resolved.
	 */
	def static <I, O, R> map(IPromise<I, O> promise, (O)=>R mappingFn) {
		promise.map [ r, it | mappingFn.apply(it) ]
	}

	/** 
	 * Create a new promise from an existing promise, 
	 * that transforms the value of the promise
	 * once the existing promise is resolved.
	 */
	def static <I, O, R> map(IPromise<I, O> promise, (I, O)=>R mappingFn) {
		val newPromise = new SubPromise<I, R>(promise)
		promise
			.onError [ r, it | newPromise.error(r, it) ]
			.then [ r, it | newPromise.set(r, mappingFn.apply(r, it)) ]
		newPromise => [ operation = 'map' ]
	}
	
	/**
	 * Create a new promise with a new input, defined by the inputFn
	 */
	def static <I1, I2, O> mapInput(IPromise<I1, O> promise, (I1, O)=>I2 inputFn) {
		val subPromise = new SubPromise<I2, O>(new Promise<I2>)
		promise
			.onError [ r, it | subPromise.error(inputFn.apply(r, null), it) ]
			.then [ r, it | subPromise.set(inputFn.apply(r, it), it) ]
		subPromise => [ operation = 'root' ]
	}
	
	/**
	 * Maps errors back into values. 
	 * Good for alternative path resolving and providing defaults.
	 */
	def static <I, O> onErrorMap(IPromise<I, O> promise, (Throwable)=>O mappingFn) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.onError [ i, it |
				try {
					newPromise.set(i, mappingFn.apply(it))
				} catch(Exception e) {
					newPromise.error(e)
				}
			]
			.then [ i, it | newPromise.set(i, it) ]
		newPromise => [ operation = 'onErrorMap' ]
	}

	/**
	 * Maps errors back into values, using an async call. 
	 * Good for alternative path resolving and providing defaults.
	 */
	def static <I, I2, O> onErrorCall(IPromise<I, O> promise, (Throwable)=>IPromise<I2, O> mappingFn) {
		val newPromise = new SubPromise<I, O>(new Promise<I>)
		promise
			.onError [ i, it |
				try {
					mappingFn.apply(it)
						.onError [ newPromise.error(i, it) ]
						.then [ newPromise.set(i, it) ]
				} catch(Exception e) {
					newPromise.error(e)
				}
			]
			.then [ i, it | newPromise.set(i, it) ]
		newPromise => [ operation = 'onErrorCall' ]
	}

	/** Flattens a promise of a promise to directly a promise. */
	def static <I1, I2, O, P extends IPromise<I1, O>> flatten(IPromise<I2, P> promise) {
		promise.resolve => [ operation = 'flatten' ]
	}

	/** Create a stream out of a promise of a stream. */
	def static <I, P extends IPromise<I, Stream<T>>, T> toStream(P promise) {
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
	def static <I, O, P extends IPromise<?, O>> resolve(IPromise<I, P> promise) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.onError [ r, it | newPromise.error(r, it) ]
			.then [ r, p |
				p
					.onError [ newPromise.error(r, it) ] 
					.then [ newPromise.set(r, it) ]
			]
		newPromise => [ operation = 'resolve' ]
	}

	/** Performs a flatmap, which is a combination of map and flatten/resolve */	
	def static <I, O, R, P extends IPromise<I, R>> IPromise<I, R> flatMap(IPromise<I, O> promise, (O)=>P promiseFn) {
		promise.map(promiseFn).flatten
			=> [ operation = 'flatMap' ]
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////
	
	/**
	 * Perform some side-effect action based on the promise. It should not affect
	 * the promise itself however if an error is thrown, this is propagated to
	 * the new generated promise.
	 */
	def static <I, O> effect(IPromise<I, O> promise, (O)=>void listener) {
		promise.effect [ r, it | listener.apply(it) ]
	}

	/**
	 * Perform some side-effect action based on the promise. It should not affect
	 * the promise itself however if an error is thrown, this is propagated to
	 * the new generated promise.
	 */
	def static <I, O> effect(IPromise<I, O> promise, (I, O)=>void listener) {
		promise.map [ r, it | listener.apply(r, it) return it ]
			=> [ operation = 'effect' ]
	}
	
	/**
	 * Asynchronously perform some side-effect action based on the promise. It should not affect
	 * the promise itself however if an error is thrown, this is propagated to
	 * the new generated promise.
	 */
	def static <I, O> perform(IPromise<I, O> promise, (I, O)=>IPromise<?,?> promiseFn) {
		promise.map[ i, o | promiseFn.apply(i, o).map[o] ].resolve
			=> [ operation = 'perform' ]
	}
	
	/**
	 * Asynchronously perform some side-effect action based on the promise. It should not affect
	 * the promise itself however if an error is thrown, this is propagated to
	 * the new generated promise.
	 */
	def static <I, O> perform(IPromise<I, O> promise, (O)=>IPromise<?,?> promiseFn) {
		promise.perform [ i, o | promiseFn.apply(o) ]
	}
	
	// ASYNC MAPPING //////////////////////////////////////////////////////////
	
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
	 *   .call [ checkCredentialsAsync ]
	 *   .call [ signinUser ]
	 *   .onError [ setErrorMessage('could not sign you in') ]
	 *   .then [ println('success!') ]
	 * </pre>
	 */
	def static <I, O, R, P extends IPromise<?, R>> call(IPromise<I, O> promise, (O)=>P promiseFn) {
		promise.map(promiseFn).resolve
			=> [ operation = 'call' ]
	}
	
	// TIMING /////////////////////////////////////////////////////////////////

	/** Create a new promise that delays the output (not the error) of the existing promise */	
	def static <I, O> wait(IPromise<I, O> promise, long periodMs, (long, =>void)=>void timerFn) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.onError [ newPromise.error(it) ]
			.then [ input, value |
				timerFn.apply(periodMs) [
					newPromise.set(input, value)
				]
			]
		newPromise
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	def static <I, O> onErrorThrow(IPromise<I, O> promise, (I, Throwable)=>Exception exceptionFn) {
		promise.onError [ i, t | throw exceptionFn.apply(i, t) ]
	}

	def static <I, O> onErrorThrow(IPromise<I, O> promise, String message) {
		promise.onError [ i, t | throw new Exception(message + ', for input ' + i, t) ]
	}
	
	/** Convert or forward a promise to a task */	
	def static <I, O> asTask(IPromise<I, O> promise) {
		val task = new Task
		promise.completes(task)
		task
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, O, O2> pipe(IPromise<I, O> promise, IPromise<O, O2> target) {
		promise
			.onError [ target.error(it) ]
			.then [ target.set(it) ]
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, I2, O> completes(IPromise<I, O> promise, Task task) {
		promise
			.onError [ r, it | task.error(it) ]
			.then [ r, it | task.set(true) ]
			.onError [ r, it | task.error(it) ]
	}

}
