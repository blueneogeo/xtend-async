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
		// stream.onReadyForNext(pushNext)
		pushNext.apply
		stream
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
		new Finish<T>
	}

	def package static <T, R> connectTo(Stream<T> newStream, AsyncSubscription<?> parent) {
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
				newStream.finish
			]
		]
		newStream.connectTo(subscription)
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
				counter.set(0) 
				newStream.finish
			]
		]
		newStream.connectTo(subscription)
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
				newStream.finish
			]
		]
		newStream.connectTo(subscription)
		newStream
	}

	def static <T> Stream<T> split(Stream<T> stream, (T)=>boolean splitConditionFn) {
		val newStream = new Stream<T>
		val subscription = stream.onAsync [
			each [
				if(splitConditionFn.apply(it)) {
					// apply multiple entries at once for a single next
					val entries = #[ new Value<T>(it), new Finish ]
					newStream.apply(new Entries<T>(entries))
				} else {
					newStream.apply(new Value(it))
				}
			]
			error [ 
				newStream.error(it)
			]
			finish [ 
				newStream.finish
			]
		]
		newStream.connectTo(subscription)
		newStream
	}
	
	/** 
	 * Create from an existing stream a stream of streams, separated by finishes in the start stream.
	 */
	def static <T> Stream<Stream<T>> substream(Stream<T> stream) {
		val newStream = new Stream<Stream<T>>
		val substream = new AtomicReference(new Stream<T>)
		val subscription = stream.onAsync [
			each [
				substream.get.push(it)
			]
			error [ 
				newStream.error(it)
			]
			finish [
				newStream.push(substream.get)
				substream.set(new Stream<T>)
			]
		]
		newStream.connectTo(subscription)
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
				newStream.finish
			]
		]
		newStream.connectTo(subscription)
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
				newStream.finish
			]
		]
		newStream.connectTo(subscription)
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
			if(concurrency > open) {
				stream.next
			}
		]
		stream.onAsync [
			each [ promise |
				processes.incrementAndGet
				promise
					.then [
						newStream.push(it)
						onProcessComplete.apply 
					]
			]
			error [ newStream.error(it) ]
			finish [ 
				if(processes.get == 0) {
					newStream.finish
					stream.next
				} else {
					// we are still processing, so finish when we are done processing instead
					isFinished.set(true)
				}
			]
		]
		// newStream.connectTo(subscription)
		stream.next
		newStream
	}

//	def static <T, R> Stream<T> resolve2(Stream<Promise<T>> stream, int concurrency) {
//		val processes = new AtomicInteger(0)
//		val newStream = new Stream<T>
//		val isFinished = new AtomicBoolean(false)
//		stream.entryListener = [
//			switch it {
//				Value<Promise<T>>: {
//					processes.incrementAndGet 
//					println('starting process ' + processes.get)
//					// if more processes are available, request a next value
//					if(concurrency > processes.get) {
//						println('requesting more since we have more concurrency')
//						stream.next
//					}
//					value
//						.onError [ newStream.error(it) ]
//						.then [
//							newStream.push(it)
//							val open = processes.decrementAndGet  
//							println('finished process ' + (processes.get + 1) + ' for ' + it)
//							if(open == 0 && isFinished.get) {
//								println('finishing after all processes completed')
//								isFinished.set(false)
//								newStream.finish
//							} else if(concurrency > open){
//								println('requesting new process')
//								stream.next
//							}
//						]
//				}
//				Finish<?>: {
//					println('process finish')
//					if(processes.get == 0) {
//						newStream.finish
//						println('requesting next after finish')
//						stream.next
//					} else {
//						// we are still processing, so finish when we are done processing instead
//						isFinished.set(true)
//					}					
//				}
//				Error<?>: newStream.error(error)
//			}
//		]
//		newStream.cmdListener = [
//			switch it {
//				Close: stream.close
//			}
//		]
//		stream.next
//		newStream
//	}

	// ENDPOINTS //////////////////////////////////////////////////////////////

	def static <T> on(Stream<T> stream, (SyncSubscription<T>)=>void handlerFn) {
		val handler = new SyncSubscription<T>(stream)
		handlerFn.apply(handler)
		stream.next // automatically start streaming
		handler
	}

	def static <T> onAsync(Stream<T> stream, (AsyncSubscription<T>)=>void handlerFn) {
		val handler = new AsyncSubscription<T>(stream)
		handlerFn.apply(handler)
		handler
	}
	
	def static <T> monitor(Stream<T> stream, (CommandSubscription)=>void subscriptionFn) {
		val handler = new CommandSubscription(stream)
		subscriptionFn.apply(handler)
		handler
	}	

	/** 
	 * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * note: onEach swallows exceptions in your listener. If you needs error detection/handling, use .on[] instead.
	 */
	def static <T> onEach(Stream<T> stream, (T)=>void listener) {
		stream.on [ 
			each (listener)
			error [ throw it ]
		]
	}

	// TODO: implement
	/** 
	 * Asynchronous listener to the stream for values. Calls the processor for each incoming value and
	 * asks the next value from the stream every time the processor finishes.
	 */
