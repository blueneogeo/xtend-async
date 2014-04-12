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
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** create a stream of the given type */
	def static <T> stream(Class<T> type) {
		new Stream<T>
	}
	
	/** create a stream of a set of data */
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
		val newStream = new Stream<R>
		stream
			.chain(newStream)
			.onFinish [ newStream.finish ]
			.onError [ newStream.error(it) ]
			.each [ newStream.push(mappingFn.apply(it)) ]
		newStream
	}
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <T> filter(Stream<T> stream, (T)=>boolean filterFn) {
		val newStream = new Stream<T>
		stream
			.chain(newStream)
			.onSkip [ newStream.skip ]
			.onError [ newStream.error(it) ]
			.onFinish [ newStream.finish ]
			.forEach [ if(filterFn.apply(it)) newStream.push(it) ]
		newStream
	}

	def static <T> Stream<T> split(Stream<T> stream, (T)=>boolean splitConditionFn) {
		val newStream = new Stream<T>
		stream
			.chain(newStream)
			.onFinish [ newStream.finish ]
			.onError [ newStream.error(it) ]
			.each [
				it >> newStream
				if(splitConditionFn.apply(it)) newStream.finish
			]
		newStream
	}
	
	/** 
	 * Create from an existing stream a stream of streams, separated by finishes in the start stream.
	 */
	def static <T> Stream<Stream<T>> substream(Stream<T> stream) {
		val substream = new AtomicReference(new BufferedStream<T>)
		val newStream = new Stream<Stream<T>>
		stream
			.chain(newStream)
			.onFinish [ newStream.push(substream.get); substream.set(new BufferedStream<T>) ]
			.onError [ newStream.error(it) ]
			.each [ substream.get.push(it) ]
		newStream
	}

	/**
	 * Collect all items from a stream, separated by finishes
	 */
	def static <T> Stream<List<T>> collect(Stream<T> stream) {
		val list = new AtomicReference(new LinkedList<T>)
		val newStream = new Stream<List<T>>
		stream
			.chain(newStream)
			.onFinish [ newStream.push(list.get); list.set(new LinkedList<T>) ]
			.onError [ newStream.error(it) ]
			.each [ list.get.add(it) ]
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
	 * Start listening to the stream immediately and handle each streamed value.
	 * Automatically asks for the next item.
	 */
	def static <T> Connection<T> each(Stream<T> stream, Procedure1<T> listener) {
		val conn = stream.connect [
			listener.apply(it)
			self.next
		]
		conn.open
		conn
	}

	/**
	 * Forward the results of the stream to another stream and start that stream. 
	 */
	def static <T> void forwardTo(Stream<T> stream, Stream<T> otherStream) {
		stream
			.onFinish [ otherStream.finish ]
			.onError [ otherStream.error(it) ]
			.each [ otherStream.push(it) ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  */
	def static <T> Promise<T> first(Stream<T> stream) {
		val promise = new Promise<T>
	 	stream.each [ if(!promise.finished) promise.apply(it) ]
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
		val newStream = new Stream<Double>
		stream
			.chain(newStream)
			.onFinish [ newStream.push(sum.doubleValue); sum.set(0) ]
			.onError [ newStream.error(it) ]
			.connect [ 
				sum.addAndGet(doubleValue)
				self.next
			]
		newStream
	}

	/**
	 * Average the items in the stream until a finish
	 */
	def static <T extends Number> avg(Stream<T> stream) {
		val sum = new AtomicDouble
		val count = new AtomicLong(0)
		val newStream = new Stream<Double>
		stream
			.chain(newStream)
			.onFinish [ newStream.push(sum.doubleValue / count.getAndSet(0)); sum.set(0) ]
			.onError [ newStream.error(it) ]
			.connect [ 
				sum.addAndGet(doubleValue)
				count.incrementAndGet
				self.next
			]
		newStream
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	def static <T> count(Stream<T> stream) {
		val count = new AtomicLong(0)
		val newStream = new Stream<Long>
		stream
			.chain(newStream)
			.onFinish [ newStream.push(count.getAndSet(0)) ]
			.onError [ newStream.error(it) ]
			.connect [ 
				count.incrementAndGet
				self.next
			]
		newStream
	}

	/**
	 * Reduce a stream of values to a single value until a finish.
	 */
	def static <T> Stream<T> reduce(Stream<T> stream, T initial, (T, T)=>T reducerFn) {
		val reduced = new AtomicReference<T>(initial)
		val newStream = new Stream<T>
		stream
			.chain(newStream)
			.onFinish [ newStream.push(reduced.getAndSet(initial)) ]
			.onError [ newStream.error(it) ]
			.connect [ 
				reduced.set(reducerFn.apply(reduced.get, it))
				self.next
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
		val newStream = new Stream<T>
		stream
			.chain(newStream)
			.onError [ newStream.error(it) ]
			.onFinish [ newStream.push(reduced.getAndSet(initial)); count.set(0) ]
			.connect [ 
				reduced.set(reducerFn.apply(reduced.get, it, count.getAndIncrement))
				self.next
			]
		newStream
	}

	/**
	 * Only let pass a certain amount of items through the stream
	 */
	def static <T> Stream<T> limit(Stream<T> stream, int amount) {
		val count = new AtomicLong(0)
		val newStream = new Stream<T>
		stream
			.chain(newStream)
			.onError [ newStream.error(it) ]
			.onFinish [ 
				count.set(0)
				newStream.finish
			]
			.connect [
				if(count.incrementAndGet > amount) {
					self.close
				} else {
					newStream.push(it)
					self.next
				}
			]
		newStream
	}
	
	/**
	 * Limit the amount of items in this batch (stop listening until the next finish)
	 */
	def static <T> Stream<T> limitBatch(Stream<T> stream, int amount) {
		val count = new AtomicLong(0)
		val newStream = new Stream<T>
		stream
			.chain(newStream)
			.onError [ newStream.error(it) ]
			.onFinish [ 
				count.set(0)
				newStream.finish
			]
			.connect [
				if(count.incrementAndGet > amount) {
					self.skip
				} else {
					newStream.push(it)
					self.next
				}
			]
		newStream
	}
	
	/**
	 * True if any of the values match the passed testFn
	 */
	 def static <T> Stream<Boolean> anyMatch(Stream<T> stream, (T)=>boolean testFn) {
	 	val anyMatch = new AtomicBoolean(false)
	 	val newStream = new Stream<Boolean>
	 	stream.onFinish [	
	 		if(!anyMatch.get) newStream.push(false)
	 		anyMatch.set(false)
	 	]
		stream.onError [ newStream.error(it) ]
	 	stream.forEach [ 
		 	// if we get a match, we communicate directly
		 	// and tell the stream we are done
	 		if(testFn.apply(it)) {	
	 			anyMatch.set(true)
	 			newStream.push(true)
	 			self.skip
	 		}
	 		self.next
	 	]
		newStream
			.onOpen [ stream.open ]
			.onSkip [ stream.skip ]
			.onClose [ stream.close ]
	 }
	 
	 /** 
	  * When the child stream opens, closes or skips, pass these messages to the
	  * parent stream. Used when chaining streams together.
	  */
	 def static private <T, R> chain(Stream<T> stream, Stream<R> childStream) {
		childStream
			.onSkip [ stream.skip ]
		stream
	 } 

}
