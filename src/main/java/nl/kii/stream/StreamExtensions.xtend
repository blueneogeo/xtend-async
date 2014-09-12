package nl.kii.stream

import com.google.common.collect.ImmutableList
import com.google.common.io.ByteProcessor
import com.google.common.io.Files
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Async
import nl.kii.observe.Observable
import nl.kii.observe.Publisher
import nl.kii.promise.IPromise
import nl.kii.promise.Promise
import nl.kii.promise.Task
import nl.kii.stream.source.LoadBalancer
import nl.kii.stream.source.StreamCopySplitter
import nl.kii.stream.source.StreamSource
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static extension com.google.common.io.ByteStreams.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class StreamExtensions {
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** Create a stream of the given type */
	def static <T> stream(Class<T> type) {
		new Stream<T>
	}
	
	/** 
	 * Create a stream of a set of data and finish it.
	 * Note: the reason this method is called datastream instead of stream, is that
	 * the type binds to anything, even void. That means that datastream() becomes a valid expression
	 * which is errorprone.
	 */
	def static <T> datastream(T... data) {
		data.iterator.stream
	}

	/** stream the data of a map as a list of key->value pairs */
	def static <K, V> stream(Map<K, V> data) {
		data.entrySet.map [ key -> value ].stream
	}

	/** Create a stream of values out of a Promise of a list. If the promise throws an error,  */
	def static <T, T2 extends Iterable<T>> stream(IPromise<T2> promise) {
		val newStream = new Stream<T>
		promise
			.onError[ newStream.error(it) ]
			.then [	stream(it).pipe(newStream) ]
		newStream
	}

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

	/** stream an list, ending with a finish. makes an immutable copy internally. */	
	def static <T> stream(List<T> list) {
		ImmutableList.copyOf(list).iterator.stream
	}


	/** stream an interable, ending with a finish */	
	def static <T> stream(Iterable<T> iterable) {
		iterable.iterator.stream
	}
	
	/** stream an iterable, ending with a finish */
	def static <T> stream(Iterator<T> iterator) {
		val finished = new AtomicBoolean(false)
		val stream = new Stream<T>
		val =>void pushNext = [|
			if(finished.get) return;
			if(iterator.hasNext) {
				iterator.next >> stream
			} else {
				finished.set(true)
				stream.finish
			}
		]
		stream.monitor [
			next [ pushNext.apply ]
			skip [ finished.set(true) stream.finish ]
		]
		stream.operation = 'iterate'
		pushNext.apply
		stream
	}
	
	/** stream a standard Java inputstream. closing the stream closes the inputstream. */
	def static Stream<List<Byte>> stream(InputStream stream) {
		val newStream = new Stream<List<Byte>>
		stream.readBytes(new ByteProcessor {
			
			override getResult() { newStream.finish null }
			
			override processBytes(byte[] buf, int off, int len) throws IOException {
				if(!newStream.open) return false
				newStream.push(buf)
				true
			}
			
		})
		newStream.monitor [
			skip [ stream.close]
			close [ stream.close ]
		]
		newStream
	}

	/** stream a file as byte blocks. closing the stream closes the file. */	
	def static Stream<List<Byte>> stream(File file) {
		val source = Files.asByteSource(file)
		source.openBufferedStream.stream
	}
	
	/** create an unending stream of random integers in the range you have given */
	def static Stream<Integer> streamRandom(IntegerRange range) {
		val randomizer = new Random
		val newStream = int.stream
		newStream.monitor [
			next [
				if(newStream.open) {
					val next = range.start + randomizer.nextInt(range.size)
					newStream.push(next)
				}
			]
			skip [ newStream.close ]
			close [ newStream.close ]
		]
		newStream
	}

	// OBSERVING //////////////////////////////////////////////////////////////

	def static <T> observe(Stream<T> stream, StreamObserver<T> observer) {
		stream.onChange [ entry |
			
		]
	}

	/** 
	 * Create a publisher for the stream. This allows you to observe the stream
	 * with multiple listeners. Publishers do not support flow control, and the
	 * created Publisher will eagerly pull all data from the stream for publishing.
	 */
	def static <T> Publisher<T> publish(Stream<T> stream) {
		val publisher = new Publisher<T>
		stream.on [
			each [
				publisher.apply(it)
				if(publisher.publishing) stream.next
			]
		]
		stream.next
		stream.operation = 'publish'
		publisher
	}

	/** 
	 * Create new streams from an observable. Notice that these streams are
	 * being pushed only, you lose flow control. Closing the stream will also
	 * unsubscribe from the observable.
	 */
	def static <T> Stream<T> stream(Observable<T> observable) {
		val newStream = new Stream<T>
		val stopObserving = observable.onChange [
			newStream.push(it)
		]
		newStream.monitor [
			close [ stopObserving.apply ]
		]
		newStream
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Add a value to a stream */
	def static <T> >> (T value, Stream<T> stream) {
		stream.push(value)
		stream
	}
	
	/** Add a value to a stream */
	def static <T> << (Stream<T> stream, T value) {
		stream.push(value)
		stream
	}

	/** Add a list of values to a stream */
	def static <T> >> (List<T> value, Stream<T> stream) {
		value.forEach [ stream.push(it) ]
		stream
	}
	
	/** Add a list of values to a stream */
	def static <T> << (Stream<T> stream, List<T> value) {
		value.forEach [ stream.push(it) ]
		stream
	}

	/** Add an entry to a stream (such as error or finish) */
	def static <T> << (Stream<T> stream, Entry<T> entry) {
		stream.apply(entry)
		stream
	}

	/** Lets you easily pass an Error<T> to the stream using the << operator */
	def static <T> << (Stream<T> stream, Throwable t) {
		stream.apply(new Error<T>(t))
		stream
	}

	/** Lets you easily pass an Error<T> to the stream using the >> operator */
	def static <T> >> (Throwable t, Stream<T> stream) {
		stream.apply(new Error<T>(t))
		stream
	}

	/** Lets you easily pass a Finish<T> entry using the << or >> operators */
	def static <T> finish() {
		new Finish<T>(0)
	}

	def static <T> finish(int level ) {
		new Finish<T>(level)
	}

	def static <T, R> controls(Stream<T> newStream, Stream<?> parent) {
		newStream.monitor [
			next [ parent.next ]
			skip [ parent.skip ]
			close [ parent.close ]
		]		
	}
	
	/** Tell the stream something went wrong */
	def static <T> error(Stream<T> stream, String message) {
		stream.error(new Exception(message))
	}

	/** Tell the stream something went wrong, with the cause throwable */
	def static <T> error(Stream<T> stream, String message, Throwable cause) {
		stream.error(new Exception(message, cause))
	}
	
	/** Tell the stream something went wrong, with the cause throwable */
	def static <T> error(Stream<T> stream, String message, T value, Throwable cause) {
		stream.error(new StreamException(message, value, cause))
	}

	/** Tell the stream something went wrong, with the cause throwable */
	def static <T> error(Stream<T> stream, String message, T value) {
		stream.error(new StreamException(message, value, null))
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/**
	 * Transform each item in the stream using the passed mappingFn
	 */
	def static <T, R> Stream<R> map(Stream<T> stream, (T)=>R mappingFn) {
		val newStream = new Stream<R>
		stream.on [
			each [
				val mapped = mappingFn.apply(it)
				newStream.push(mapped)
			]
			error [	newStream.error(it)	false ]
			finish [ newStream.finish(it) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		stream.operation = 'map'
		newStream
	}
	
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
	def static <T> filter(Stream<T> stream, (T)=>boolean filterFn) {
		stream.filter [ it, index, passed | filterFn.apply(it) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	def static <T> filter(Stream<T> stream, (T, Long, Long)=>boolean filterFn) {
		val newStream = new Stream<T>
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)
		stream.on [
			each [
				val i = index.incrementAndGet
				if(filterFn.apply(it, i, passed.get)) {
					passed.incrementAndGet
					newStream.push(it)
				} else {
					stream.next
				}
			]
			error [	newStream.error(it) false ]
			finish [
				index.set(0)
				passed.set(0)
				newStream.finish(it)
			]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		stream.operation = 'filter'
		newStream
	}
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <K, V> Stream<Pair<K, V>> filter(Stream<Pair<K, V>> stream, (K, V)=>boolean filterFn) {
		stream.filter [ filterFn.apply(key, value) ]
	}

	/**
	 * Splits a stream into multiple parts. These parts are separated by Finish entries.
	 * Streams support multiple levels of finishes, to indicate multiple levels of splits.
	 * This allows you to split a stream, and then split it again.
	 * <p>
	 * It follows these rules:
	 * <ul>
	 * <li>when a new split is applied, it is always at finish level 0
	 * <li>all other stream operations that use finish, always use this level
	 * <li>the existing splits are all upgraded a level. so a level 0 finish becomes a level 1 finish
	 * <li>splits at a higher level always are carried through to a lower level. so wherever there is a
	 *     level 1 split for example, there is also a level 0 split
	 * </ul> 
	 * <p>
	 * The reason for these seemingly strange rules is that it allows us to split and merge a stream
	 * multiple times consistently. For example, consider the following stream, where finish(x) represents 
	 * a finish of level x:
	 * <pre>
	 * val s = (1..10).stream
	 * // this stream contains:
	 * 1, 2, 3, 4, 5, 6, 7, finish(0) 
	 * // the finish(0) is automatically added by the iterator as it ends
	 * </pre>
	 * If we split this stream at 4, we get this:
	 * <pre>
	 * val s2 = s.split[it==4]
	 * // s2 now contains:
	 * 1, 2, 3, 4, finish(0), 5, 6, 7, finish(0), finish(1)
	 * </pre>
	 * The split had as condition that it would occur at it==4, so after 4 a finish(0) was added.
	 * Also, the finish(0) at the end became upgraded to finish(1), and because a splits at
	 * a higher level always punch through, it also added a finish(0).
	 * <p>
	 * In the same manner we can keep splitting the stream and each time we will add another layer
	 * of finishes.
	 * <p>
	 * We can also merge the stream and this will reverse the process, reducing one level:
	 * <pre>
	 * val s3 = s2.merge
	 * // s3 now contains:
	 * 1, 2, 3, 4, 5, 6, 7, finish(0)
	 * </pre>
	 * <p>
	 * We can also merge by calling collect, which will transform the data between the splits into lists.
	 * The reason why splits of higher levels cut into the splits of lower levels is that the split levels
	 * are not independant. The finishes let you model a stream of stream. What essentially is simulated is:
	 * <pre>
	 * val s = int.stream
	 * val Stream<Stream<Integer>> s2 = s.split
	 * // and merge reverses:
	 * val Stream<Integer> s3 = s2.merge
	 * </pre>
	 * There are several reasons why this library does not use this substream approach:
	 * <ul>
	 * <li>Streams of streams are uncertain in their behavior, it is not guaranteed that this stream is serial or parallel.
	 * <li>Streams are not light objects, having queues, and having streams of streams would be memory and performance expensive
	 * <li>Streams of streams are not easily serializable and cannot easliy be throught of as a linear stream
	 * <li>Streams of streams are harder to reason and program with than a single stream
	 * </ul>
	 * <p>
	 * However the split and merge commands are there to simulate having substreams. To think of it more simply like a
	 * List<List<T>>, you cannot have a separation at the higher list level, which is not represented at the <List<T>> level.
	 */
	def static <T> Stream<T> split(Stream<T> stream, (T)=>boolean splitConditionFn) {
		val newStream = new Stream<T>
		val justPostedFinish0 = new AtomicBoolean(false)
		stream.on [
			each [
				if(splitConditionFn.apply(it)) {
					// apply multiple entries at once for a single next
					val entries = #[ new Value(it), new Finish(0) ]
					justPostedFinish0.set(true)
					newStream.apply(new Entries(entries))
				} else {
					justPostedFinish0.set(false)
					newStream.apply(new Value(it))
				}
			]
			error [ newStream.error(it) false ]
			finish [
				// a higher level split also splits up the lower level
				if(justPostedFinish0.get) {
					// we don't put finish(0)'s in a row
					newStream.apply(new Finish(it + 1))
				} else {
					justPostedFinish0.set(true)
					val entries = #[ new Finish(0), new Finish(it+1) ]
					newStream.apply(new Entries(entries))
				}
			]
			closed [ newStream.close ]
		]
		stream.operation = 'split'
		newStream.controls(stream)
		newStream
	}
	
	/**
	 * Merges one level of finishes. 
	 * @see StreamExtensions.split
	 */
	def static <T> Stream<T> merge(Stream<T> stream) {
		val newStream = new Stream<T>
		stream.on [
			each [ newStream.apply(new Value(it)) ]
			error [ newStream.error(it)	false ]
			finish [
				if(it > 0)
					newStream.finish(it - 1)
				else stream.next
			]
			closed [ newStream.close ]
		]
		stream.operation = 'merge'
		newStream.controls(stream)
		newStream
	}
	
	/**
	 * Only let pass a certain amount of items through the stream
	 */
	def static <T> Stream<T> limit(Stream<T> stream, int amount) {
		stream.until [ it, c | c > amount ] => [ stream.operation = 'limit(amount=' + amount + ')' ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <T> Stream<T> until(Stream<T> stream, (T)=>boolean untilFn) {
		stream.until [ it, index, passed | untilFn.apply(it) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <T> Stream<T> until(Stream<T> stream, (T, Long, Long)=>boolean untilFn) {
		val newStream = new Stream<T>
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)	
		stream.on [
			each [
				val i = index.incrementAndGet
				if(untilFn.apply(it, i, passed.get)) {
					passed.incrementAndGet
					stream.skip
					stream.next
				} else {
					newStream.push(it)
				}
			]
			error [ newStream.error(it) false ]
			finish [
				index.set(0)
				passed.set(0)
				newStream.finish(it)
			]
			closed [ newStream.close ]
		]
		stream.operation = 'until'
		newStream.controls(stream)
		newStream
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * Passes a counter as second parameter to the untilFn, starting at 1.
	 */
	def static <T> Stream<T> until(Stream<T> stream, (T, Long)=>boolean untilFn) {
		val count = new AtomicLong(0)
		val newStream = new Stream<T>
		stream.on [
			each [
				if(untilFn.apply(it, count.incrementAndGet)) {
					stream.skip
					stream.next
				} else {
					newStream.push(it)
				}
			]
			error [ newStream.error(it) false ]
			finish [ 
				count.set(0)
				newStream.finish(it)
			]
			closed [ newStream.close ]
		]
		stream.operation = 'until'
		newStream.controls(stream)
		newStream
	}
	
	/**
	 * Flatten a stream of streams into a single stream.
	 * <p>
	 * Note: breaks finishes and flow control!
	 */
	def static <T> Stream<T> flatten(Stream<Stream<T>> stream) {
		val newStream = new Stream<T>
		stream.on [ 
			each [ s |
				s.on [
					each [ newStream.push(it) s.next ]
					error [ newStream.error(it) s.next false ]
				]
				s.next
			]
			error [ newStream.error(it) false ]
			closed [ newStream.close ]
		]
		stream.operation = 'flatten'
		newStream.controls(stream)
		newStream
	}
	
	/**
	 * Performs a flatmap operation on the stream using the passed mapping function.
	 * <p>
	 * Flatmapping allows you to transform the values of the stream to multiple streams, 
	 * which are then merged to a single stream.
	 * <p>
	 * Note: breaks finishes and flow control!
	 */
	def static <T, R> Stream<R> flatMap(Stream<T> stream, (T)=>Stream<R> mapFn) {
		stream.map(mapFn).flatten => [ stream.operation = 'flatmap' ]
	}
	
	/**
	 * Keep count of how many items have been streamed so far, and for each
	 * value from the original stream, push a pair of count->value.
	 * Finish(0) resets the count.
	 */
	def static <T> Stream<Pair<Integer, T>> index(Stream<T> stream) {
		val newStream = new Stream<Pair<Integer, T>>
		val counter = new AtomicInteger(0)
		stream.on [
			each [ newStream.push(counter.incrementAndGet -> it) ]
			error [ newStream.error(it) false ]
			finish [
				if(it == 0) counter.set(0)
				newStream.finish(it)
			]
			closed [ newStream.close ]
		]
		stream.operation = 'index'
		newStream.controls(stream)
		newStream
	}
	
	// SPLITTING //////////////////////////////////////////////////////////////
	
	def static <T> StreamSource<T> split(Stream<T> stream) {
		new StreamCopySplitter(stream)
	}

	def static <T> StreamSource<T> balance(Stream<T> stream) {
		new LoadBalancer(stream)
	}

	// FLOW CONTROL ///////////////////////////////////////////////////////////
	
	/**
	 * Only allows one value for every timeInMs milliseconds to pass through the stream.
	 * All other values are dropped.
	 */
	def static <T> Stream<T> throttle(Stream<T> stream, int periodMs) {
		// -1 means allow, we want to allow the first incoming value
		val startTime = new AtomicLong(-1) 
		stream.filter [
			val now = System.currentTimeMillis
			if(startTime.get == -1 || now - startTime.get > periodMs) {
				// period has expired, reset the period and pass one
				startTime.set(now)
				true
			} else false
		] => [ stream.operation = 'throttle(periodMs=' + periodMs + ')' ]
	}
	
	// TODO: implement
	/** 
	 * Only allows one value for every timeInMs milliseconds to pass through the stream.
	 * All other values are buffered, and dropped only after the buffer has reached a given size.
	 */
	def static <T> Stream<T> ratelimit(Stream<T> stream, int periodMs, int bufferSize) {
		// -1 means allow, we want to allow the first incoming value
		val startTime = new AtomicLong(-1) 
		stream.filter [
			val now = System.currentTimeMillis
			if(startTime.get == -1 || now - startTime.get > periodMs) {
				// period has expired, reset the period and pass one
				startTime.set(now)
				true
			} else false
		] => [ stream.operation = 'ratelimit(periodMs=' + periodMs + ',bufferSize=' + bufferSize + ')' ]
	}
	
	/** 
	 * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
	 * <p>
	 * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
	 */
	def static <T> Stream<T> forEvery(Stream<T> stream, Stream<?> timerStream) {
		val newStream = new Stream<T>
		timerStream.on [
			error [ stream.error(it) timerStream.next false ]
			finish [ newStream.finish timerStream.next ]
			each [ 
				if(stream.open) stream.next
				else timerStream.close
				timerStream.next
			]
			closed [ stream.close ]
		]
		stream.on [
			each [ newStream.push(it) ]
			finish [ newStream.finish(it) ]
			error [ newStream.error(it) false ]
			closed [ newStream.close ]
		]
		newStream.monitor [
			skip [ stream.skip ]
			close [ stream.close ]
		]
		stream.operation = 'forEvery'
		timerStream.next
		newStream
	}
	
	/**
	 * Always get the latest value that was on the stream.
	 * FIX: does not work yet. Will require streams to allow multiple listeners
	 */
	@Deprecated
	def static <T> Stream<T> latest(Stream<T> stream) {
		val latest = new AtomicReference<T>
		val newStream = new Stream<T>
		stream.on [
			each [
				latest.set(it)
				stream.next
			]
			error [ newStream.error(it) stream.next false ]
			finish [ newStream.finish(it) stream.next ]
			closed [ newStream.close ]
		]
		// the new stream is not coupled at all to the source stream
		newStream.monitor [
			next [ newStream.push(latest.get) ]
			close [ stream.close ]
		]
		newStream
	}
	
	// RESOLVING //////////////////////////////////////////////////////////////
	
	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * It only asks the next promise from the stream when the previous promise has been resolved.  
	 */
	def static <T, R> Stream<T> resolve(Stream<? extends IPromise<T>> stream) {
		stream.resolve(1) => [ stream.operation = 'resolve' ]
	}

	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * <p>
	 * Allows concurrent promises to be resolved in parallel.
	 */
	def static <T, R> Stream<T> resolve(Stream<? extends IPromise<T>> stream, int concurrency) {
		val newStream = new Stream<T>
		val isFinished = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)
		stream.on [
			each [ promise |
				processes.incrementAndGet
				promise
					.onError [
						processes.decrementAndGet
						newStream.error(it)
						if(isFinished.get) newStream.finish
					]
					.then [
						processes.decrementAndGet
						newStream.push(it)
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
		stream.operation = 'resolve(concurrency=' + concurrency + ')'
		newStream
	}
	
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

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.call(1)
	 */	
	def static <T, R, P extends IPromise<R>> call(Stream<T> stream, (T)=>P promiseFn) {
		stream.call(1, promiseFn)
			=> [ stream.operation = 'call' ]
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.map(mappingFn).resolve(concurrency)
	 */	
	def static <T, R, P extends IPromise<R>> call(Stream<T> stream, int concurrency, (T)=>P promiseFn) {
		stream.map(promiseFn).resolve(concurrency)
			=> [ stream.operation = 'call(concurrency=' + concurrency + ')' ]
	}

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
	 * @return a new stream like the incoming stream but without the caught errors.
	 */
	def static <T> Stream<T> onError(Stream<T> stream, (Throwable)=>void handler) {
		val newStream = new Stream<T>
		stream.on [
			each [ newStream.push(it) ]
			error [ handler.apply(it) stream.next false ]
			finish [ newStream.finish(it) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	def static <T> Stream<T> onClosed(Stream<T> stream, (Void)=>void handler) {
		val newStream = new Stream<T>
		stream.on [
			each [ newStream.push(it) ]
			error [ newStream.error(it) false ]
			finish [ newStream.finish(it) ]
			closed [ 
				try {
					handler.apply(null)
				} finally {
					newStream.close
				}
			]
		]
		newStream.controls(stream)
		newStream
	}

	/** 
	 * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * Returns a task that completes once the stream finishes or closes.
	 */
	def static <T> Task onEach(Stream<T> stream, (T)=>void handler) {
		stream.on [
			each [ handler.apply(it) stream.next ]
			error [ stream.next true ]
			finish [ stream.next ]
			closed [ stream.close ]
		] => [ 
			stream.operation = 'onEach'
			stream.next
		]
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
	def static <T> Task onEachAsync(Stream<T> stream, (T)=>Task taskFn) {
		stream.map(taskFn).resolve.onEach [
			// just ask for the next 
		] => [ stream.operation = 'onEach(async)' ]
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
	 * Shortcut for splitting a stream and then performing a pipe to another stream.
	 * @return a CopySplitter source that you can connect more streams to. 
	 */
	def static <T> StreamSource<T> pipe(Stream<T> stream, Stream<T> target) {
		stream.split.pipe(target)
			=> [ stream.operation = 'pipe' ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Closes the stream once it has the value or an error.
	  */
	def static <T> IPromise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
		stream.on [
			each [ 
				if(!promise.fulfilled)
					promise.set(it) 
				stream.close 
			]
			error [
				if(!promise.fulfilled)
					promise.error(it) 
				 stream.close
				 false
			]
			finish [
				promise.error('Stream.first: stream finished without returning a value')
				stream.close
			]
			closed [
				promise.error('Stream.first: stream closed without returning a value')
			]
		]
		stream.operation = 'first'
	 	stream.next
		promise
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Will keep asking next on the stream until it gets to the last value!
	  * Skips any stream errors, and closes the stream when it is done.
	  */
	def static <T> IPromise<T> last(Stream<T> stream) {
		val promise = new Promise<T>
		val last = new AtomicReference<T>
		stream.on [
			each [ 
				if(!promise.fulfilled) last.set(it) 
				stream.next
			]
			finish [
				if(it == 0) {
					if(!promise.fulfilled && last.get != null) {
						promise.set(last.get)
						stream.close
					} else promise.error('stream finished without passing a value, no last entry found.')
				} else stream.next 
			]
			closed [ 
				if(!promise.fulfilled && last.get != null) {
					promise.set(last.get)
					stream.close
				} else promise.error('stream closed without passing a value, no last entry found.')
			]
			error [	stream.next false ]
		]
		stream.operation = 'last'
		stream.next
		promise
	}

	/**
	 * Skip an amount of items from the stream, and process only the ones after that.
	 * Resets at finish.
	 */
	def static <T> Stream<T> skip(Stream<T> stream, int amount) {
		stream.filter [ it, index, passed | index > amount ] 
			=> [ stream.operation = 'skip(amount=' + amount + ')' ]
	}

	/**
	 * Take only a set amount of items from the stream. 
	 * Resets at finish.
	 */
	def static <T> take(Stream<T> stream, int amount) {
		stream.until [ it, index, passed | index > amount ]
			=> [ stream.operation = 'limit(amount=' + amount + ')' ]
	}
	
	/**
	 * Start the stream and and promise the first value from it.
	 */
	def static <T> then(Stream<T> stream, Procedure1<T> listener) {
		stream.first.then(listener)
			=> [ stream.operation = 'then' ]
	}
	
	/**
	 * Start the stream and and promise the first value from it.
	 */
	def static <K, V> then(Stream<Pair<K, V>> stream, Procedure2<K, V> listener) {
		stream.first.then [ listener.apply(key, value) ]
			=> [ stream.operation = 'then' ]
	}

	// LISTENER BUILDERS //////////////////////////////////////////////////////

	/**
	 * Convenient builder to easily asynchronously respond to stream entries.
	 * Defaults for each[], finish[], and error[] is to simply ask for the next entry.
	 * <p>
	 * Note: you can only have a single entry handler for a stream.  
	 * <p>
	 * Usage example:
	 * <pre>
	 * val stream = (1.10).stream
	 * stream.on [
	 *    each [ println('got ' + it) stream.next ] // do not forget to ask for the next entry
	 *    error [ printStackTrace stream.next true ] // true to indicate you want to throw the error
	 * ]
	 * stream.next // do not forget to start the stream!
	 * </pre>
	 * @return a task that completes on finish(0) or closed, or that gives an error
	 * if the stream passed an error. 
	 */
	def static <T> Task on(Stream<T> stream, (StreamHandlerBuilder<T>)=>void handlerFn) {
		stream.on [ s, builder | handlerFn.apply(builder) ]
	}

	def static <T> Task on(Stream<T> stream, (Stream<T>, StreamHandlerBuilder<T>)=>void handlerFn) {
		val handler = new StreamHandlerBuilder<T>(stream) => [
			// by default, do nothing but call the next item from the stream
			each [ stream.next ]
			finish [ stream.next ]
			error [ stream.next true ] // by default, escalate errors
		]
		// observing first so the handlerFn is last and can issue stream commands
		val task = stream.observe(handler)
		handlerFn.apply(stream, handler)
		task
	}

	/**
	 * Convenient builder to easily respond to commands given to a stream.
	 * <p>
	 * Note: you can only have a single monitor for a stream.
	 * <p>
	 * Example: 
	 * <pre>
	 * stream.monitor [
	 *     next [ println('next was called on the stream') ]
	 *     close [ println('the stream was closed') ]
	 * ]
	 * </pre>
	 */
	def static <T> void monitor(Stream<T> stream, (StreamResponder)=>void handlerFn) {
		val handler = new StreamResponderBuilder
		handlerFn.apply(handler)
		stream.monitor(handler)
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////

	/** Perform some side-effect action based on the stream. */
	def static <T> effect(Stream<T> stream, (T)=>void listener) {
		stream.map [
			listener.apply(it)
			it
		] => [ stream.operation = 'effect' ]
	}

	/** Perform some side-effect action based on the stream. */
	def static <K, T> effect(Stream<Pair<K, T>> stream, (K, T)=>void listener) {
		stream.map [
			listener.apply(key, value)
			it
		] => [ stream.operation = 'effect' ]
	}

	// REVERSE AGGREGATIONS ///////////////////////////////////////////////////
	
	/** 
	 * Opposite of collect, separate each list in the stream into separate
	 * stream entries and streams those separately.
	 */
	def static <T> Stream<T> separate(Stream<List<T>> stream) {
		val newStream = new Stream<T>
		stream.on [
			each [ list |
				// apply multiple entries at once for a single next
				val entries = list.map [ new Value(it) ]
				newStream.apply(new Entries(entries))
			]
			error [ newStream.error(it) false ]
			finish [ newStream.finish(it) ]
			closed [ newStream.close ]
		]
		stream.operation = 'separate'
		newStream.controls(stream)
		newStream
	}

	// AGGREGATIONS ///////////////////////////////////////////////////////////

	/**
	 * Collect all items from a stream, separated by finishes
	 */
	def static <T> Stream<List<T>> collect(Stream<T> stream) {
		val Stream<List<T>> s = stream.reduce(newArrayList) [ list, it | list.concat(it) ]
		stream.operation = 'collect'
		s
	}
	
	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	def static <T extends Number> sum(Stream<T> stream) {
		stream.reduce(0D) [ acc, it | acc + doubleValue ]
			=> [ stream.operation = 'sum' ]
	}

	/**
	 * Average the items in the stream until a finish.
	 */
	def static <T extends Number> average(Stream<T> stream) {
		stream
			.index
			.reduce(0 -> 0D) [ acc, it | key -> (acc.value + value.doubleValue) ]
			.map [ value / key ]
			=> [ stream.operation = 'average' ]
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	def static <T> Stream<Integer> count(Stream<T> stream) {
		stream.reduce(0) [ acc, it | acc + 1 ]
			=> [ stream.operation = 'count' ]
	}

	/**
	 * Gives the maximum value found on the stream.
	 * Values must implement Comparable
	 */
	def static <T extends Comparable<T>> Stream<T> max(Stream<T> stream) {
		val Stream<T> s = stream.reduce(null) [ acc, it | if(acc != null && acc.compareTo(it) > 0) acc else it ]
		stream.operation = 'max'
		s
	}

	/**
	 * Gives the minimum value found on the stream.
	 * Values must implement Comparable
	 */
	def static <T extends Comparable<T>> Stream<T> min(Stream<T> stream) {
		val Stream<T> s = stream.reduce(null) [ acc, it | if(acc != null && acc.compareTo(it) < 0) acc else it ]
		stream.operation = 'min'
		s
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * The counter is the count of the incoming stream entry (since the start or the last finish)
	 * Errors in the stream are suppressed.
	 */
	def static <T, R> Stream<R> reduce(Stream<T> stream, R initial, (R, T)=>R reducerFn) {
		val reduced = new AtomicReference<R>(initial)
		val newStream = new Stream<R>
		stream.on [
			each [
				reduced.set(reducerFn.apply(reduced.get, it))
				stream.next
			]
			finish [
				if(it == 0) {
					val result = reduced.getAndSet(initial)
					if(result != null) newStream.push(result)
					else newStream.error('no result found when reducing')
				} else {
					newStream.finish(it - 1)
				}
			]
			error [
				// newStream.error(it)
				// just process the next
				stream.next
				false
			]
			closed [ newStream.close ]
		]
		stream.operation = 'reduce(initial=' + initial + ')'
		newStream.controls(stream)
		newStream
	}

	def static <T, R> Stream<R> scan(Stream<T> stream, R initial, (R, T)=>R reducerFn) {
		val reduced = new AtomicReference<R>(initial)
		val newStream = new Stream<R>
		stream.on [
			each [
				val result = reducerFn.apply(reduced.get, it)
				reduced.set(result)
				if(result != null) newStream.push(result)
				stream.next
			]
			finish [
				reduced.set(initial) 
				newStream.finish(it)
			]
			error [ newStream.error(it) false ]
			closed [ newStream.close ]
		]
		stream.operation = 'scan(initial=' + initial + ')'
		newStream.controls(stream)
		newStream
	}
	
	/**
	 * Streams true if all stream values match the test function
	 */
	def static <T> Stream<Boolean> all(Stream<T> stream, (T)=>boolean testFn) {
		val s = stream.reduce(true) [ acc, it | acc && testFn.apply(it) ]
		stream.operation = 'all'
		s
	}

	/**
	 * Streams true if no stream values match the test function
	 */
	def static <T> Stream<Boolean> none(Stream<T> stream, (T)=>boolean testFn) {
		val s = stream.reduce(true) [ acc, it | acc && !testFn.apply(it) ]
		stream.operation = 'none'
		s
	}

	/**
	 * Streams true if any of the values match the passed testFn.
	 * <p>
	 * Note that this is not a normal reduction, since no finish is needed
	 * for any to fire true. The moment testFn gives off true, true is streamed
	 * and the rest of the incoming values are skipped.
	 */
	def static <T> Stream<Boolean> any(Stream<T> stream, (T)=>boolean testFn) {
		val anyMatch = new AtomicBoolean(false)
		val newStream = new Stream<Boolean>
		stream.on [
			each [
				// if we get a match, we communicate directly and tell the stream we are done
				if(testFn.apply(it)) {	
					anyMatch.set(true)
					newStream.push(true)
					stream.skip
				}
				stream.next
			]
			finish [
				if(it == 0) {
					val matched = anyMatch.get
					anyMatch.set(false)
					if(!matched) newStream.push(false)
				} else {
					newStream.finish(it - 1)
				}
			]
			error [ newStream.error(it) false ]
			closed [ newStream.close ]
		]
		stream.operation = 'any'
		newStream.controls(stream)
		newStream
	}
	
	/**
	 * Streams the first value that matches the testFn
	 * <p>
	 * Note that this is not a normal reduction, since no finish is needed to fire a value.
	 * The moment testFn gives off true, the value is streamed and the rest of the incoming 
	 * values are skipped.
	 */
	def static <T> Stream<T> first(Stream<T> stream, (T)=>boolean testFn) {
		val match = new AtomicReference<T>
		val newStream = new Stream<T>
		stream.on [
			each [
				// if we get a match, we communicate directly and tell the stream we are done
				if(testFn.apply(it)) {	
					match.set(it)
					newStream.push(it)
					stream.skip
				}
				stream.next
			]
			finish [
				if(it == 0) {
					match.set(null)
				} else {
					newStream.finish(it - 1)
				}
			]
			error [ newStream.error(it) false ]
			closed [ newStream.close ]
		]
		stream.operation = 'first'
		newStream.controls(stream)
		newStream
	}

	// WRITING TO OUTPUT STREAMS AND FILES ///////////////////////////////////

	def static Stream<String> toText(Stream<List<Byte>> stream) {
		stream.toText('UTF-8')
	}
	
	def static Stream<String> toText(Stream<List<Byte>> stream, String encoding) {
		stream
			.map [ new String(it, encoding).split('\n').toList ]
			.separate
			=> [ stream.operation = 'toText(encoding=' +  encoding + ')' ]
	}
	
	def static Stream<List<Byte>> toBytes(Stream<String> stream) {
		stream.toBytes('UTF-8')
	}

	def static Stream<List<Byte>> toBytes(Stream<String> stream, String encoding) {
		stream
			.map [ (it + '\n').getBytes(encoding) as List<Byte> ]
			=> [ stream.operation = 'toBytes(encoding=' +  encoding + ')' ]
	}

	/** write a buffered bytestream to an standard java outputstream */
	@Async def static void writeTo(Stream<List<Byte>> stream, OutputStream out, Task task) {
		stream.on [
			closed [ out.close task.complete ]
			finish [ 
				if(it == 0) out.close task.complete 
				stream.next
			]
			error [ 
				task.error(it)
				stream.close
				true
			]
			each [
				out.write(it)
				stream.next
			]
		]
		stream.operation = 'writeTo'
		stream.next
	}

	/** write a buffered bytestream to a file */
	@Async def static void writeTo(Stream<List<Byte>> stream, File file, Task task) {
		val sink = Files.asByteSink(file)
		val out = sink.openBufferedStream
		stream.writeTo(out, task)
		stream.operation = 'writeTo(file=' + file.absolutePath + ')'
	}

	// OTHER //////////////////////////////////////////////////////////////////

	/** 
	 * Complete a task when the stream finishes or closes, 
	 * or give an error on the task when the stream gives an error.
	 */	
	@Async def static void toTask(Stream<?> stream, Task task) {
		stream.on [
			closed [ task.complete ]
			finish [ stream.close task.complete ]
			error [ stream.close task.error(it) true ]
			each [ /* discard values */]
		]
		stream.operation = 'toTask'
	}
	
	// From Xtend-tools
	private def static <T> List<T> concat(Iterable<? extends T> list, T value) {
		if(value != null) ImmutableList.builder.add
		if(value != null) ImmutableList.builder.addAll(list).add(value).build
	}
}
