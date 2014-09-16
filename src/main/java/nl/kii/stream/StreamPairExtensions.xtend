package nl.kii.stream

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.promise.IPromise
import nl.kii.promise.Task
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class StreamPairExtensions {
	
	
	/** Create a stream of values out of a Promise of a list. If the promise throws an error,  */
	def static <K, T, T2 extends Iterable<T>> streamValue(IPromise<Pair<K, T2>> promise) {
		val newStream = new Stream<Pair<K, T>>
		promise
			.onError[ newStream.error(it) ]
			.then [	key, value |
				stream(value)
					.map [ key -> it ]
					.pipe(newStream)
			]
		newStream
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

	// RESOLVING //////////////////////////////////////////////////////////////
	
	/** 
	 * Resolves the value promise of the stream into just the value.
	 * Only a single promise will be resolved at once.
	 */
	def static <K, V, P extends IPromise<V>> Stream<Pair<K, V>> resolveValue(Stream<Pair<K, P>> stream) {
		stream.resolveValue(1) => [ stream.operation = 'resolveValue' ]
	}
	
	/** 
	 * Resolves the value promise of the stream into just the value.
	 * [concurrency] promises will be resolved at once (at the most).
	 */
	def static <K, V, P extends IPromise<V>> Stream<Pair<K, V>> resolveValue(Stream<Pair<K, P>> stream, int concurrency) {
		val newStream = new Stream<Pair<K, V>>
		val isFinished = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)
		stream.on [
			each [ result |
				val key = result.key
				val promise = result.value				
				processes.incrementAndGet
				promise
					.onError [
						processes.decrementAndGet
						newStream.error(it)
						if(isFinished.get) newStream.finish
					]
					.then [
						processes.decrementAndGet
						newStream.push(key -> it)
						if(isFinished.get) newStream.finish
					]
			]
			error [ newStream.error(it) false ]
			finish [ 
				if(processes.get == 0) {
					newStream.finish(it)
				} else {
					// we are still processing, so finish when we are done processing instead
					isFinished.set(true)
				}
			]
			closed [ newStream.close ]
		]
		newStream.monitor [
			next [ if(concurrency > processes.get) stream.next ]
			skip [ stream.skip ]
			close [ stream.close ]
		]
		stream.operation = 'resolveValue(concurrency=' + concurrency + ')'
		newStream
	}
	
	// CALL ////////////////////////////////////////////////////////////////

	// call for pair streams	

	/**
	 * Make an asynchronous call. Passes the key and value of the pair stream as separate parameters.
	 * This is an alias for stream.map(mappingFn).resolve
	 */	
	def static <K, T, R, P extends IPromise<R>> call(Stream<Pair<K, T>> stream, (K, T)=>P promiseFn) {
		stream.map(promiseFn).resolve
			 => [ stream.operation = 'call' ]
	}
	
	/**
	 * Make an asynchronous call. Passes the key and value of the pair stream as separate parameters.
	 * This is an alias for stream.map(mappingFn).resolve(concurrency)
	 */	
	def static <K, T, R, P extends IPromise<R>> call(Stream<Pair<K,T>> stream, int concurrency, (K, T)=>P promiseFn) {
		stream.map(promiseFn).resolve(concurrency)
			=> [ stream.operation = 'call(concurrency=' + concurrency + ')' ]
	}
	
	// call only the value of the pair
	
	def static <T, R, P extends IPromise<R>, K2> call2(Stream<T> stream, (T)=>Pair<K2, P> promiseFn) {
		stream.map(promiseFn).resolveValue
			=> [ stream.operation = 'call2' ]
	}
	
	def static <T, R, P extends IPromise<R>, K2> call2(Stream<T> stream, int concurrency, (T)=>Pair<K2, P> promiseFn) {
		stream.map(promiseFn).resolveValue(concurrency)
			=> [ stream.operation = 'call2(concurrency=' + concurrency + ')' ]
	}

	// call only the value of the pair, for pair streams
	
	def static <K, T, R, P extends IPromise<R>, K2> call2(Stream<Pair<K, T>> stream, (K, T)=>Pair<K2, P> promiseFn) {
		stream.map(promiseFn).resolveValue 
			=> [ stream.operation = 'call2' ]
	}
	
	def static <K, T, R, P extends IPromise<R>, K2> call2(Stream<Pair<K, T>> stream, int concurrency, (K, T)=>Pair<K2, P> promiseFn) {
		stream.map(promiseFn).resolveValue(concurrency) 
			=> [ stream.operation = 'call2(concurrency=' + concurrency + ')' ]
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////

	/**
	 * Handle errors on the stream.  This will swallow the error from the stream.
	 * It will attempt to get the key where it went wrong and pass it. If that
	 * fails the value is null.
	 * @return a new stream like the incoming stream but without the caught errors.
	 */
	def static <K, V> Stream<Pair<K, V>> onError(Stream<Pair<K, V>> stream, (K, Throwable)=>void handler) {
		val newStream = new Stream<Pair<K, V>>
		stream.on [
			each [ newStream.push(it) ]
			error [
				switch it {
					StreamException: try {
						val pair = value as Pair<K, ?>
						handler.apply(pair.key, it)
					} catch(ClassCastException e) {
						handler.apply(null, it)
					}
					default: {
						handler.apply(null, it)
						println('got error ' + message)
					}
				} 
				stream.next false
			]
			finish [ newStream.finish(it) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}
	/**
	 * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * Returns a task that completes once the stream finishes or closes.
	 */
	def static <K, V> Task onEach(Stream<Pair<K, V>> stream, (K, V)=>void listener) {
		stream.onEach [ listener.apply(key, value) ]
	}

	/**
	 * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * Performs the task for every value, and only requests the next value from the stream once the task has finished.
	 * Returns a task that completes once the stream finishes or closes.
	 */
	def static <K, V> Task onEachAsync(Stream<Pair<K, V>> stream, (K, V)=>Task taskFn) {
		stream.map(taskFn).resolve.onEach [
			// just ask for the next 
		] => [ stream.operation = 'onEach(async)' ]
	}

	/**
	 * Start the stream and and promise the first value from it.
	 */
	def static <K, V> then(Stream<Pair<K, V>> stream, Procedure2<K, V> listener) {
		stream.first.then [ listener.apply(key, value) ]
			=> [ stream.operation = 'then' ]
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////

	/** Perform some side-effect action based on the stream. */
	def static <K, T> effect(Stream<Pair<K, T>> stream, (K, T)=>void listener) {
		stream.map [
			listener.apply(key, value)
			it
		] => [ stream.operation = 'effect' ]
	}

	
}