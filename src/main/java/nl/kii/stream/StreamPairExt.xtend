package nl.kii.stream

import static extension nl.kii.stream.StreamExt.*

/**
 * These extensions let you pass state with a stream more easily. 
 * <p/>
 * In essence what it allows you to do is to work more easily with streams of pairs. 
 * When you can pass a pair, you can pass along some state with the value you are streaming.
 * <p/>
 * For example:
 * <pre>
 * // say we want to stream incoming messages from Vert.x:
 * val stream = Message.stream
 * vertx.eventBus.registerHandler('/test') [ it >> stream ]
 * 
 * // now that we have a message to stream, we'd like to keep
 * // a reference to the message down the stream, so we can reply
 * // to it... with pairs we can.
 * // Given some method processMessageAsync that takes a message and results
 * // in a promise:
 * stream.async [ it -> processMessageAsync(it) ].each [ msg, it | msg.reply(it) ]
 * </pre>
 */
class StreamPairExt {

	// CREATION ///////////////////////////////////////////////////////////////
	
	/** create a stream of pairs */
	def static <K, V> streamPair(Pair<Class<K>, Class<V>> type) {
		new Stream<Pair<K, V>>
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/**
	 * Perform mapping of a pair stream using a function that exposes the key and value of
	 * the incoming value.
	 */
	def static <K1, V1, V2> Stream<V2> map(Stream<Pair<K1, V1>> stream, (K1, V1)=>V2 mappingFn) {
		stream.map [ mappingFn.apply(key, value) ]
	}

	/**
	 * Maps a stream of pairs to a new stream, passing the key and value of the incoming
	 * stream as listener parameters.
	 */
	def static <V1, K2, V2> Stream<Pair<K2, V2>> mapToPair(Stream<V1> stream, (V1)=>Pair<K2, V2> mappingFn) {
		val newStream = new Stream<Pair<K2, V2>>
		stream
			.onError [ newStream.error(it) ]
			.forEach [
				val pair = mappingFn.apply(it)
				newStream.push(pair)
			]
		newStream	
	}
	
	/**
	 * Maps a stream of pairs to a new stream, passing the key and value of the incoming
	 * stream as listener parameters.
	 */
	def static <K1, V1, K2, V2> Stream<Pair<K2, V2>> mapToPair(Stream<Pair<K1,V1>> stream, (K1, V1)=>Pair<K2, V2> mappingFn) {
		val newStream = new Stream<Pair<K2, V2>>
		stream.then [
			val pair = mappingFn.apply(key, value)
			newStream.push(pair)
		]
		newStream
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <K, V> Stream<Pair<K, V>> filter(Stream<Pair<K, V>> stream, (K, V)=>boolean filterFn) {
		stream.filter [ filterFn.apply(key, value) ]
	}
	
	// PROMISE CHAINING ///////////////////////////////////////////////////////
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the promise result pair.
	 * See async2() for example of how to use.
	 */
	def static <K1, V1, V2> Stream<V2> async(Stream<Pair<K1, V1>> stream, (K1, V1)=>Promise<V2> promiseFn) {
		val newStream = new Stream<V2>(stream)
		stream.forEach [ 
			promiseFn.apply(key, value)
				.onError [ newStream.error(it) ]
				.then [ newStream.push(it) ]
		]
		stream.onError [ newStream.error(it) ]
		stream.onFinish [ newStream.finish ]
		newStream
	}
	
	/**
	 * Perform async chaining and allows for passing along a value.
	 * One of the problems with stream and promise programming is
	 * that in closures, you can pass a result along. In promises,
	 * you have no state in the lambda so you lose this information.
	 * This cannot be simulated with normal chaining:
	 * <pre>
	 * loadUsers()
	 *    .chain [ uploadUser ] 
	 *    .each [ showUploadResult(it, user) ] // error, no user known here
	 * </pre>
	 * However with chain2, you can pass along this extra user:
	 * <pre>
	 * loadUsers()
	 *    .async2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise 
	 *    .then2 [ user, result | showUploadResult(result, user) ] // you get back the user
	 */
	def static <V1, K2, V2> Stream<Pair<K2, V2>> asyncToPair(Stream<V1> stream, (V1)=>Pair<K2, Promise<V2>> promiseFn) {
		val newStream = new Stream<Pair<K2, V2>>(stream)
		stream.forEach [
			val pair = promiseFn.apply(it)
			pair.value
				.onError [ newStream.error(it) ]
				.then [ newStream.push(pair.key -> it) ]
		]
		stream.onError [ newStream.error(it) ]
		stream.onFinish [ newStream.finish ]
		newStream
	}

	/**
	 * Version of async2 that itself receives a pair as input. For multiple chaining:
	 * <pre>
	 * loadUsers()
	 *    .async2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise 
	 *    .async2 [ user, result | user -> showUploadResult(result, user) ] // you get back the user
	 *    .each [ user, result | println(result) ]
	 */	
	def static <K1, V1, K2, V2> Stream<Pair<K2, V2>> asyncToPair(Stream<Pair<K1, V1>> stream, (K1, V1)=>Pair<K2, Promise<V2>> promiseFn) {
		val newStream = new Stream<Pair<K2, V2>>(stream)
		stream.forEach [
			val pair = promiseFn.apply(key, value)
			pair.value
				.onError [ newStream.error(it) ]
				.then [ newStream.push(pair.key -> it) ]
		]
		stream.onError [ newStream.error(it) ]
		stream.onFinish [ newStream.finish ]
		newStream
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 */
	def static <K, V> void each(Stream<Pair<K, V>> stream, (K, V)=>void listener) {
		stream.forEach [ listener.apply(key, value) ]
	}

	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 * See async2() for example of how to use. This version is controlled: the listener gets passed
	 * the =stream and must indicate when it is ready for the next value. It also allows you to skip to
	 * the next finish.
	 */
	def static <K, V> void each(Stream<Pair<K, V>> stream, (K, V, Stream<Pair<K, V>>)=>void listener) {
		stream.forEach [ it, s | listener.apply(key, value, s) ]
	}	
	

}
