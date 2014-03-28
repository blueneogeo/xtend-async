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

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/**
	 * Perform mapping of a pair stream using a function that exposes the key and value of
	 * the incoming value.
	 */
	def static <T, P, R> Stream<R> map2(Stream<Pair<T, P>> stream, (T, P)=>R mappingFn) {
		stream.map [ mappingFn.apply(key, value) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <T, P> filter(Stream<Pair<T, P>> stream, (T, P)=>boolean filterFn) {
		stream.filter [ filterFn.apply(key, value) ]
	}
	
	// PROMISE CHAINING ///////////////////////////////////////////////////////
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the promise result pair.
	 * See async2() for example of how to use.
	 */
	def static <T, R, P> Stream<R> async(Stream<Pair<P, T>> stream, (P, T)=>Promise<R> promiseFn) {
		val newStream = new Stream<R>(stream)
		stream.each(false) [ it, done, s | promiseFn.apply(key, value).then [ newStream.push(it) ] ]
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
	def static <T, R, P> Stream<Pair<P, R>> async2(Stream<T> stream, (T)=>Pair<P, Promise<R>> promiseFn) {
		val newStream = new Stream<Pair<P, R>>(stream)
		stream.each(false) [ it, done, s |
			val pair = promiseFn.apply(it)
			pair.value.then [ newStream.push(pair.key -> it) ]
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
	def static <T, R, P1, P2> Stream<Pair<P2, R>> async2(Stream<Pair<P1,T>> stream, (P1, T)=>Pair<P2, Promise<R>> promiseFn) {
		val newStream = new Stream<Pair<P2, R>>(stream)
		stream.each(false) [ it, done, s |
			val pair = promiseFn.apply(key, value)
			pair.value.then [ newStream.push(pair.key -> it) ]
		]
		stream.onError [ newStream.error(it) ]
		stream.onFinish [ newStream.finish ]
		newStream
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 * See async2() for example of how to use.
	 */
	def static <T, P> void each(Stream<Pair<T, P>> stream, (T, P)=>void listener) {
		stream.each(true) [ it, done, s | listener.apply(key, value) ]
	}

	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 * See async2() for example of how to use. This version also gives you access to the done function
	 * so you can shortcut the stream, and the stream itself, so you can check it for statistics.
	 */
	def static <T, P> void each(Stream<Pair<T, P>> stream, (T, P, =>void, Stream<Pair<T, P>>)=>void listener) {
		stream.each(true) [ it, done, s | listener.apply(key, value, done, s) ]
	}	
	

}
