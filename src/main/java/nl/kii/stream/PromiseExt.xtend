package nl.kii.stream

import java.util.concurrent.Future

class PromiseExt {
	
	def static <T> promise(Class<T> type) {
		new Promise<T>
	}
	
	def static <T> promise(T value) {
		new Promise<T>(value)
	}

	def static <T> Future<T> future(Promise<T> promise) {
		new PromiseFuture(promise)
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	def static <T> operator_doubleGreaterThan(T value, Promise<T> promise) {
		promise.set(value)
		promise
	}
	
	def static <T> operator_doubleLessThan(Promise<T> promise, T value) {
		promise.set(value)
		promise
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	def static <T, R> map(Promise<T> promise, (T)=>R mappingFn) {
		val newPromise = new Promise<R>(promise)
		promise.then [ newPromise.set(mappingFn.apply(it)) ]
		newPromise
	}
	
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
	 *    .chain [ uploadUser ] 
	 *    .then [ showUploadResult ]
	 * </pre>
	 */
	def static <T, R> Promise<R> async(Promise<T> promise, (T)=>Promise<R> promiseFn) {
		val newPromise = new Promise<R>(promise)
		promise.then [
			promiseFn.apply(it)
				.onError [ newPromise.error(it) ]
				.then [ newPromise.set(it) ]
		]
		newPromise
	}
	
	/** 
	 * Create a new promise that listenes to this promise
	 */
	def static <T> fork(Promise<T> promise) {
		promise.map[it]
	}	
	
}
