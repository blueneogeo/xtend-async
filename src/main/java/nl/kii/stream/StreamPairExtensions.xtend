package nl.kii.stream

import static extension nl.kii.stream.StreamExtensions.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
class StreamPairExtensions {

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
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <K, V> Stream<Pair<K, V>> filter(Stream<Pair<K, V>> stream, (K, V)=>boolean filterFn) {
		stream.filter [ filterFn.apply(key, value) ]
	}
	
	def static <K, V> Stream<Pair<K, V>> resolvePair(Stream<Pair<K, Promise<V>>> stream) {
		stream.resolvePair(1)
	}
	
	def static <K, V> Stream<Pair<K, V>> resolvePair(Stream<Pair<K, Promise<V>>> stream, int concurrency) {
		val newStream = new Stream<Pair<K, V>>
		val isFinished = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)
		val =>void onProcessComplete = [|
			val open = processes.decrementAndGet
			if(isFinished.get) {
				newStream.finish
			}
			if(concurrency > open) {
				stream.next
			}
		]
		stream.onAsync [
			each [ result |
				val key = result.key
				val promise = result.value
				processes.incrementAndGet
				promise
					.onError [
						newStream.error(it)
						stream.next
					]
					.then [
						newStream.push(key -> it)
						onProcessComplete.apply 
					]
			]
			error [
				newStream.error(it)
				stream.next
			]
			finish [ 
				if(processes.get == 0) {
					newStream.finish(level)
					stream.next
				} else {
					// we are still processing, so finish when we are done processing instead
					isFinished.set(true)
				}
			]
		]
		stream.next
		newStream
	}
		
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 */
	def static <K, V> void onEach(Stream<Pair<K, V>> stream, (K, V)=>void listener) {
		stream.on [ each [ listener.apply(key, value) ] ]
	}

	/**
	 * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
	 * See async2() for example of how to use. This version is controlled: the listener gets passed
	 * the =stream and must indicate when it is ready for the next value. It also allows you to skip to
	 * the next finish.
	 */
	def static <K, V> void onEach(Stream<Pair<K, V>> stream, (K, V, Stream<Pair<K, V>>)=>void listener) {
		stream.on [ each [ it | listener.apply(key, value, stream) ] ]
	}

}
