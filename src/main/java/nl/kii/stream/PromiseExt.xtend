package nl.kii.stream

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

import static java.util.concurrent.Executors.*

class PromiseExt {
	
	// CREATING PROMISES //////////////////////////////////////////////////////
	
	/** Create a promise of the given type */
	def static <T> promise(Class<T> type) {
		new Promise<T>
	}
	
	/** Create a promise that immediately resolves to the passed value */
	def static <T> promise(T value) {
		new Promise<T>(value)
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
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Perform an async operation which returns a promise.
	 * This allows you to chain multiple async methods, as
	 * long as you let your closures return a Promise
	 * <p>
	 * Example:
	 * <pre>
	 * def Promise<User> loadUser(int userId)
	 * def Promise<Result> uploadUser(User user)
	 * def void showUploadResult(Result result)
	 * 
	 * loadUser(12)
	 *    .async [ uploadUser ] 
	 *    .then [ showUploadResult ]
	 * </pre>
	 */
	def static <T, R> Promise<R> mapAsync(Promise<T> promise, (T)=>Promise<R> promiseFn) {
		promise.map(promiseFn).flatten
//		
//		
//		val newPromise = new Promise<R>(promise)
//		promise.then [
//			promiseFn.apply(it)
//				.onError [ newPromise.error(it) ]
//				.then [ newPromise.set(it) ]
//		]
//		newPromise
	}
	
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
	 * Easily run a procedure in the background and return a promise
	 * promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	def static promise(=>void procedure) {
		promise([| procedure.apply ] as Runnable)
	}

	/** 
	 * Easily run a function in the background and return a promise
	 * <pre>
	 * promise [| doSomeHeavyLifting ].then [ println('done!') ]
	 */
	def static <T> promise(=>T function) {
		promise([| function.apply ] as Callable<T>)
	}

	/** Execute the callable in the background and return as a promise */
	def static <T> Promise<T> promise(Callable<T> callable) {
		promise(newSingleThreadExecutor, callable)
	}

	/** Execute the runnable in the background and return as a promise */
	def static Promise<Void> promise(Runnable runnable) {
		promise(newSingleThreadExecutor, runnable)
	}

	/** 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	def static <T> Promise<T> promise(ExecutorService service, Callable<T> callable) {
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
	def static Promise<Void> promise(ExecutorService service, Runnable runnable) {
		val promise = new Promise<Void>
		val Runnable processor = [|
			try {
				runnable.run
				promise.set(null)
			} catch(Throwable t) {
				promise.error(t)
			}
		]
		service.submit(processor)
		promise
	}	
	
}
