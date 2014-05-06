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

class StreamExt {
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** create a stream of the given type */
	def static <T> stream(Class<T> type) {
		new Stream<T>
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
				stream.push(iterator.next)
			} else if(!finished.get) {
				finished.set(true)
				stream.finish
			}
		]
		stream.onReadyForNext(pushNext)
		pushNext.apply
		stream
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Add a value to a stream */
	def static <T> operator_doubleGreaterThan(T value, Stream<T> stream) {
		stream.push(value)
		stream
	}
	
	/** Add a value to a stream */
	def static <T> operator_doubleLessThan(Stream<T> stream, T value) {
		stream.push(value)
		stream
	}

	/** Add a list of values to a stream */
	def static <T> operator_doubleGreaterThan(List<T> value, Stream<T> stream) {
		value.forEach [ stream.push(it) ]
		stream
	}
	
	/** Add a list of values to a stream */
	def static <T> operator_doubleLessThan(Stream<T> stream, List<T> value) {
		value.forEach [ stream.push(it) ]
		stream
	}

	/** Add an entry to a stream (such as error or finish) */
	def static <T> operator_doubleLessThan(Stream<T> stream, Entry<T> entry) {
		stream.apply(entry)
		stream
	}

	/** Lets you easily pass an Error<T> to the stream using the << operator */
	def static <T> operator_doubleLessThan(Stream<T> stream, Throwable t) {
		stream.apply(new Error<T>(t))
		stream
	}

	/** Lets you easily pass an Error<T> to the stream using the >> operator */
	def static <T> operator_doubleGreaterThan(Throwable t, Stream<T> stream) {
		stream.apply(new Error<T>(t))
		stream
	}

	/** Lets you easily pass a Finish<T> entry using the << or >> operators */
	def static <T> finish() {
		new Finish<T>
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/**
	 * Transform each item in the stream using the passed mappingFn
	 */
	def static <T, R> map(Stream<T> stream, (T)=>R mappingFn) {
		val newStream = new Stream<R>(stream)
		stream.onNextValue [ 
			newStream.push(mappingFn.apply(it))
		]
		newStream
	}
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <T> filter(Stream<T> stream, (T)=>boolean filterFn) {
		val newStream = new Stream<T>(stream)
		stream.onNextValue [ 
			if(filterFn.apply(it)) {
				newStream.push(it)
			} else {
				stream.next
			}
		]
		newStream
	}

	def static <T> Stream<T> split(Stream<T> stream, (T)=>boolean splitConditionFn) {
		val newStream = new Stream<T>(stream)
		stream.onNextValue [
			if(splitConditionFn.apply(it)) {
				// manually publishing, so the publish is 'atomic'
				newStream << new Value(it) << new Finish
				newStream.publish
			} else {
				newStream.push(it)
			}
		]
		newStream
	}
	
	/** 
	 * Create from an existing stream a stream of streams, separated by finishes in the start stream.
	 */
	def static <T> Stream<Stream<T>> substream(Stream<T> stream) {
		val newStream = new Stream<Stream<T>>(stream)
		val substream = new AtomicReference(new Stream<T>)
		stream.onNextFinish [| 
			newStream.push(substream.get)
			substream.set(new Stream<T>)
		]
		stream.onNextValue [
			substream.get.push(it)
		]
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
		val newStream = new Stream<T>(stream)
		stream.onNextValue [
			if(untilFn.apply(it)) {
				stream.skip
				stream.next
			} else {
				newStream.push(it)
			}
		]
		newStream		
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * Passes a counter as second parameter to the untilFn, starting at 1.
	 */
	def static <T> Stream<T> until(Stream<T> stream, (T, Long)=>boolean untilFn) {
		val count = new AtomicLong(0)
		val newStream = new Stream<T>(stream)
		stream
			.onNextFinish [|
				count.set(0)
				newStream.finish
			]
			.onNextValue [
				if(untilFn.apply(it, count.incrementAndGet)) {
					stream.skip
					stream.next
				} else {
					newStream.push(it)
				}
			]
		newStream
	}
	
	/**
	 * Map using a promise. 
	 * Waits until each promise is resolved before calling the promiseFn again.
	 */
	def static <T, R> Stream<R> mapAsync(Stream<T> stream, (T)=>Promise<R> promiseFn) {
		stream.map(promiseFn).resolve
	}

	/**
	 * Map using a promise. 
	 * Allows for up to concurreny promises to be open at once.
	 */
	def static <T, R> Stream<R> mapAsync(Stream<T> stream, int concurrency, (T)=>Promise<R> promiseFn) {
		stream.map(promiseFn).resolve(concurrency)		
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
	 * Allows concurrency promises to be resolved in parallel.  
	 */
	def static <T, R> Stream<T> resolve(Stream<Promise<T>> stream, int concurrency) {
		val processes = new AtomicInteger(0)
		val newStream = new Stream<T>(stream)
		stream.onNextValue [ promise |
			processes.incrementAndGet 
			promise
				.onError [ 
					newStream.error(it)
					if(processes.decrementAndGet < concurrency) 
						stream.next
				]
				.then [ 
					newStream.push(it)
					if(processes.decrementAndGet < concurrency) 
						stream.next
				]
		]
		newStream
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////

	/** 
	 * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
	 */
	def static <T> onEach(Stream<T> stream, (T)=>void listener) {
		stream.onNextValue [
			try {
				listener.apply(it)
			} finally {
				stream.next
			}
		]
		stream.next
	}

	/** 
	 * Asynchronous listener to the stream for values. Calls the processor for each incoming value and
	 * asks the next value from the stream every time the processor finishes.
	 */
	def static <T, R> onEachAsync(Stream<T> stream, (T)=>Promise<R> asyncProcessor) {
		stream.mapAsync(asyncProcessor)
			.onEach [
				// do nothing, just request the next
			]
//		stream.onNextValue [
//			asyncProcessor.apply(it)
//				.onError [ stream.error(it) ]
//				.then [ stream.next ]
//		]
	}

	/** 
	 * Synchronous listener to the stream for finishes. Automatically requests the next entry.
	 */
	def static <T> onFinish(Stream<T> stream, (Void)=>void listener) {
		stream.onNextFinish [| 
			try {
				listener.apply(null)
			} finally {
				stream.next
			}
		]
	}

	/** 
	 * Synchronous listener to the stream for finishes. Automatically requests the next entry.
	 */
	def static <T, R> onFinishAsync(Stream<T> stream, (Void)=>Promise<R> listener) {
		stream.onNextFinish [|
			try {
				listener.apply(null)
					.onError [
						stream.error(it)
						stream.next
					]
					.then [
						stream.next
					]
			} finally {
				stream.next
			}
		]
	}
		
	/** 
	 * Synchronous listener to the stream for errors. Automatically requests the next entry.
	 */
	def static <T> onError(Stream<T> stream, (Throwable)=>void listener) {
		stream.onNextError [
			try {
				listener.apply(null)
			} finally {
				stream.next
			}
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
		stream.onNextError [ 
			otherStream.error(it)
			stream.next
		]
		stream.onNextFinish [| 
			otherStream.finish
			stream.next
		]
		stream.onNextValue [ 
			otherStream.push(it)
			stream.next
		]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  */
	def static <T> Promise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
	 	stream.onNextValue [
	 		if(!promise.fulfilled) promise.set(it)
	 	]
	 	stream.next
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
		val newStream = new Stream<List<T>>(stream)
		stream.onNextFinish [|
			val collected = list.get
			list.set(new LinkedList<T>)
			newStream.push(collected)
		]
		stream.onNextValue [ 
			list.get.add(it)
			stream.next
		]
		newStream
	}
	
	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	def static <T extends Number> sum(Stream<T> stream) {
		val sum = new AtomicDouble(0)
		val newStream = new Stream<Double>(stream)
		stream.onNextFinish [|
			val collected = sum.doubleValue
			sum.set(0)
			newStream.push(collected)
		]
		stream.onNextValue [ 
			sum.addAndGet(doubleValue)
			stream.next
		]
		newStream
	}

	/**
	 * Average the items in the stream until a finish
	 */
	def static <T extends Number> avg(Stream<T> stream) {
		val avg = new AtomicDouble
		val count = new AtomicLong(0)
		val newStream = new Stream<Double>(stream)
		stream.onNextFinish [|
			val collected = avg.doubleValue / count.getAndSet(0) 
			avg.set(0)
			newStream.push(collected)
		]
		stream.onNextValue [ 
			avg.addAndGet(doubleValue)
			count.incrementAndGet
			stream.next
		]
		newStream
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	def static <T> count(Stream<T> stream) {
		val count = new AtomicLong(0)
		val newStream = new Stream<Long>(stream)
		stream.onNextFinish [| 
			newStream.push(count.getAndSet(0))
		]
		stream.onNextValue [ 
			count.incrementAndGet
			stream.next
		]
		newStream
	}

	/**
	 * Reduce a stream of values to a single value until a finish.
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val newStream = new Stream<T>(stream)
		stream.onNextFinish [|
			newStream.push(reduced.getAndSet(initial))
		]
		stream.onNextValue [
			reduced.set(reducerFn.apply(reduced.get, it))
			stream.next
		]
		newStream
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * The counter is the count of the incoming stream entry (since the start or the last finish)
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T, Long)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val count = new AtomicLong(0)
		val newStream = new Stream<T>(stream)
		stream.onNextFinish [|
			val result = reduced.getAndSet(initial)
			count.set(0)
			newStream.push(result)
		]
		stream.onNextValue [ 
			reduced.set(reducerFn.apply(reduced.get, it, count.getAndIncrement))
			stream.next
		]
		newStream
	}

	/**
	 * True if any of the values match the passed testFn
	 * FIX: currently .first gives recursive loop?
	 */
	 def static <T> Stream<Boolean> anyMatch(Stream<T> stream, (T)=>boolean testFn) {
	 	val anyMatch = new AtomicBoolean(false)
	 	val newStream = new Stream<Boolean>(stream)
	 	stream.onNextFinish [|
	 		val matched = anyMatch.get
	 		anyMatch.set(false)
	 		if(!matched) newStream.push(false)
	 	]
	 	stream.onNextValue [
		 	// if we get a match, we communicate directly and tell the stream we are done
	 		if(testFn.apply(it)) {	
	 			anyMatch.set(true)
	 			newStream.push(true)
	 			stream.skip
	 		}
	 		stream.next
	 	]
		newStream
	 }

}
