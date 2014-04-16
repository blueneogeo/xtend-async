package nl.kii.stream

import static extension nl.kii.stream.PromiseExt.*

class PromisePairExt {
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Promise<Pair<K, V>>
	}
	
	// TRANSFORMATIONS /////////////////////////////////////////////////////////
	
	/**
	 * Maps a promise of a pair to a new promise, passing the key and value of the incoming
	 * promise as listener parameters.
	 */
	def static <K1, V1, V2> map(Promise<Pair<K1, V1>> promise, (K1, V1)=>V2 mappingFn) {
		promise.map [ 
			mappingFn.apply(key, value)
		]
	}

	/**
	 * Maps a promise of a pair to a new promise, passing the key and value of the incoming
	 * promise as listener parameters.
	 */
	def static <V1, K2, V2> Promise<Pair<K2, V2>> mapToPair(Promise<V1> promise, (V1)=>Pair<K2, V2> mappingFn) {
		val newPromise = new Promise<Pair<K2, V2>>(promise)
		promise.then [
			val pair = mappingFn.apply(it)
			newPromise.set(pair)
		]
		newPromise	
	}
	
	/**
	 * Maps a promise of a pair to a new promise, passing the key and value of the incoming
	 * promise as listener parameters.
	 */
	def static <K1, V1, K2, V2> Promise<Pair<K2, V2>> mapToPair(Promise<Pair<K1,V1>> promise, (K1, V1)=>Pair<K2, V2> mappingFn) {
		val newPromise = new Promise<Pair<K2, V2>>(promise)
		promise.then [
			val pair = mappingFn.apply(key, value)
			newPromise.set(pair)
		]
		newPromise	
	}

	// ASYNC //////////////////////////////////////////////////////////////////
		
	/**
	 * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
	 * See chain2() for example of how to use.
	 */
	def static <K1, V1, V2> Promise<V2> async(Promise<Pair<K1, V1>> promise, (K1, V1)=>Promise<V2> promiseFn) {
		val newPromise = new Promise<V2>(promise)
		promise.then [
			promiseFn.apply(key, value)
				.onError [ newPromise.error(it) ]
				.then [ newPromise.set(it) ]
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
	def static <V1, K2, V2> Promise<Pair<K2, V2>> asyncToPair(Promise<V1> promise, (V1)=>Pair<K2, Promise<V2>> promiseFn) {
		val newPromise = new Promise<Pair<K2, V2>>(promise)
		promise.then [
			val pair = promiseFn.apply(it)
			pair.value
				.onError [ newPromise.error(it) ]
				.then [ newPromise.set(pair.key -> it) ]
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
	def static <K1, V1, K2, V2> Promise<Pair<K2, V2>> asyncToPair(Promise<Pair<K1, V1>> promise, (K1, V1)=>Pair<K2, Promise<V2>> promiseFn) {
		val newPromise = new Promise<Pair<K2, V2>>(promise)
		promise.then [
			val pair = promiseFn.apply(key, value)
			pair.value
				.onError [ newPromise.error(it) ]
				.then [ newPromise.set(pair.key -> it) ]
		]
		newPromise
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
	 * See chain2() for example of how to use.
	 */
	def static <K, V> then(Promise<Pair<K, V>> promise, (K, V)=>void listener) {
		promise.then [ listener.apply(key, value) ]
	}
	
}
