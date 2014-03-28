package nl.kii.stream

import com.google.common.util.concurrent.AtomicDouble
import java.util.LinkedList
import java.util.List
import java.util.Map
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class StreamExt {
	
	/** create a stream of the given type */
	def static <T> stream(Class<T> type) {
		new Stream<T>
	}
	
	/** create a stream of a set of data */
	def static <T> stream(T... data) {
		val newStream = new Stream<T>
		data.forEach [ newStream.push(it) ]
		newStream.finish
		newStream
	}

	/** stream the data of a map as a list of key->value pairs */
	def static <K, V> stream(Map<K, V> data) {
		val newStream = new Stream<Pair<K, V>>
		data.entrySet.forEach [ newStream.push(key -> value) ]
		newStream.finish
		newStream
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
		stream.onFinish [ newStream.finish ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | newStream.onDone(done).push(mappingFn.apply(it)) ]
		newStream
	}
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <T> filter(Stream<T> stream, (T)=>boolean filterFn) {
		val newStream = new Stream<T>(stream)
		stream.onFinish [ newStream.finish ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | if(filterFn.apply(it)) newStream.push(it) ]
		newStream
	}
	
	/**
	 * Flatten a stream of lists to just the items in that list
	 */
	def static <T> flatten(Stream<List<T>> stream) {
		val newStream = new Stream<T>(stream)
		stream.onFinish [ newStream.finish ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | forEach [ newStream.push(it) ] ]
		newStream
	}

	/**
	 * Collect all items from a stream, separated by finishes
	 */
	def static <T> Stream<List<T>> collect(Stream<T> stream) {
		val list = new AtomicReference(new LinkedList<T>)
		val newStream = new Stream<List<T>>(stream)
		stream.onFinish [ newStream.push(list.get); list.set(new LinkedList<T>) ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | list.get.add(it) ]
		newStream
	}
	
	def static <T> Stream<T> split(Stream<T> stream, (T)=>boolean splitConditionFn) {
		val newStream = new Stream<T>(stream)
		stream.onFinish [ newStream.finish ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s |
			it >> newStream
			if(splitConditionFn.apply(it))
				newStream.finish
		]
		newStream
	}

	// PROMISE CHAINING ///////////////////////////////////////////////////////	
	
	/**
	 * Perform an async operation which returns a promise.
	 * This allows you to chain multiple async methods, as
	 * long as you let your methods return a Promise
	 */
	def static <T, R> Stream<R> async(Stream<T> stream, (T)=>Promise<R> promiseFn) {
		val newStream = new Stream<R>(stream)
		stream.onFinish [ newStream.finish ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | promiseFn.apply(it).then [ newStream.push(it) ] ]
		newStream
	}	

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/**
	 * Start the stream and handle each streamed value.
	 * <p>
	 * If autostart is true for the stream (which is it by default), this will
	 * start the stream, as well as call the listerer for each pushed stream value.
	 */
	def static <T> void each(Stream<T> stream, Procedure1<T> listener) {
		stream.each(true) [ it, done, s | 
			listener.apply(it)
		]
	}
	
	/**
	 * Forward the results of the stream to another stream 
	 */
	def static <T> void forwardTo(Stream<T> stream, Stream<T> otherStream) {
		stream.onChange [ otherStream.apply(it) ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  */
	def static <T> Promise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
	 	stream.each(true) [ it, done, s |
 			promise.apply(it)
	 		done.apply
	 	]
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
	 * Add the value of all the items in the stream until a finish.
	 */
	def static <T extends Number> sum(Stream<T> stream) {
		val sum = new AtomicDouble(0)
		val newStream = new Stream<Double>(stream)
		stream.onFinish [ newStream.push(sum.doubleValue); sum.set(0) ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | sum.addAndGet(doubleValue) ]
		newStream
	}

	/**
	 * Average the items in the stream until a finish
	 */
	def static <T extends Number> avg(Stream<T> stream) {
		val sum = new AtomicDouble
		val count = new AtomicLong(0)
		val newStream = new Stream<Double>(stream)
		stream.onFinish [ newStream.push(sum.doubleValue / count.getAndSet(0)); sum.set(0) ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | sum.addAndGet(doubleValue); count.incrementAndGet ]
		newStream
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	def static <T> count(Stream<T> stream) {
		val count = new AtomicLong(0)
		val newStream = new Stream<Long>(stream)
		stream.onFinish [ newStream.push(count.getAndSet(0)) ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | count.incrementAndGet ]
		newStream
	}

	/**
	 * Reduce a stream of values to a single value until a finish.
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val newStream = new Stream<T>(stream)
		stream.onFinish [ newStream.push(reduced.getAndSet(initial)) ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | reduced.set(reducerFn.apply(reduced.get, it)) ]
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
		stream.onFinish [ newStream.push(reduced.getAndSet(initial)); count.set(0) ]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s | reduced.set(reducerFn.apply(reduced.get, it, count.getAndIncrement)) ]
		newStream
	}

	/**
	 * Only let pass a certain amount of items through the stream
	 */
	def static <T> Stream<T> limit(Stream<T> stream, int amount) {
		val count = new AtomicLong(0)
		val newStream = new Stream<T>(stream)
		stream.onFinish [ 
			count.set(0)
			newStream.finish
		]
		stream.onError [ newStream.error(it) ]
		stream.each(false) [ it, done, s |
			println('OI!' + count.get)
			if(count.incrementAndGet > amount) {
				done.apply
			} else {
				newStream.push(it)
			}
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
		stream.onError [ newStream.error(it) ]
	 	stream.each(false) [ it, done, s | 
		 	// if we get a match, we communicate directly
		 	// and tell the stream we are done
	 		if(testFn.apply(it)) {	
	 			anyMatch.set(true)
	 			newStream.push(true)
	 			done.apply
	 		}
	 	]
	 	newStream
	 }

}
