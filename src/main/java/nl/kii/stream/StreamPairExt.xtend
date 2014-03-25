package nl.kii.stream

class StreamPairExt {
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 * See async2() for example of how to use.
	 */
	def static <T, P> each(Stream<Pair<T, P>> stream, (T, P)=>void listener) {
		stream.each(true) [ it, done | listener.apply(key, value) ]
	}	
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the promise result pair.
	 * See async2() for example of how to use.
	 */
	def static <T, R, P> Stream<R> async(Stream<Pair<P, T>> stream, (P, T)=>Promise<R> promiseFn) {
		val newStream = new Stream<R>(stream)
		stream.each(false) [ it, done | promiseFn.apply(key, value).then [ newStream.push(it) ] ]
		stream.onFinish [ newStream.finish ]
		newStream
	}
	
	/**
	 * Perform chaining and allows for passing along a value.
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
		stream.each(false) [ it, done |
			val pair = promiseFn.apply(it)
			pair.value.then [ newStream.push(pair.key -> it) ]
		]
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
		stream.each(false) [ it, done |
			val pair = promiseFn.apply(key, value)
			pair.value.then [ newStream.push(pair.key -> it) ]
		]
		stream.onFinish [ newStream.finish ]
		newStream
	}

}
