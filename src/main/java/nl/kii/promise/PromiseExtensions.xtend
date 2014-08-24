package nl.kii.promise

import java.util.List
import java.util.Map
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

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
	
	/** Create a promise that immediately resolves to the passed value */
	def static <T> promise(T value) {
		new Promise<T>(value)
	}
	
//	@Async(false) def static onAny(Promise<?>[] promises, Task task) {
//		promises.forEach [
//			it.fork
//		]
//	}

	/** Create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Promise<Pair<K, V>>
	}

	/** Distribute work using an asynchronous method */	
	def static <T, R, P extends IPromise<R>> IPromise<List<R>> call(List<T> data, int concurrency, (T)=>P operationFn) {
		data.stream
			.map(operationFn) // put each of them
			.resolve(concurrency) // we get back a pair of the key->value used, and the done result
			.collect // see it as a list of results
			.first
	}
	
	// COMPLETING TASKS ///////////////////////////////////////////////////////
	
	/** Tell the promise it went wrong */
	def static <T> error(IPromise<T> promise, String message) {
		promise.error(new Exception(message))
	}

	/** Tell the promise it went wrong, with the cause throwable */
	def static <T> error(IPromise<T> promise, String message, Throwable cause) {
		promise.error(new Exception(message, cause))
	}

	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	def static <T> operator_doubleGreaterThan(T value, IPromise<T> promise) {
		promise.set(value)
		promise
	}
	
	/** Fulfill a promise */
	def static <T> operator_doubleLessThan(IPromise<T> promise, T value) {
		promise.set(value)
		promise
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////

	/** Convert a promise into a task */	
	def static <T> toTask(IPromise<T> promise) {
		val task = new Task
		promise.map[true].forwardTo(task)
		task
	}
	
	/** 
	 * Create a new promise from an existing promise, 
	 * that transforms the value of the promise
	 * once the existing promise is resolved.
	 */
	def static <T, R> map(IPromise<T> promise, (T)=>R mappingFn) {
		val newPromise = new Promise<R>(promise)
		promise.then [ newPromise.set(mappingFn.apply(it)) ]
		newPromise
	}
	
	/**
	 * Maps a promise of a pair to a new promise, passing the key and value of the incoming
	 * promise as listener parameters.
	 */
	def static <K1, V1, V2> map(IPromise<Pair<K1, V1>> promise, (K1, V1)=>V2 mappingFn) {
		promise.map [ 
			mappingFn.apply(key, value)
		]
	}
	
	/**
	 * Maps just the values of a promise of a pair to a new promise
	 */
	def static <K1, V1, V2> mapValue(IPromise<Pair<K1, V1>> promise, (V1)=>V2 mappingFn) {
		promise.map [ 
			key -> mappingFn.apply(value)
		]
	}

	/** Flattens a promise of a promise to directly a promise. */
	def static <R, P extends IPromise<R>> flatten(IPromise<P> promise) {
		promise.resolve
	}

	/** 
	 * Resolve a promise of a promise to directly a promise.
	 * Alias for Promise.flatten, added for consistent syntax with streams 
	 * */
	def static <R, P extends IPromise<R>> resolve(IPromise<P> promise) {
		val newPromise = new Promise<R>(promise)
		promise.then [
			onError [ newPromise.error(it) ] 
			.then [ newPromise.set(it) ]
		]
		newPromise
	}	

	/**
	 * Same as normal promise resolve, however this time for a pair of a key and a promise.
	 * Similar to Stream.resolveValue.
	 */
	def static <K, R, P extends IPromise<R>> resolveValue(IPromise<Pair<K, P>> promise) {
		val newPromise = new Promise<Pair<K, R>>(promise)
		promise.then [ pair |
			pair.value
				.onError [ newPromise.error(it) ] 
				.then [ newPromise.set(pair.key -> it) ]
		]
		newPromise
	}

	/** Performs a flatmap, which is a combination of map and flatten/resolve */	
	def static <T, R, P extends IPromise<R>> IPromise<R> flatMap(IPromise<T> promise, (T)=>P promiseFn) {
		promise.map(promiseFn).flatten
	}

	def static <T, R, K, P extends IPromise<R>> IPromise<R> flatMap(IPromise<Pair<K, T>> promise, (K, T)=>P promiseFn) {
		promise.map(promiseFn).flatten
	}
	
	// SIDEEFFECTS ////////////////////////////////////////////////////////////
	
	/** 
	 * Peek into what values going through the promise chain at this point.
	 * It is meant as a debugging tool for inspecting the data flowing
	 * through the promise.
	 * <p>
	 * The listener will not modify the promise and only get a view of the
	 * data passing by. It should never modify the passed reference!
	 * <p>
	 * If the listener throws an error, it will be caught and printed,
	 * and not interrupt the promise or throw an error on the promise.
	 */
	def static <T> peek(IPromise<T> promise, (T)=>void listener) {
		promise.map [
			try {
				listener.apply(it)
			} catch(Throwable t) {
				t.printStackTrace
			}
			it
		]
	}
	
	/**
	 * Perform some side-effect action based on the promise. It will not
	 * really affect the promise itself.
	 */
	def static <T> effect(IPromise<T> promise, (T)=>void listener) {
		promise.map [
			listener.apply(it)
			it
		]
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
	def static <T, R, P extends IPromise<R>> IPromise<R> call(IPromise<T> promise, (T)=>P promiseFn) {
		promise.map(promiseFn).resolve
	}

	def static <T, R, K, P extends IPromise<R>> IPromise<R> call(IPromise<Pair<K, T>> promise, (K, T)=>P promiseFn) {
		promise.map(promiseFn).resolve
	}

	def static <T, R, K, P extends IPromise<R>, K2> IPromise<Pair<K, R>> call2(IPromise<Pair<K, T>> promise, (K, T)=>Pair<K, P> promiseFn) {
		promise.map(promiseFn).resolveValue
	}

	def static <T, R, K, P extends IPromise<R>> IPromise<Pair<K, R>> call2(IPromise<T> promise, (T)=>Pair<K, P> promiseFn) {
		promise.map(promiseFn).resolveValue
	}

	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
	 * See chain2() for example of how to use.
	 */
	def static <K, V> then(IPromise<Pair<K, V>> promise, (K, V)=>void listener) {
		promise.then [ listener.apply(key, value) ]
	}
	
	/** 
	 * Fork a single promise into a list of promises
	 * Note that the original promise is then being listened to and you 
	 * can no longer perform .then and .onError on it.
	 */
	def static <T> fork(IPromise<T> promise, int amount) {
		val promises = newArrayOfSize(amount)
		promise
			.onError [ t | promises.forEach [ IPromise<T> p | p.error(t) ] ]
			.then [ value | promises.forEach [ IPromise<T> p | p.set(value) ] ]
		promises
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <T> forwardTo(IPromise<T> promise, IPromise<T> existingPromise) {
		promise
			.always [ existingPromise.apply(it) ]
			.then [ ] // starts listening
	}

	// BLOCKING ///////////////////////////////////////////////////////////////	
	
	/** 
	 * Convert a promise into a Future.
	 * Promises are non-blocking. However you can convert to a Future 
	 * if you must block and wait for a promise to resolve.
	 * <pre>
	 * val result = promise.future.get // blocks code until the promise is fulfilled
	 */
	def static <T> Future<T> future(IPromise<T> promise) {
		new PromiseFuture(promise)
	}

	// THREADED PROMISES //////////////////////////////////////////////////////

	/** 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	def static <T> IPromise<T> async(ExecutorService service, Callable<T> callable) {
		val promise = new Promise<T>
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
	def static Task run(ExecutorService service, Runnable runnable) {
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
	
}
