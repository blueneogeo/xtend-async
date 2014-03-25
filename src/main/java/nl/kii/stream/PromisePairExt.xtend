package nl.kii.stream

class PromisePairExt {
	
	/**
	 * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
	 * See chain2() for example of how to use.
	 */
	def static <T, P> then(Promise<Pair<T, P>> promise, (T, P)=>void listener) {
		promise.then [
			listener.apply(key, value)
		]
	}	
	
	/**
	 * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
	 * See chain2() for example of how to use.
	 */
	def static <T, R, P> Promise<R> async(Promise<Pair<P, T>> promise, (P, T)=>Promise<R> promiseFn) {
		val newPromise = new Promise<R>
		promise.then [
			promiseFn.apply(key, value).then [
				newPromise.apply(it)
			]
		]
		newPromise
	}
	
	/**
	 * Perform chaining and allows for passing along a value.
	 * One of the problems with stream and promise programming is
	 * that in closures, you can pass a result along. In promises,
	 * you have no state in the lambda so you lose this information.
	 * <p>
	 * Example with closures:
	 * <pre>
	 * loadUser(12) [ user |
	 *     uploadUser(user) [ result |
	 *         showUploadResult(result, user) // user from top closure is referenced
	 *     ]
	 * ]
	 * </pre>
	 * This cannot be simulated with normal chaining:
	 * <pre>
	 * loadUser(12)
	 *    .chain [ uploadUser ] 
	 *    .then [ showUploadResult(it, user) ] // error, no user known here
	 * </pre>
	 * However with chain2, you can pass along this extra user:
	 * <pre>
	 * loadUser(12)
	 *    .chain2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise 
	 *    .then2 [ user, result | showUploadResult(result, user) ] // you get back the user
	 */
	def static <T, R, P> Promise<Pair<P, R>> async2(Promise<T> promise, (T)=>Pair<P, Promise<R>> promiseFn) {
		val newPromise = new Promise<Pair<P, R>>
		promise.then [
			val pair = promiseFn.apply(it)
			pair.value.then [
				newPromise.apply(pair.key -> it)
			]
		]
		newPromise
	}

	/**
	 * Version of chain2 that itself receives a pair as input. For multiple chaining:
	 * <pre>
	 * loadUser(12)
	 *    .chain2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise 
	 *    .chain2 [ user, result | user -> showUploadResult(result, user) ] // you get back the user
	 *    .then [ user, result | println(result) ]
	 */	
	def static <T, R, P1, P2> Promise<Pair<P2, R>> async2(Promise<Pair<P1,T>> promise, (P1, T)=>Pair<P2, Promise<R>> promiseFn) {
		val newPromise = new Promise<Pair<P2, R>>
		promise.then [
			val pair = promiseFn.apply(key, value)
			pair.value.then [
				newPromise.apply(pair.key -> it)
			]
		]
		newPromise
	}

}