package nl.kii.stream

import java.util.List
import java.util.Map
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

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
	
	// CREATING PROMISE AND TASK FUNCTIONS ////////////////////////////////////

	def static <T> promise(Class<T> type, (Promise<T>)=>void blockThatFulfillsPromise) {
		val promise = type.promise
		try {
			blockThatFulfillsPromise.apply(promise)			
		} catch(Throwable t) {
			promise.error(t)
		}
		promise
	}

	def static <T> promiseList(Class<T> type, (Promise<List<T>>)=>void blockThatFulfillsPromise) {
		val promise = type.promiseList
		try {
			blockThatFulfillsPromise.apply(promise)			
		} catch(Throwable t) {
			promise.error(t)
		}
		promise
	}

	def static <K, V> promiseMap(Pair<Class<K>, Class<V>> type, (Promise<Map<K, V>>)=>void blockThatFulfillsPromise) {
		val promise = type.promiseMap
		try {
			blockThatFulfillsPromise.apply(promise)			
		} catch(Throwable t) {
			promise.error(t)
		}
		promise
	}
	
	def static task((Task)=>void blockThatPerformsTask) {
		val task = new Task
		try {
			blockThatPerformsTask.apply(task)			
		} catch(Throwable t) {
			task.error(t)
		}
		task
	}
	
	// COMPLETING TASKS ///////////////////////////////////////////////////////
	
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
		task [ task |
			val Runnable processor = [|
				try {
					runnable.run
					task.complete
				} catch(Throwable t) {
					task.error(t)
				}
			]
			service.submit(processor)
		]
	}	
	
}
