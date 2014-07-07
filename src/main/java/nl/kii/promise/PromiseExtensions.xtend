package nl.kii.promise

import java.util.List
import java.util.Map
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.eclipse.xtext.xbase.lib.Functions.Function1

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
	
	// COMPLETING TASKS ///////////////////////////////////////////////////////
	
	/** When the promise gives a result, call the function that returns another promise and 
	 * return that promise so you can chain and continue. Any thrown errors will be caught 
	 * and passed down the chain so you can catch them at the bottom.
	 * <p>
	 * Example:
	 * <pre>
	 * loadUser
	 *   .then [ checkCredentialsAsync ]
	 *   .then [ signinUser ]
	 *   .onError [ setErrorMessage('could not sign you in') ]
	 *   .then [ println('success!') ]
	 * </pre>
	 */
	def static <T, P> Promise<P> then(Promise<T> promise, Function1<T, Promise<P>> promiseFn) {
		val p = new Promise<P>
		promise
			.onError[ 
				p.error(it)
			]
			.then [
				try {
					val returnedPromise = promiseFn.apply(it)
					returnedPromise
						.onError [ 
							p.error(it)
						]
						.then [ 
							p.set(it)
						]
				} catch(Throwable t) {
					// println(t)
					p.error(t)
				}
			]
		p
	}
	
	/** Tell the task it went wrong */
	def static error(Task task, String message) {
		task.error(new Exception(message)) as Task
	}

	/** Tell the promise it went wrong */
	def static <T> error(Promise<T> promise, String message) {
		promise.error(new Exception(message))
	}

	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	def static <T> operator_doubleGreaterThan(T value, Promise<T> promise) {
		promise.set(value)
		promise
	}
	
	/** Fulfill a promise */
	def static <T> operator_doubleLessThan(Promise<T> promise, T value) {
		promise.set(value)
		promise
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/** 
	 * Create a new promise from an existing promise, 
	 * that transforms the value of the promise
	 * once the existing promise is resolved.
	 */
	def static <T, R> map(Promise<T> promise, (T)=>R mappingFn) {
		val newPromise = new Promise<R>(promise)
		promise.then [ newPromise.set(mappingFn.apply(it)) ]
		newPromise
	}
	
	/** Flattens a promise of a promise to directly a promise. */
	def static <T> flatten(Promise<Promise<T>> promise) {
		val newPromise = new Promise<T>(promise)
		promise.then [
			onError [ newPromise.error(it) ] 
			.then [ newPromise.set(it) ]
		]
		newPromise
	}
	
	/** Alias for flatten, turns a promise of a promise into a promise */
	def static <T> resolve(Promise<Promise<T>> promise) {
		promise.flatten
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/** Create a new promise that listenes to this promise */
	def static <T> fork(Promise<T> promise) {
		promise.map[it]
	}	

	// BLOCKING ///////////////////////////////////////////////////////////////	
	
	/** 
	 * Convert a promise into a Future.
	 * Promises are non-blocking. However you can convert to a Future 
	 * if you must block and wait for a promise to resolve.
	 * <pre>
	 * val result = promise.future.get // blocks code until the promise is fulfilled
	 */
	def static <T> Future<T> future(Promise<T> promise) {
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
	def static <T> Promise<T> async(ExecutorService service, Callable<T> callable) {
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
