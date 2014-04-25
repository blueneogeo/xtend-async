package nl.kii.stream

import com.google.common.util.concurrent.AtomicDouble
import java.util.Iterator
import java.util.LinkedList
import java.util.List
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import java.util.Map

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
		stream.onNext(pushNext)
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
		stream.onValue [ 
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
		stream.onValue [ 
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
		stream.onValue [
			if(splitConditionFn.apply(it)) {
				newStream.queue(new Value(it))
				newStream.queue(new Finish)
				newStream.publishFromQueue
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
		stream
			.onFinish [| 
				newStream.push(substream.get)
				substream.set(new Stream<T>)
			]
			.onValue [
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
		stream.onValue [
			if(untilFn.apply(it)) {
				stream.skip
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
			.onFinish [|
				count.set(0)
				newStream.finish
			]
			.onValue [
				if(untilFn.apply(it, count.incrementAndGet)) {
					stream.skip
				} else {
					newStream.push(it)
				}
				// s.next
			]
		newStream
	}
	
	// PROMISE CHAINING ///////////////////////////////////////////////////////	
	
	/**
	 * Perform an async operation which returns a promise.
	 * This allows you to chain multiple async methods, as
	 * long as you let your methods return a Promise.
	 * <p>
	 * It will only perform one async operation at a time, asking for the
	 * next value from the stream only after it has performed each promise.
	 * <p>
	 * Use stream.async(concurrent) [ ] to have more than one async operation
	 * performing simulaniously.
	 */
	def static <T, R> Stream<R> async(Stream<T> stream, (T)=>Promise<R> promiseFn) {
		// same as stream.async(1, promiseFn), here just for performance reasons
		val newStream = new Stream<R>(stream)
		stream.onValue [
			promiseFn.apply(it)
				.onError [ newStream.error(it) ]
				.then [ 
					newStream.push(it)
					// s.next
				]
		]
		newStream
	}	

	/**
	 * Perform an async operation which returns a promise.
	 * This allows you to chain multiple async methods, as
	 * long as you let your methods return a Promise.
	 * <p>
	 * It will only perform at max 'concurrency' async operations at the same time.
	 */
	def static <T, R> Stream<R> async(Stream<T> stream, int concurrency, (T)=>Promise<R> promiseFn) {
		val processes = new AtomicInteger(0)
		val newStream = new Stream<R>(stream)
		stream.onValue [
			processes.incrementAndGet 
			promiseFn.apply(it)
				.onError [ newStream.error(it) ]
				.then [ 
					newStream.push(it)
					if(processes.decrementAndGet < concurrency) stream.next
				]
		]
		newStream
	}	

	// ENDPOINTS //////////////////////////////////////////////////////////////

	def static <T> each(Stream<T> stream, (T)=>void listener) {
		try {
			stream.onValue(listener)
		} finally {
			stream.next
		}
	}
	
	def static <T> each(Stream<T> stream, (T)=>Promise<?> asyncProcessor) {
		stream.onValue [
			asyncProcessor.apply(it)
				.onError [ stream.error(it) ]
				.then [	stream.next	]
		]
	}
	
	
	def static <T> finish(Stream<T> stream, (Void)=>void listener) {
		stream.onFinish [| 
			try {
				listener.apply(null)
			} finally {
				stream.next
			}
		]
	}
	
	def static <T> error(Stream<T> stream, (Throwable)=>void listener) {
		try {
			stream.onError(listener)
		} finally {
			stream.next
		}
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
		stream
			.onError [ 
				otherStream.error(it)
				stream.next
			]
			.onFinish [| 
				otherStream.finish
				stream.next
			]
			.onValue [ 
				otherStream.push(it)
				stream.next
			]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  */
	def static <T> Promise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
	 	stream.onValue [ if(!promise.fulfilled) promise.set(it) ]
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
		stream
			.onFinish [|
				newStream.push(list.get)
				list.set(new LinkedList<T>)
				stream.next
			]
			.onValue [ 
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
		stream
			.onFinish [| 
				newStream.push(sum.doubleValue)
				sum.set(0)
				stream.next
			]
			.onValue [ 
				sum.addAndGet(doubleValue)
				stream.next
			]
		newStream
	}

	/**
	 * Average the items in the stream until a finish
	 */
	def static <T extends Number> avg(Stream<T> stream) {
		val sum = new AtomicDouble
		val count = new AtomicLong(0)
		val newStream = new Stream<Double>(stream)
		stream
			.onFinish [|
				newStream.push(sum.doubleValue / count.getAndSet(0))
				sum.set(0)
				stream.next
			]
			.onValue [ 
				sum.addAndGet(doubleValue)
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
		stream
			.onFinish [| 
				newStream.push(count.getAndSet(0))
				stream.next
			]
			.onValue [ 
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
		stream
			.onFinish [|
				newStream.push(reduced.getAndSet(initial))
				stream.next
			]
			.onValue [
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
		stream
			.onFinish [|
				newStream.push(reduced.getAndSet(initial))
				count.set(0)
				stream.next
			]
			.onValue [ 
				reduced.set(reducerFn.apply(reduced.get, it, count.getAndIncrement))
				stream.next
			]
		newStream
	}

	/**
	 * True if any of the values match the passed testFn
	 */
	 def static <T> Stream<Boolean> anyMatch(Stream<T> stream, (T)=>boolean testFn) {
	 	val anyMatch = new AtomicBoolean(false)
	 	val newStream = new Stream<Boolean>(stream)
	 	stream.onFinish [|
	 		if(!anyMatch.get) newStream.push(false)
	 		anyMatch.set(false)
	 	]
	 	stream.onValue [
		 	// if we get a match, we communicate directly
		 	// and tell the stream we are done
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
