package nl.kii.stream

import static extension nl.kii.stream.PromiseExtensions.*

class PromisePairExtensions {
	
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
	 * Same as normal promise resolve, however this time for a pair of a key and a promise.
	 * @see PromiseExtensions.resolve()
	 */
	def static <K, V> resolvePair(Promise<Pair<K, Promise<V>>> promise) {
		val newPromise = new Promise<Pair<K, V>>(promise)
		promise.then [ pair |
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