//	def static <T, R> onEachAsync(Stream<T> stream, (T)=>Promise<T> asyncProcessor) {
//		stream
//			.map(asyncProcessor)
//			.resolve
//			.onEach [
//				// do nothing, just request the next
//			]
//		stream.onNextValue [
//			asyncProcessor.apply(it)
//				.onError [ stream.error(it) ]
//				.then [ stream.next ]
//		]
//	}

	// TODO: implement
	/** 
	 * Processes a stream of tasks. It returns a new stream that allows you to listen for errors,
	 * and that pushes thr number of tasks that were performed when the source stream of tasks
	 * finishes.
	 * <pre>
	 * def Task doSomeTask(int userId) {
	 * 		task [ doSomeTask |
	 * 			..do some asynchronous work here..
	 * 		]
	 * }
	 * 
	 * val userIds = #[5, 6, 2, 67]
	 * userIds.stream
	 * 		.map [ doSomeTask ]
	 * 		.process(3) // 3 parallel processes max
	 * </pre>
	 */
//	def static <T, R> process(Stream<Task> stream, int concurrency) {
//		val processes = new AtomicInteger(0)
//		val count = new AtomicLong(0)
//		val newStream = new Stream<Long>(stream)
//		stream.onNextValue [ promise |
//			processes.incrementAndGet 
//			promise
//				.onError [ 
//					newStream.error(it)
//					if(processes.decrementAndGet < concurrency) 
//						stream.next
//				]
//				.then [
//					count.incrementAndGet
//					if(processes.decrementAndGet < concurrency) 
//						stream.next
//				]
//		]
//		stream.onNextFinish[|
//			newStream.push(count.get)
//			count.set(0)
//			stream.next
//		]
//		newStream
//	}

	/**
	 * Processes a stream of tasks. It returns a new stream that allows you to listen for errors,
	 * and that pushes the number of tasks that were performed when the source stream of tasks
	 * finishes. Performs at most one task at a time.
	 * 
	 * @see StreamExtensions.process(Stream<Task> stream, int concurrency)
	 */
//	def static <T, R> process(Stream<Task> stream) {
//		stream.process(1)
//	}

// TODO: implement	
//	/** 
//	 * Synchronous listener to the stream for finishes. Automatically requests the next entry.
//	 */
//	def static <T> onFinish(Stream<T> stream, (Void)=>void listener) {
//		stream.onNextFinish [| 
//			try {
//				listener.apply(null)
//			} finally {
//				stream.next
//			}
//		]
//	}
//
//	/** 
//	 * Synchronous listener to the stream for finishes. Automatically requests the next entry.
//	 */
//	def static <T, R> onFinishAsync(Stream<T> stream, (Void)=>Promise<R> listener) {
//		stream.onNextFinish [|
//			try {
//				listener.apply(null)
//					.onError [
//						stream.error(it)
//						stream.next
//					]
//					.then [
//						stream.next
//					]
//			} finally {
//				stream.next
//			}
//		]
//	}

//	/** 
//	 * Synchronous listener to the stream for errors. Automatically requests the next entry.
//	 */
//	def static <T> onError(Stream<T> stream, (Throwable)=>void listener) {
//		stream.onNextError [
//			try {
//				listener.apply(null)
//			} finally {
//				stream.next
//			}
//		]
//	}
//	

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
				val collected = list.get
				list.set(new LinkedList)
				newStream.push(collected)
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
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
				val collected = sum.doubleValue
				sum.set(0)
				newStream.push(collected)
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
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
				val collected = avg.doubleValue / count.getAndSet(0) 
				avg.set(0)
				newStream.push(collected)
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
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
				newStream.push(count.getAndSet(0))
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
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
				newStream.push(reduced.getAndSet(initial))
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
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
				val result = reduced.getAndSet(initial)
				count.set(0)
				newStream.push(result)
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
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
		 		val matched = anyMatch.get
		 		anyMatch.set(false)
		 		if(!matched) newStream.push(false)
			]
			error [
				newStream.error(it)
			]
		]
		newStream.connectTo(subscription)
		newStream
	 }

}
