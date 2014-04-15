package nl.kii.stream

class PromiseExt {
	
	/** create a stream of the given type */
	def static <T> promise(Class<T> type) {
		new Promise<T>
	}
	
	def static <T> promise(T value) {
		new Promise<T>(value)
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	def static <T> operator_doubleGreaterThan(T value, Promise<T> promise) {
		promise.apply(value)
		promise
	}
	
	def static <T> operator_doubleLessThan(Promise<T> promise, T value) {
		promise.apply(value)
		promise
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	def static <T, R> map(Promise<T> promise, (T)=>R mappingFn) {
		val newPromise = new Promise<R>
		promise.then [ newPromise.apply(mappingFn.apply(it)) ]
		newPromise
	}
	
	def static <T> flatten(Promise<Promise<T>> promise) {
		val newPromise = new Promise<T>
		promise.then [ then [ newPromise.apply(it) ]]
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
		val newPromise = new Promise<R>
		promise.then [
			promiseFn.apply(it).then [
				newPromise.apply(it)
			]
		]
		newPromise
	}
	
}