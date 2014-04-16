package nl.kii.stream

import com.google.common.util.concurrent.AtomicDouble
import java.util.LinkedList
import java.util.List
import java.util.Map
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import java.util.concurrent.atomic.AtomicInteger

class StreamExt {
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** create a stream of the given type */
	def static <T> stream(Class<T> type) {
		new Stream<T>
	}
	
	/** create a stream of a set of data and finish it */
	def static <T> stream(T... data) {
		new Stream<T> => [
			for(i : data) push(i)
			finish
		]
	}

	/** stream the data of a map as a list of key->value pairs */
	def static <K, V> stream(Map<K, V> data) {
		new Stream<Pair<K, V>> => [
			for(i : data.entrySet) push(i.key -> i.value)
			finish
		]
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
		stream.forEach [ newStream.push(mappingFn.apply(it)) ]
		newStream
	}
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <T> filter(Stream<T> stream, (T)=>boolean filterFn) {
		val newStream = new Stream<T>(stream)
		stream.forEach [ if(filterFn.apply(it)) newStream.push(it) ]
		newStream
	}

	def static <T> Stream<T> split(Stream<T> stream, (T)=>boolean splitConditionFn) {
		val newStream = new Stream<T>(stream)
		stream.forEach [
			it >> newStream
			if(splitConditionFn.apply(it)) newStream.finish
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
			.onFinish [ 
				newStream.push(substream.get)
				substream.set(new Stream<T>)
			]
			.forEach [ substream.get.push(it) ]
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
		stream.forEach [ it, s |
			promiseFn.apply(it)
				.onError [ newStream.error(it) ]
				.then [ 
					newStream.push(it)
					s.next
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
		stream.forEach [ it, s |
			processes.incrementAndGet 
			promiseFn.apply(it)
				.onError [ newStream.error(it) ]
				.then [ 
					newStream.push(it)
					if(processes.decrementAndGet < concurrency) s.next
				]
		]
		newStream
	}	

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Alias for forEach
	 */
	def static <T> each(Stream<T> stream, (T)=>void listener) {
		stream.forEach(listener)
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
			.onError [ otherStream.error(it) ]
			.onFinish [ otherStream.finish ]
			.forEach [ otherStream.push(it) ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  */
	def static <T> Promise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
	 	stream.forEach [ if(!promise.fulfilled) promise.set(it) ]
		promise
	}

	/**
	 * Start the stream and listen to the first value only.
	 */
	 def static <T> void then(Stream<T> stream, Procedure1<T> listener) {
	 	stream.first.then(listener)
	 }
	
	// REDUCTIONS /////////////////////////////////////////////////////////////

	/**
	 * Collect all items from a stream, separated by finishes
	 */
	def static <T> Stream<List<T>> collect(Stream<T> stream) {
		val list = new AtomicReference(new LinkedList<T>)
		val newStream = new Stream<List<T>>(stream)
		stream
			.onFinish [ 
				newStream.push(list.get)
				list.set(new LinkedList<T>)
			]
			.forEach [ list.get.add(it) ]
		newStream
	}
	
	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	def static <T extends Number> sum(Stream<T> stream) {
		val sum = new AtomicDouble(0)
		val newStream = new Stream<Double>(stream)
		stream
			.onFinish [ newStream.push(sum.doubleValue); sum.set(0) ]
			.forEach [ sum.addAndGet(doubleValue) ]
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
			.onFinish [ 
				newStream.push(sum.doubleValue / count.getAndSet(0))
				sum.set(0)
			]
			.forEach [ 
				sum.addAndGet(doubleValue)
				count.incrementAndGet
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
			.onFinish [ newStream.push(count.getAndSet(0)) ]
			.forEach [ count.incrementAndGet ]
		newStream
	}

	/**
	 * Reduce a stream of values to a single value until a finish.
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val newStream = new Stream<T>(stream)
		stream
			.onFinish [ newStream.push(reduced.getAndSet(initial)) ]
			.forEach [	reduced.set(reducerFn.apply(reduced.get, it)) ]
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
			.onFinish [ newStream.push(reduced.getAndSet(initial)); count.set(0) ]
			.forEach [ reduced.set(reducerFn.apply(reduced.get, it, count.getAndIncrement)) ]
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
		stream.forEach [ it, s |
			if(untilFn.apply(it)) {
				s.skip
			} else {
				newStream.push(it)
			}
			s.next
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
			.onFinish [ 
				count.set(0)
				newStream.finish
			]
			.forEach [ it, s |
				if(untilFn.apply(it, count.incrementAndGet)) {
					s.skip
				} else {
					newStream.push(it)
				}
				s.next
			]
		newStream
	}

	/**
	 * True if any of the values match the passed testFn
	 */
	 def static <T> Stream<Boolean> anyMatch(Stream<T> stream, (T)=>boolean testFn) {
	 	val anyMatch = new AtomicBoolean(false)
	 	val newStream = new Stream<Boolean>(stream)
	 	stream.onFinish [	
	 		if(!anyMatch.get) newStream.push(false)
	 		anyMatch.set(false)
	 	]
	 	stream.forEach [ it, s |
		 	// if we get a match, we communicate directly
		 	// and tell the stream we are done
	 		if(testFn.apply(it)) {	
	 			anyMatch.set(true)
	 			newStream.push(true)
	 			s.skip
	 		}
	 		s.next
	 	]
		newStream
	 }

}
