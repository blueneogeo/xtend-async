package nl.kii.stream

import com.google.common.util.concurrent.AtomicDouble
import java.util.Iterator
import java.util.LinkedList
import java.util.List
import java.util.Map
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class StreamExtensions {
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** create a stream of the given type */
	def static <T> stream(Class<T> type) {
		new Stream<T>
	}
	
	def static <T> streamList(Class<T> type) {
		new Stream<List<T>>
	}

	def static <K, V> streamMap(Pair<Class<K>, Class<V>> type) {
		new Stream<Map<K, V>>
	}
	
	/** create a stream of a set of data and finish it */
	def static <T> stream(T... data) {
		data.iterator.stream
	}

	/** stream the data of a map as a list of key->value pairs */
	def static <K, V> stream(Map<K, V> data) {
		data.entrySet.map [ key -> value ].stream
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
			if(iterator.hasNext) {
				iterator.next >> stream
			} else if(!finished.get) {
				finished.set(true)
				stream.finish
			}
		]
		stream.monitor [
			onNext [ pushNext.apply ]
		]
		pushNext.apply
		stream
	}
	
	// OBSERVING //////////////////////////////////////////////////////////////

	/** 
	 * Observe changes on the observable. This allows you to observe the stream
	 * with multiple listeners. Observables do not support flow control, so every
	 * value coming from the stream will be pushed out immediately.
	 */
	def static <T> Observable<T> observe(Stream<T> stream) {
		new StreamObserver(stream)
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

	def package static <T, R> controls(Stream<T> newStream, AsyncSubscription<?> parent) {
		newStream.monitor [
			onNext [ 
				parent.next
			]
			onSkip [ 
				parent.skip
			]
			onClose [ 
				parent.close
			]
		]		
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/**
	 * Transform each item in the stream using the passed mappingFn
	 */
	def static <T, R> map(Stream<T> stream, (T)=>R mappingFn) {
		val newStream = new Stream<R>
		val subscription = stream.onAsync [
			each [
				val mapped = mappingFn.apply(it)
				newStream.push(mapped)
			]
			error [ 
				newStream.error(it)
			]
			finish [ 
				newStream.finish(level)
			]
		]
		newStream.controls(subscription)
		newStream
	}
	
	/**
	 * Transform each item in the stream using the passed mappingFn.
	 * Also passes a counter to count the amount of items passed since
	 * the start or the last finish.
	 */
	def static <T, R> map(Stream<T> stream, (T, long)=>R mappingFn) {
		val counter = new AtomicLong(0)
		val newStream = new Stream<R>
		val subscription = stream.onAsync [
			each [
				val mapped = mappingFn.apply(it, counter.incrementAndGet)
				newStream.push(mapped)
			]
			error [ 
				newStream.error(it)
			]
			finish [
				if(level == 0) 
					counter.set(0) 
				newStream.finish(level)
			]
		]
		newStream.controls(subscription)
		newStream
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <T> filter(Stream<T> stream, (T)=>boolean filterFn) {
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				if(filterFn.apply(it)) {
					newStream.push(it)
				} else {
					stream.next
				}
			]
			error [ 
				newStream.error(it)
			]
			finish [ 
				newStream.finish(level)
			]
		]
		newStream.controls(subscription)
		newStream
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
		val subscription = stream.onAsync [
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
			error [ 
				newStream.error(it)
			]
			finish [
				// a higher level split also splits up the lower level
				if(justPostedFinish0.get) {
					// we don't put finish(0)'s in a row
					newStream.apply(new Finish(level + 1))
				} else {
					justPostedFinish0.set(true)
					val entries = #[ new Finish(0), new Finish(level+1) ]
					newStream.apply(new Entries(entries))
				}
			]
		]
		newStream.controls(subscription)
		newStream
	}
	
	/**
	 * Merges one level of finishes.
	 */
	def static <T> Stream<T> merge(Stream<T> stream) {
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				newStream.apply(new Value(it))
			]
			error [
				newStream.error(it)
			]
			finish [
				if(level > 0)
					newStream.finish(level - 1)
				else stream.next
			]
		]
		newStream.controls(subscription)
		newStream
	}
	
	/**
	 * Only let pass a certain amount of items through the stream
	 */
	def static <T> Stream<T> limit(Stream<T> stream, int amount) {
		stream.until [ it, c | c > amount ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <T> Stream<T> until(Stream<T> stream, (T)=>boolean untilFn) {
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				if(untilFn.apply(it)) {
					stream.skip
					stream.next
				} else {
					newStream.push(it)
				}
			]
			error [ 
				newStream.error(it)
			]
			finish [ 
				newStream.finish(level)
			]
		]
		newStream.controls(subscription)
		newStream
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * Passes a counter as second parameter to the untilFn, starting at 1.
	 */
	def static <T> Stream<T> until(Stream<T> stream, (T, Long)=>boolean untilFn) {
		val count = new AtomicLong(0)
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				if(untilFn.apply(it, count.incrementAndGet)) {
					stream.skip
					stream.next
				} else {
					newStream.push(it)
				}
			]
			error [ 
				newStream.error(it)
			]
			finish [ 
				count.set(0)
				newStream.finish(level)
			]
		]
		newStream.controls(subscription)
		newStream
	}
	
	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * It only asks the next promise from the stream when the previous promise has been resolved.  
	 */
	def static <T, R> Stream<T> resolve(Stream<Promise<T>> stream) {
		stream.resolve(1)
	}

	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * <p>
	 * Allows concurrency promises to be resolved in parallel.
	 * <p>
	 * note: resolving breaks flow control. 
	 */
	def static <T, R> Stream<T> resolve(Stream<Promise<T>> stream, int concurrency) {
		val newStream = new Stream<T>
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
			each [ promise |
				processes.incrementAndGet
				promise
					.onError [
						newStream.error(it)
						stream.next
					]
					.then [
						newStream.push(it)
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
	
	// STREAM ENDPOINTS ////////////////////////////////////////////////////

	/** 
	 * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * note: onEach swallows exceptions in your listener. If you needs error detection/handling, use .on[] instead.
	 */
	def static <T> void onEach(Stream<T> stream, (T)=>void listener) {
		stream.on [ 
			each (listener)
			error [ throw it ]
		]
	}

	def static <T> void onEach(Stream<T> stream, (T, AsyncSubscription<T>)=>void listener) {
		stream.onAsync [ sub |
			sub.each [
				listener.apply(it, sub)
			]
			sub.error [ throw it ]
		]
	}

	/** 
	 * Create a new stream that listenes to this stream
	 */
	def static <T> fork(Stream<T> stream) {
		stream.map[it]
	}

	/**
	 * Forward the results of the stream to another stream and start that stream. 
	 */
	def static <T> void forwardTo(Stream<T> stream, Stream<T> otherStream) {
		stream.on [
			each [ otherStream.push(it) ]
			error [ otherStream.error(it) ]
			finish [ otherStream.finish ]
		]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  */
	def static <T> Promise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
		val subscription = stream.onAsync [
			each [
				if(!promise.fulfilled) promise.set(it)
			]
			error [
				if(!promise.fulfilled) promise.error(it)
			]
		]
	 	subscription.next
		promise
	}

	/**
	 * Start the stream and listen to the first value only.
	 */
	 def static <T> void then(Stream<T> stream, Procedure1<T> listener) {
	 	stream.first.then(listener)
	 }
	
	// SUBSCRIPTION BUILDERS //////////////////////////////////////////////////

	def static <T> onError(Stream<T> stream, (Throwable)=>void listener) {
		stream.onAsync [ subscription | 
			subscription.error [
				listener.apply(it)
				subscription.next
			]
		]
	}

	def static <T> onFinish(Stream<T> stream, (Finish<T>)=>void listener) {
		stream.onAsync [ subscription | 
			subscription.finish [
				listener.apply(it)
				subscription.next
			]
		]
	}
	
	def static <T> onError(AsyncSubscription<T> subscription, (Throwable)=>void listener) {
		subscription.error [
			listener.apply(it)
			subscription.next
		]
		subscription
	}

	def static <T> onFinish(AsyncSubscription<T> subscription, (Finish<T>)=>void listener) {
		subscription.finish [
			listener.apply(it)
			subscription.next
		]
		subscription
	}
	
	def static <T> on(Stream<T> stream, (SyncSubscription<T>)=>void subscriptionFn) {
		val subscription = new SyncSubscription<T>(stream)
		subscriptionFn.apply(subscription)
		stream.next // automatically start streaming
		subscription
	}

	def static <T> onAsync(Stream<T> stream, (AsyncSubscription<T>)=>void subscriptionFn) {
		val subscription = new AsyncSubscription<T>(stream)
		subscriptionFn.apply(subscription)
		subscription
	}
	
	def static <T> monitor(Stream<T> stream, (CommandSubscription)=>void subscriptionFn) {
		val handler = new CommandSubscription(stream)
		subscriptionFn.apply(handler)
		handler
	}	

	// SUBSCRIPTION ENDPOINTS /////////////////////////////////////////////////

	def static <T> void onEach(AsyncSubscription<T> subscription, (T)=>void listener) {
		subscription.each [
			listener.apply(it)
			subscription.next
		]
		subscription.next
	}

	def static <T> void onEach(AsyncSubscription<T> subscription, (T, AsyncSubscription<T>)=>void listener) {
		subscription.each [
			listener.apply(it, subscription)
		]
		subscription.next
	}

	// AGGREGATIONS ///////////////////////////////////////////////////////////

	/**
	 * Collect all items from a stream, separated by finishes
	 */
	def static <T> Stream<List<T>> collect(Stream<T> stream) {
		val list = new AtomicReference(new LinkedList<T>)
		val newStream = new Stream<List<T>>
		val subscription = stream.onAsync [
			each [
				list.get.add(it)
				stream.next
			]
			finish [
				if(level == 0) {
					val collected = list.get
					list.set(new LinkedList)
					newStream.push(collected)
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	}
	
	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	def static <T extends Number> sum(Stream<T> stream) {
		val sum = new AtomicDouble(0)
		val newStream = new Stream<Double>
		val subscription = stream.onAsync [
			each [
				sum.addAndGet(doubleValue)
				stream.next
			]
			finish [
				if(level == 0) {
					val collected = sum.doubleValue
					sum.set(0)
					newStream.push(collected)
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	}

	/**
	 * Average the items in the stream until a finish
	 */
	def static <T extends Number> avg(Stream<T> stream) {
		val avg = new AtomicDouble
		val count = new AtomicLong(0)
		val newStream = new Stream<Double>
		val subscription = stream.onAsync [
			each [
				avg.addAndGet(doubleValue)
				count.incrementAndGet
				stream.next
			]
			finish [
				if(level == 0) {
					val collected = avg.doubleValue / count.getAndSet(0) 
					avg.set(0)
					newStream.push(collected)
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	def static <T> count(Stream<T> stream) {
		val count = new AtomicLong(0)
		val newStream = new Stream<Long>
		val subscription = stream.onAsync [
			each [
				count.incrementAndGet
				stream.next
			]
			finish [
				if(level == 0) {
					newStream.push(count.getAndSet(0))
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	}

	/**
	 * Reduce a stream of values to a single value until a finish.
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				reduced.set(reducerFn.apply(reduced.get, it))
				stream.next
			]
			finish [
				if(level == 0) {
					newStream.push(reduced.getAndSet(initial))
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * The counter is the count of the incoming stream entry (since the start or the last finish)
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T, Long)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val count = new AtomicLong(0)
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				reduced.set(reducerFn.apply(reduced.get, it, count.getAndIncrement))
				stream.next
			]
			finish [
				if(level == 0) {
					val result = reduced.getAndSet(initial)
					count.set(0)
					newStream.push(result)
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	}

	/**
	 * True if any of the values match the passed testFn
	 * FIX: currently .first gives recursive loop?
	 */
	 def static <T> Stream<Boolean> anyMatch(Stream<T> stream, (T)=>boolean testFn) {
	 	val anyMatch = new AtomicBoolean(false)
	 	val newStream = new Stream<Boolean>
		val subscription = stream.onAsync [
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
				if(level == 0) {
			 		val matched = anyMatch.get
			 		anyMatch.set(false)
			 		if(!matched) newStream.push(false)
				} else {
					newStream.finish(level - 1)
				}
			]
			error [
				newStream.error(it)
			]
		]
		newStream.controls(subscription)
		newStream
	 }

}
