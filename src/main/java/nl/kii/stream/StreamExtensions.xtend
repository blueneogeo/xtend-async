package nl.kii.stream

import com.google.common.collect.ImmutableList
import com.google.common.io.ByteProcessor
import java.io.IOException
import java.io.InputStream
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.AsyncException
import nl.kii.async.UncaughtAsyncException
import nl.kii.observe.Observable
import nl.kii.observe.Publisher
import nl.kii.promise.IPromise
import nl.kii.promise.SubPromise
import nl.kii.promise.SubTask
import nl.kii.promise.Task
import nl.kii.stream.message.Close
import nl.kii.stream.message.Closed
import nl.kii.stream.message.Entries
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Finish
import nl.kii.stream.message.Next
import nl.kii.stream.message.Overflow
import nl.kii.stream.message.Skip
import nl.kii.stream.message.Value
import nl.kii.stream.source.LoadBalancer
import nl.kii.stream.source.StreamCopySplitter
import nl.kii.stream.source.StreamSource
import nl.kii.util.AssertionException
import nl.kii.util.Period
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static extension com.google.common.io.ByteStreams.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.util.DateExtensions.*
import static extension nl.kii.util.ThrowableExtensions.*

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
	def static <R, T, T2 extends Iterable<T>> stream(IPromise<R, T2> promise) {
		val newStream = new Stream<T>
		promise
			.on(Throwable) [ newStream.error(it) ]
			.then [	stream(it).pipe(newStream) ]
		newStream
	}

	/** stream an list, ending with a finish. makes an immutable copy internally. */	
	def static <T> streamList(List<T> list) {
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
		stream.when [
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
		newStream.when [
			skip [ stream.close]
			close [ stream.close ]
		]
		newStream
	}
	
	/** create an unending stream of random integers in the range you have given */
	def static streamRandom(IntegerRange range) {
		val randomizer = new Random
		val newStream = int.stream
		newStream.when [
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

	/**
	 * Observe the entries coming off this stream using a StreamObserver.
	 * Note that you can only have ONE stream observer for every stream!
	 * If you want more than one observer, you can split the stream, or
	 * use StreamExtensions.monitor, which does this for you.
	 * <p>
	 * If you are using Xtend, it is recommended to use the StreamExtensions.on [ ]
	 * instead, for a more concise and elegant builder syntax.
	 */
	def static <I, O> void observe(IStream<I, O> stream, StreamObserver<I, O> observer) {
		stream.operation = 'observe'
		stream.onChange [ entry |
			switch it : entry {
				Value<I, O>: observer.onValue(from, value)
				Finish<I, O>: observer.onFinish(from, level)
				Error<I, O>: observer.onError(from, error)
				Closed<I, O>: observer.onClosed
			}
		]
	}
	
	/** Listen to commands given to the stream. */
	def static <I, O> void handle(IStream<I, O> stream, StreamEventHandler controller) {
		stream.onNotify [ notification |
			switch it : notification {
				Next: controller.onNext
				Skip: controller.onSkip
				Close: controller.onClose
				Overflow: controller.onOverflow(entry)
			}
		]
	}

	/** 
	 * Create a publisher for the stream. This allows you to observe the stream
	 * with multiple listeners. Publishers do not support flow control, and the
	 * created Publisher will eagerly pull all data from the stream for publishing.
	 */
	def static <I, O> publisher(IStream<I, O> stream) {
		val publisher = new Publisher<O>
		stream.on [
			each [
				publisher.apply($1)
				if(publisher.publishing) stream.next
			]
			closed [ publisher.publishing = false ]
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
	def static <T> observe(Observable<T> observable) {
		val newStream = new Stream<T>
		val stopObserving = observable.onChange [
			newStream.push(it)
		]
		newStream.when [
			close [ stopObserving.apply ]
		]
		newStream
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Add a value to a stream */
	def static <T> >> (T input, Stream<T> stream) {
		stream << input
	}
	
	/** Add a value to a stream */
	def static <T> << (Stream<T> stream, T input) {
		stream.push(input)
		stream
	}

	/** Add a list of values to a stream */
	def static <T> >> (List<T> values, Stream<T> stream) {
		stream << values
	}
	
	/** Add a list of values to a stream */
	def static <T> << (Stream<T> stream, List<T> values) {
		for(value : values) stream.push(value)
		stream
	}

	/** Add an entry to a stream (such as error or finish) */
	def static <I, O> << (Stream<O> stream, Entry<I, O> entry) {
		stream.apply(entry)
		stream
	}

	/** Lets you easily pass an Error<T> to the stream using the >> operator */
	def static <T> >> (Throwable t, Stream<T> stream) {
		stream << t
	}

	/** Lets you easily pass an Error<T> to the stream using the << operator */
	def static <I, O> << (Stream<O> stream, Throwable t) {
		stream.apply(new Error<I, O>(null, t))
		stream
	}

	/** pipe a stream into another stream */
	def static <I, O> >> (IStream<I, O> source, IStream<I, O> dest) {
		dest << source
	}

	/** pipe a stream into another stream */
	def static <I, O> << (IStream<I, O> dest, IStream<I, O> source) {
		source.pipe(dest)
	}

	/** split a source into a new destination stream */
	def static <I, O> >> (StreamSource<I, O> source, IStream<I, O> dest) {
		source.pipe(dest)
	}

	/** Lets you easily pass a Finish<T> entry using the << or >> operators */
	def static <I, O> finish() {
		new Finish<I, O>(null, 0)
	}

	/** Lets you easily pass a Finish<T> entry using the << or >> operators */
	def static <I, O> finish(int level ) {
		new Finish<I, O>(null, level)
	}

	/** Forwards commands given to the newStream directly to the parent. */
	def static <I1, I2, O1, O2> controls(IStream<I1, O2> newStream, IStream<I2, O1> parent) {
		newStream.when [
			next [ parent.next ]
			skip [ parent.skip ]
			close [ parent.close ]
		]		
	}
	
	/** Set the concurrency of the stream, letting you keep chaining by returning the stream. */
	def static <I, O> concurrency(IStream<I, O> stream, int value) {
		stream.concurrency = value
		stream
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	def static <I, O, I2, O2, S extends IStream<I2, O2>> S transform(IStream<I, O> stream, (Entry<I, O>, SubStream<I2, O2>)=>void mapFn) {
		val newStream = new SubStream<I2, O2>
		stream.onChange [ entry |
			mapFn.apply(entry, newStream)
		]
		newStream.controls(stream)
		newStream as S => [ operation = 'transform' ]
	}
	
	/**
	 * Transform each item in the stream using the passed mappingFn
	 */
	def static <I, O, R> map(IStream<I, O> stream, (O)=>R mappingFn) {
		stream.map [ r, it | mappingFn.apply(it) ]
	}

	/**
	 * Transform each item in the stream using the passed mappingFn.
	 */
	def static <I, O, R> map(IStream<I, O> stream, (I, O)=>R mappingFn) {
		val newStream = new SubStream<I, R>
		stream.on [
			each [
				val mapped = mappingFn.apply($0, $1)
				newStream.push($0, mapped)
			]
			error [	newStream.error($0, $1) ]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream => [ operation = 'map' ]
	}
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	def static <I, O> filter(IStream<I, O> stream, (O)=>boolean filterFn) {
		stream.filter [ it, index, passed | filterFn.apply(it) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	def static <I, O> filter(IStream<I, O> stream, (O, Long, Long)=>boolean filterFn) {
		stream.filter [ r, it, index, passed | filterFn.apply(it, index, passed) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	def static <I, O> filter(IStream<I, O> stream, (I, O, Long, Long)=>boolean filterFn) {
		val newStream = new SubStream<I, O>
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)
		stream.on [
			each [
				val i = index.incrementAndGet
				if(filterFn.apply($0, $1, i, passed.get)) {
					passed.incrementAndGet
					newStream.push($0, $1)
				} else {
					stream.next
				}
			]
			error [	newStream.error($0, $1) ]
			finish [
				index.set(0)
				passed.set(0)
				newStream.finish($0, $1)
			]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		stream.operation = 'filter'
		newStream
	}
	
	/**
	 * Splits a stream into multiple parts. These parts are separated by Finish entries.
	 * <p>
	 * Streams support multiple levels of finishes, to indicate multiple levels of splits.
	 * This allows you to split a stream, and then split it again. Rules for splitting a stream:
	 * <ul>
	 * <li>when a new split is applied, it is always at finish level 0
	 * <li>all other stream operations that use finish, always use this level
	 * <li>the existing splits are all upgraded a level. so a level 0 finish becomes a level 1 finish
	 * <li>splits at a higher level always are carried through to a lower level. so wherever there is a
	 *     level 1 split for example, there is also a level 0 split
	 * </ul> 
	 */
	def static <I, O> split(IStream<I, O> stream, (O)=>boolean splitConditionFn) {
		val newStream = new SubStream<I, O>
		val skipping = new AtomicBoolean(false)
		val justPostedFinish0 = new AtomicBoolean(false)
		stream.on [
			each [
				if(skipping.get) {
					stream.next
				} else {
					if(splitConditionFn.apply($1)) {
						// apply multiple entries at once for a single next
						val entries = #[ new Value($0, $1), new Finish($0, 0) ]
						justPostedFinish0.set(true)
						newStream.apply(new Entries(entries))
					} else {
						justPostedFinish0.set(false)
						newStream.apply(new Value($0, $1))
					}
				}
			]
			error [ newStream.error($0, $1) ]
			finish [
				// stop skipping if it is a level 0 finish
				if($1 == 0) skipping.set(false)
				// a higher level split also splits up the lower level
				if(justPostedFinish0.get) {
					// we don't put finish(0)'s in a row
					newStream.apply(new Finish($0, $1 + 1))
				} else {
					justPostedFinish0.set(true)
					val entries = #[ new Finish($0, 0), new Finish($0, $1 + 1) ]
					newStream.apply(new Entries(entries))
				}
			]
			closed [ newStream.close ]
		]
		stream.operation = 'split'
		newStream.when [
			next [ stream.next ]
			close[ stream.close ]
			skip [ skipping.set(true) ]
		]
		newStream
	}
	
	/**
	 * Merges one level of finishes. 
	 * @see StreamExtensions.split
	 */
	def static <I, O> merge(IStream<I, O> stream) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.apply(new Value($0, $1)) ]
			error [ newStream.error($0, $1) ]
			finish [
				if($1 > 0)
					newStream.finish($0, $1 - 1)
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
	def static <I, O> limit(IStream<I, O> stream, int amount) {
		stream.until [ it, c | c > amount ] => [ stream.operation = 'limit(amount=' + amount + ')' ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <I, O> until(IStream<I, O> stream, (O)=>boolean untilFn) {
		stream.until [ it, index, passed | untilFn.apply(it) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <I, O> until(IStream<I, O> stream, (O, Long, Long)=>boolean untilFn) {
		val newStream = new SubStream<I, O>
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)	
		stream.on [
			each [
				val i = index.incrementAndGet
				if(untilFn.apply($1, i, passed.get)) {
					passed.incrementAndGet
					stream.skip
					stream.next
				} else {
					newStream.push($0, $1)
				}
			]
			error [ newStream.error($0, $1) ]
			finish [
				index.set(0)
				passed.set(0)
				newStream.finish($0, $1)
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
	def static <I, O> until(IStream<I, O> stream, (O, Long)=>boolean untilFn) {
		val count = new AtomicLong(0)
		val newStream = new SubStream<I, O>
		stream.on [
			each [
				if(untilFn.apply($1, count.incrementAndGet)) {
					stream.skip
					stream.next
				} else {
					newStream.push($0, $1)
				}
			]
			error [ newStream.error($0, $1) ]
			finish [ 
				count.set(0)
				newStream.finish($0, $1)
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
	def static <I, I2, O, S extends IStream<I2, O>> flatten(IStream<I, S> stream) {
		val newStream = new SubStream<I, O>
		stream.on [ 
			each [ r, s |
				s.on [
					each [ newStream.push(r, $1) s.next ]
					error [ newStream.error(r, $1) s.next ]
				]
				s.next
			]
			error [ newStream.error($0, $1) ]
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
	def static <I, O, R> flatMap(IStream<I, O> stream, (O)=>IStream<I, R> mapFn) {
		stream.map(mapFn).flatten => [ stream.operation = 'flatmap' ]
	}
	
	/**
	 * Keep count of how many items have been streamed so far, and for each
	 * value from the original stream, push a pair of count->value.
	 * Finish(0) resets the count.
	 */
	def static <I, O> index(IStream<I, O> stream) {
		val newStream = new SubStream<I, Pair<Integer, O>>
		val counter = new AtomicInteger(0)
		stream.on [
			each [ newStream.push($0, counter.incrementAndGet -> $1) ]
			error [ newStream.error($0, $1) ]
			finish [
				if($1 == 0) counter.set(0)
				newStream.finish($0, $1)
			]
			closed [ newStream.close ]
		]
		stream.operation = 'index'
		newStream.controls(stream)
		newStream
	}
	
	// SPLITTING //////////////////////////////////////////////////////////////
	
	/**
	 * Create a splitter StreamSource from a stream that lets you listen to the stream 
	 * with multiple listeners.
	 */
	def static <I, O> split(IStream<I, O> stream) {
		new StreamCopySplitter(stream)
	}

	/**
	 * Balance the stream into multiple listening streams.
	 */
	def static <T> balance(Stream<T> stream) {
		new LoadBalancer(stream)
	}

	// FLOW CONTROL ///////////////////////////////////////////////////////////

	/**
	 * Tell the stream what size its buffer should be, and what should happen in case
	 * of a buffer overflow.
	 */	
	def static <I, O> buffer(IStream<I, O> stream, int maxSize, (Entry<?, ?>)=>void onOverflow) {
		switch stream {
			BaseStream<I, O>: stream.maxBufferSize = maxSize
		}
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.when [
			next[ stream.next ]
			skip [ stream.skip ]
			close [ stream.close ]
			overflow [ onOverflow.apply(it) ]
		]
		stream.when [
			overflow [ onOverflow.apply(it) ]
		]
		newStream => [ operation = stream.operation ]
	}
	
	/**
	 * Only allows one value for every timeInMs milliseconds. All other values are dropped.
	 */
	def static <I, O> throttle(IStream<I, O> stream, Period period) {
		// -1 means allow, we want to allow the first incoming value
		val startTime = new AtomicLong(-1) 
		stream.filter [
			val now = System.currentTimeMillis
			if(startTime.get == -1 || now - startTime.get > period.ms) {
				// period has expired, reset the period and pass one
				startTime.set(now)
				true
			} else false
		] => [ stream.operation = 'throttle(period=' + period + ')' ]
	}
	
	/** 
	 * Only allows one value for every timeInMs milliseconds to pass through the stream.
	 * All other values are buffered, and dropped only after the buffer has reached a given size.
	 * This method requires a timer function, that takes a period in milliseconds, and that calls
	 * the passed procedure when that period has expired.
	 * <p>
	 * Since ratelimiting does not throw away values when the source is pushing data faster
	 * than can be processed, it can cause buffer overflow after some time.
	 * <p>
	 * You are strongly adviced to put a buffer statement before the ratelimit so you can 
	 * set the max buffer size and handle overflowing data, like this:
	 * <p>
	 * <pre>
	 * someStream
	 *    .buffer(1000) [ println('overflow error! handle this') ]
	 *    .ratelimit(100) [ period, timeoutFn, | vertx.setTimer(period, timeoutFn) ]
	 *    .onEach [ ... ]
	 * </pre>
	 * FIX: BREAKS ON ERRORS
	 */
	def static <I, O> ratelimit(IStream<I, O> stream, Period period, (Period)=>Task timerFn) {
		// check if we really need to ratelimit at all!
		if(period.ms <= 0) return stream
		// -1 means allow, we want to allow the first incoming value
		val lastNextMs = new AtomicLong(-1)
		val isTiming = new AtomicBoolean
		val newStream = new SubStream<I, O>
		stream.onChange [ newStream.apply(it) ]
		newStream.when [
			next [
				val now = System.currentTimeMillis
				// perform a next right now?
				if(lastNextMs.get == -1 || now - lastNextMs.get > period.ms) {
					lastNextMs.set(now)
					stream.next
				// or, if we are not already timing, set up a delayed next on the timerFn
				} else if(!isTiming.get) {
					// delay the next for the remaining time
					val delay = new Period(now + period.ms - lastNextMs.get)
					timerFn.apply(delay).then [
						isTiming.set(false)
						lastNextMs.set(System.currentTimeMillis)
						stream.next
					]
				}
			]
			skip [ stream.skip ]
			close [ stream.close ]
		]
		newStream => [ stream.operation = 'ratelimit(period=' + period + ')' ]
	}

	/**
	 * Incrementally slow down the stream on errors, and recovering to full speed once a normal value is processed.
	 * <p>
	 * When an error occurs, delay the next call by a given period, and if the next stream value again generates
	 * an error, multiply the period by 2 and wait that period. This way increasing the period
	 * up to a maximum period of one hour per error. The moment a normal value gets processed, the period is reset 
	 * to the initial period.
	 */
	def static <I, O> backoff(IStream<I, O> stream, Class<? extends Throwable> errorType, Period period, (Period)=>Task timerFn) {
		stream.backoff(errorType, period, 2, 1.hours, timerFn)
	}
	
	/**
	 * Incrementally slow down the stream on errors, and recovering to full speed once a normal value is processed.
	 * <p>
	 * When an error occurs, delay the next call by a given period, and if the next stream value again generates
	 * an error, multiply the period by the given factor and wait that period. This way increasing the period
	 * up to a maximum period that you pass. The moment a normal value gets processed, the period is reset to the
	 * initial period.
	 * 
	 * FIX: not working correctly!
	 */
	def static <I, O> backoff(IStream<I, O> stream, Class<? extends Throwable> errorType, Period period, int factor, Period maxPeriod, (Period)=>Task timerFn) {
		if(period.ms <= 0 || maxPeriod.ms <= 0) return stream
		val delay = new AtomicLong(period.ms)
		val newStream = new SubStream<I, O>
		stream.on [
			each [
				newStream.push($0, $1)
				delay.set(period.ms)
			]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
			error [
				// delay the error for the period
				timerFn.apply(new Period(delay.get)).then [ newStream.error($0, $1) ]
				// double the delay for a next error, up to the maxiumum
				val newDelay = Math.min(delay.get * factor, maxPeriod.ms)
				delay.set(newDelay)
			]
		]
		newStream.controls(stream)
		newStream => [ stream.operation = 'onErrorBackoff(period=' + period + ', factor=' + factor + ', maxPeriod=' + maxPeriod + ')' ]
	}

	/** 
	 * Splits a stream of values up into a stream of lists of those values, 
	 * where each new list is started at each period interval.
	 * <p>
	 * FIX: this can break the finishes, lists may come in after a finish.
	 * FIX: somehow ratelimit and window in a single stream do not time well together
	 */
	def static <I, O> window(IStream<I, O> stream, Period period, (Period)=>Task timerFn) {
		val newStream = new SubStream<I, List<O>>
		val list = new AtomicReference<List<O>>
		stream.on [
			each [
				if(list.get == null) {
					list.set(newLinkedList)
					timerFn.apply(period).then [
						newStream.push($0, list.getAndSet(null))
					]
				}
				list.get?.add($1)
				stream.next
			]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
			error [ newStream.error($0, $1) ]
		]
		newStream.controls(stream)
		newStream
	}
	
	/** 
	 * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
	 * <p>
	 * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
	 */
	def static <I, O> synchronizeWith(IStream<I, O> stream, IStream<?, ?> timerStream) {
		val newStream = new SubStream<I, O>
		timerStream.on [
			error [ newStream.error(null, $1) timerStream.next ]
			each [ 
				if(stream.open) stream.next else timerStream.close
				timerStream.next
			]
			closed [ stream.close ]
		]
		stream.on [
			each [ newStream.push($0, $1) ]
			finish [ newStream.finish($0, $1) ]
			error [ newStream.error($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.when [
			skip [ stream.skip ]
			close [ stream.close ]
		]
		stream.operation = 'forEvery'
		timerStream.next
		newStream
	}
	
	// RESOLVING //////////////////////////////////////////////////////////////
	
	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * It only asks the next promise from the stream when the previous promise has been resolved.  
	 */
	def static <I, I2, O> resolve(IStream<I, ? extends IPromise<I2, O>> stream) {
		stream.resolve(stream.concurrency) => [ stream.operation = 'resolve' ]
	}

	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * <p>
	 * Allows concurrent promises to be resolved in parallel.
	 * Passing a concurrency of 0 means all incoming promises will be called concurrently.
	 */
	def static <I, O> SubStream<I, O> resolve(IStream<I, ? extends IPromise<?, O>> stream, int concurrency) {
		val newStream = new SubStream<I, O>
		val isFinished = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)
		stream.on [

			// consider each incoming promise a new parallel process
			each [ r, promise |
				// listen for the process to complete
				promise
					// in case of a processing error, report it to the listening stream
					.on(Throwable) [ 
						newStream.error(r, new AsyncException('resolve', r, it))
						// are we done processing? and did we finish? then finish now 
						if(processes.decrementAndGet == 0 && isFinished.compareAndSet(true, false)) 
							newStream.finish(r)
					]
					// in case of a processing value, push it to the listening stream
					.then [ 
						// we are doing one less parallel process
						newStream.push(r, it)
						// are we done processing? and did we finish? then finish now 
						if(processes.decrementAndGet == 0 && isFinished.compareAndSet(true, false)) 
							newStream.finish(r)
					]
				// if we have space for more parallel processes, ask for the next value
				// concurrency of 0 is unlimited concurrency
				if(concurrency > processes.incrementAndGet || concurrency == 0) stream.next
			]

			// forward errors to the listening stream directly
			error [ newStream.error($0, $1) ]

			// if we finish, we only want to forward the finish when all processes are done
			finish [ 
				if(processes.get == 0) {
					// we are not parallel processing, you may inform the listening stream
					newStream.finish($0, $1)
				} else {
					// we are still busy, so remember to call finish when we are done
					isFinished.set(true)
				}
			]

			closed [ newStream.close ]
		]
		newStream.when [
			next [ stream.next ]
			// next [ if(concurrency > processes.incrementAndGet || concurrency == 0) stream.next ]
			skip [ stream.skip ]
			close [ stream.close ]
		]
		stream.operation = 'resolve(concurrency=' + concurrency + ')'
		newStream
	}
	
	// CALL ////////////////////////////////////////////////////////////////

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.call(stream.concurrency)
	 */	
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, (O)=>P promiseFn) {
		stream.call(stream.concurrency, promiseFn) => [ stream.operation = 'call' ]
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.call(stream.concurrency)
	 */	
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, (I, O)=>P promiseFn) {
		stream.call(stream.concurrency, promiseFn) => [ stream.operation = 'call' ]
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.map(mappingFn).resolve(concurrency)
	 */	
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, int concurrency, (O)=>P promiseFn) {
		stream.call(concurrency) [ i, o | promiseFn.apply(o) ]
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.map(mappingFn).resolve(concurrency)
	 */	
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, int concurrency, (I, O)=>P promiseFn) {
		stream.map(promiseFn).resolve(concurrency)
			=> [ stream.operation = 'call(concurrency=' + concurrency + ')' ]
	}

	// MONITORING ERRORS //////////////////////////////////////////////////////

	@Deprecated
	def static <I, O> onError(IStream<I, O> stream, (Throwable)=>void handler) {
		stream.on(Throwable, handler)
	}

	@Deprecated
	def static <I, O> onError(IStream<I, O> stream, (I, Throwable)=>void handler) {
		stream.on(Throwable, handler)
	}

	/** Catch errors of the specified type, call the handler, and pass on the error. */
	def static <I, O> on(IStream<I, O> stream, Class<? extends Throwable> errorType, (Throwable)=>void handler) {
		stream.on(errorType, false) [ in, it | handler.apply(it) ]
	}

	/** Catch errors of the specified type, call the handler, and pass on the error. */
	def static <I, O> on(IStream<I, O> stream, Class<? extends Throwable> errorType, (I, Throwable)=>void handler) {
		stream.on(errorType, false) [ in, it | handler.apply(in, it) ]
	}

	/** 
	 * Catch errors of the specified type coming from the stream, and call the handler with the error.
	 * If swallow is true, the error will be caught and not be passed on (much like you expect a normal Java catch to work).
	 */	
	def static <I, O> on(IStream<I, O> stream, Class<? extends Throwable> errorType, boolean swallow, (Throwable)=>void handler) {
		stream.on(errorType, swallow) [ in, it | handler.apply(it) ]
	}
	
	/** 
	 * Catch errors of the specified type coming from the stream, and call the handler with the error.
	 * If swallow is true, the error will be caught and not be passed on (much like you expect a normal Java catch to work).
	 */	
	def static <I, O> on(IStream<I, O> stream, Class<? extends Throwable> errorType, boolean swallow, (I, Throwable)=>void handler) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						handler.apply(from, err)
						if(!swallow) {
							newStream.error(from, err)
						} else {
							stream.next
						} 
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}
	
	// MAP ERRORS INTO ANOTHER ERROR //////////////////////////////////////////

	/** 
	 * Map an error to a new AsyncException with a message. 
	 * passing the value, and with the original error as the cause.
	 */
	def static <I, O> map(IStream<I, O> stream, Class<? extends Throwable> errorType, String message) {
		stream.effect(errorType) [ from, e | throw new AsyncException(message, from, e) ]
	}

	// TRANSFORM ERRORS INTO A SIDEEFFECT /////////////////////////////////////

	/** Catch errors of the specified type, call the handler, and swallow them from the stream chain. */
	def static <I, O> effect(IStream<I, O> stream, Class<? extends Throwable> errorType, (Throwable)=>void handler) {
		stream.on(errorType, true) [ in, it | handler.apply(it) ]
	}
	
	/** Catch errors of the specified type, call the handler, and swallow them from the stream chain. */
	def static <I, O> effect(IStream<I, O> stream, Class<? extends Throwable> errorType, (I, Throwable)=>void handler) {
		stream.on(errorType, true) [ in, it | handler.apply(in, it) ]
	}

	// TRANSFORM ERRORS INTO AN ASYNCHRONOUS SIDEEFFECT ////////////////////////

	def static <I, O, R, P extends IPromise<?, R>> perform(IStream<I, O> stream, Class<? extends Throwable> errorType, (Throwable)=>P handler) {
		stream.perform(errorType) [ i, e | handler.apply(e) ]
	}
	
	def static <I, O, R, P extends IPromise<?, R>> perform(IStream<I, O> stream, Class<? extends Throwable> errorType, (I, Throwable)=>P handler) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						handler.apply(from, err)
							.then [ stream.next ]
							.on(Throwable) [ newStream.error(from, it) ]
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	def static <I, O> map(IStream<I, O> stream, Class<? extends Throwable> errorType, (Throwable)=>O mappingFn) {
		stream.map(errorType) [ input, err | mappingFn.apply(err) ]
	}

	/** Map an error back to a value. Swallows the error. */
	def static <I, O> map(IStream<I, O> stream, Class<? extends Throwable> errorType, (I, Throwable)=>O mappingFn) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						val value = mappingFn.apply(from, err)
						newStream.push(from, value)
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <I, O> call(IStream<I, O> stream, Class<? extends Throwable> errorType, (Throwable)=>IPromise<?, O> mappingFn) {
		stream.call(errorType) [ input, err | mappingFn.apply(err) ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <I, O> call(IStream<I, O> stream, Class<? extends Throwable> errorType, (I, Throwable)=>IPromise<?, O> mappingFn) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						mappingFn.apply(from, err)
							.then [ newStream.push(from, it) ]
							.on(Throwable) [ newStream.error(from, it) ]
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}
		
	// ENDPOINTS //////////////////////////////////////////////////////////////

	/** 
	 * Start the stream by asking for the next value, and keep asking for the next value
	 * until the stream ends.
	 * @return a task that either contains an error if the stream had errors, or completes if the stream completes 
	 */
	def static <I, O> SubTask<I> start(IStream<I, O> stream) {
		val task = new SubTask<I>
		stream.on [
			each [ stream.next ]
			error [ task.error($0, $1) stream.next ]
			finish [
				if($1 == 0) task.complete($0)
				stream.next
			]
			closed [
				task.complete(null)
				stream.close
			]
		] 
		stream.operation = 'start'
		stream.next // this kicks off the stream
		task
	}
	

	@Deprecated	
	def static <I, O> onErrorThrow(IStream<I, O> stream) {
		stream.onErrorThrow('onErrorThrow')
	}

	@Deprecated	
	def static <I, O> onErrorThrow(IStream<I, O> stream, String message) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [	throw new UncaughtAsyncException(message, $0, $1) ]
			finish [ newStream.finish($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	/** Lets you respond to the closing of the stream */
	def static <I, O> onClosed(IStream<I, O> stream, (Void)=>void handler) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
			finish [ newStream.finish($0, $1) ]
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
	@Deprecated
	def static <I, O> SubTask<I> onEach(IStream<I, O> stream, (O)=>void handler) {
		stream.onEach [ r, it | handler.apply(it) ]
	}

	/** 
	 * Deprecated, instead use stream.effect[].start
	 * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * Returns a task that completes once the stream finishes or closes.
	 */
	@Deprecated
	def static <I, O> SubTask<I> onEach(IStream<I, O> stream, (I, O)=>void handler) {
		val task = new SubTask<I>
		stream.on [
			each [ handler.apply($0, $1) stream.next ]
			error [ task.error($0, $1) stream.next ]
			finish [
				if($1 == 0) task.set($0, true)
				stream.next
			]
			closed [ stream.close ]
		] 
		stream.operation = 'onEach'
		stream.next
		task
	}

	/**
	 * Deprecated, instead use stream.call[].start
	 * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * Performs the task for every value, and only requests the next value from the stream once the task has finished.
	 * Returns a task that completes once the stream finishes or closes.
	 */
	@Deprecated
	def static <I, I2, O, R, P extends IPromise<I2, R>> onEachCall(IStream<I, O> stream, (I, O)=>P taskFn) {
		stream.map(taskFn).resolve.onEach [
			// just ask for the next 
		] => [ stream.operation = 'onEachCall' ]
	}

	/**
	 * Deprecated, instead use stream.call[].start
	 * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
	 * Performs the task for every value, and only requests the next value from the stream once the task has finished.
	 * Returns a task that completes once the stream finishes or closes.
	 */
	@Deprecated
	def static <I, I2, O, R, P extends IPromise<I2, R>> onEachCall(IStream<I, O> stream, (O)=>P taskFn) {
		stream.map(taskFn).resolve.onEach [
			// just ask for the next 
		] => [ stream.operation = 'onEachCall' ]
	}

	/**
	 * Shortcut for splitting a stream and then performing a pipe to another stream.
	 * @return a CopySplitter source that you can connect more streams to. 
	 */
	def static <I, O> pipe(IStream<I, O> stream, IStream<I, ?> target) {
		stream.split.pipe(target)
			=> [ stream.operation = 'pipe' ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Closes the stream once it has the value or an error.
	  */
	def static <I, O> IPromise<I, O> first(IStream<I, O> stream) {
		val promise = new SubPromise<I, O>
		stream.on [
			each [ 
				if(!promise.fulfilled)
					promise.set($0, $1) 
				stream.close 
			]
			error [
				if(!promise.fulfilled)
					promise.error($0, $1) 
				 stream.close
			]
			finish [ from, e |
				promise.error(from, new Exception('Stream.first: stream finished without returning a value'))
				stream.close
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
	def static <I, O> last(IStream<I, O> stream) {
		val promise = new SubPromise<I, O>
		val last = new AtomicReference<O>
		stream.on [
			each [ 
				if(!promise.fulfilled) last.set($1) 
				stream.next
			]
			finish [
				if($1 == 0) {
					if(!promise.fulfilled && last.get != null) {
						promise.set($0, last.get)
						stream.close
					} else promise.error($0, new Exception('stream finished without passing a value, no last entry found.'))
				} else stream.next 
			]
			closed [ 
				if(!promise.fulfilled && last.get != null) {
					promise.set(null, last.get)
					stream.close
				}
			]
			error [	stream.next ]
		]
		stream.operation = 'last'
		stream.next
		promise
	}

	/**
	 * Skip an amount of items from the stream, and process only the ones after that.
	 * Resets at finish.
	 */
	def static <I, O> SubStream<I, O> skip(IStream<I, O> stream, int amount) {
		stream.filter [ it, index, passed | index > amount ] 
			=> [ stream.operation = 'skip(amount=' + amount + ')' ]
	}

	/**
	 * Take only a set amount of items from the stream. 
	 * Resets at finish.
	 */
	def static <I, O> take(IStream<I, O> stream, int amount) {
		stream.until [ it, index, passed | index > amount ]
			=> [ stream.operation = 'limit(amount=' + amount + ')' ]
	}
	
	/**
	 * Start the stream and and promise the first value from it.
	 */
	def static <I, O> then(IStream<I, O> stream, Procedure1<O> listener) {
		stream.first.then(listener)
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
	def static <I, O> on(IStream<I, O> stream, (StreamResponder<I, O>)=>void handlerFn) {
		stream.on [ s, builder | handlerFn.apply(builder) ]
	}

	def static <I, O> on(IStream<I, O> stream, (IStream<I, O>, StreamResponder<I, O>)=>void handlerFn) {
		val handler = new StreamResponder<I, O> => [
			it.stream = stream
			// by default, do nothing but call the next item from the stream
			each [ stream.next ]
			finish [ stream.next ]
			error [ stream.next ]
		]
		// observing first so the handlerFn is last and can issue stream commands
		stream.observe(handler) 
		handlerFn.apply(stream, handler)
		// return the stream so things can be applied
		stream
	}

	/**
	 * Convenient builder to easily respond to commands given to a stream.
	 * <p>
	 * Note: you can only have a single monitor for a stream.
	 * <p>
	 * Example: 
	 * <pre>
	 * stream.when [
	 *     next [ println('next was called on the stream') ]
	 *     close [ println('the stream was closed') ]
	 * ]
	 * </pre>
	 */
	def static <I, O> when(IStream<I, O> stream, (StreamEventResponder)=>void handlerFn) {
		val handler = new StreamEventResponder
		handlerFn.apply(handler)
		stream.handle(handler)
		stream
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////

	/**
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <I, O> check(IStream<I, O> stream, String checkDescription, (O)=>boolean checkFn) {
		stream.check(checkDescription) [ from, it | checkFn.apply(it) ]
	}

	/** 
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <I, O> check(IStream<I, O> stream, String checkDescription, (I, O)=>boolean checkFn) {
		stream.effect [ from, it |
			if(!checkFn.apply(from, it)) throw new AssertionException(
			'stream.check ("' + checkDescription + '") failed for checked value: ' + it + ' and stream input: ' + from)
		]
	}

	/** Perform some side-effect action based on the stream. */
	def static <I, O> effect(IStream<I, O> stream, (O)=>void listener) {
		stream.effect [ r, it | listener.apply(it)]
	}

	/** Perform some side-effect action based on the stream. */
	def static <I, O> effect(IStream<I, O> stream, (I, O)=>void listener) {
		stream.map [ r, it |
			listener.apply(r, it)
			it
		] => [ stream.operation = 'effect' ]
	}

	/** Perform some side-effect action based on the stream. */
	def static <I, O> perform(IStream<I, O> stream, (O)=>IPromise<?,?> promiseFn) {
		stream.perform(stream.concurrency, promiseFn)
	}

	/** Perform some side-effect action based on the stream. */
	def static <I, O> perform(IStream<I, O> stream, (I, O)=>IPromise<?,?> promiseFn) {
		stream.perform(stream.concurrency, promiseFn)
	}

	/** 
	 * Perform some asynchronous side-effect action based on the stream.
	 * Perform at most 'concurrency' calls in parallel.
	 */
	def static <I, O> perform(IStream<I, O> stream, int concurrency, (O)=>IPromise<?, ?> promiseFn) {
		stream.perform(concurrency) [ i, o | promiseFn.apply(o) ]
	}

	/** 
	 * Perform some asynchronous side-effect action based on the stream.
	 * Perform at most 'concurrency' calls in parallel.
	 */
	def static <I, O> perform(IStream<I, O> stream, int concurrency, (I, O)=>IPromise<?,?> promiseFn) {
		stream.call(concurrency) [ i, o | promiseFn.apply(i, o).map [ o ] ]
			=> [ stream.operation = 'perform(concurrency=' + concurrency + ')' ]
	}

	// REVERSE AGGREGATIONS ///////////////////////////////////////////////////
	
	/** 
	 * Opposite of collect, separate each list in the stream into separate
	 * stream entries and streams those separately.
	 */
	def static <I, O> SubStream<I, O> separate(IStream<I, List<O>> stream) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ r, list |
				// apply multiple entries at once for a single next
				val entries = list.map [ new Value(r, it) ]
				newStream.apply(new Entries(entries))
			]
			error [ newStream.error($0, $1) ]
			finish [ newStream.finish($0, $1) ]
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
	def static <I, O> collect(IStream<I, O> stream) {
		val SubStream<I, List<O>> s = stream.reduce(newArrayList) [ list, it | list.concat(it) ]
		stream.operation = 'collect'
		s
	}

	/**
	 * Concatenate a lot of strings into a single string, separated by a separator string.
	 * <pre>
	 * (1..3).stream.join('-').then [ println(it) ] // prints 1-2-3
	 */	
	def static <I, O> join(IStream<I, O> stream, String separator) {
		stream.reduce('') [ acc, it | acc + (if(acc != '') separator else '') + it.toString ]
			=> [ stream.operation = 'join' ]
	}

	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	def static <I, O extends Number> sum(IStream<I, O> stream) {
		stream.reduce(0D) [ acc, it | acc + doubleValue ]
			=> [ stream.operation = 'sum' ]
	}

	/**
	 * Average the items in the stream until a finish.
	 */
	def static <I, O extends Number> average(IStream<I, O> stream) {
		stream
			.index
			.reduce(0 -> 0D) [ acc, it | key -> (acc.value + value.doubleValue) ]
			.map [ value / key ]
			=> [ stream.operation = 'average' ]
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	def static <I, O> count(IStream<I, O> stream) {
		stream.reduce(0) [ acc, it | acc + 1 ]
			=> [ stream.operation = 'count' ]
	}

	/**
	 * Gives the maximum value found on the stream.
	 * Values must implement Comparable
	 */
	def static <I, O extends Comparable<O>> max(IStream<I, O> stream) {
		val SubStream<I, O> s = stream.reduce(null) [ acc, it | if(acc != null && acc.compareTo(it) > 0) acc else it ]
		stream.operation = 'max'
		s
	}

	/**
	 * Gives the minimum value found on the stream.
	 * Values must implement Comparable
	 */
	def static <I, O extends Comparable<O>> min(IStream<I, O> stream) {
		val SubStream<I, O> s = stream.reduce(null) [ acc, it | if(acc != null && acc.compareTo(it) < 0) acc else it ]
		stream.operation = 'min'
		s
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed.
	 */
	def static <I, O, R> reduce(IStream<I, O> stream, R initial, (R, O)=>R reducerFn) {
		val reduced = new AtomicReference<R>(initial)
		val newStream = new SubStream<I, R>
		stream.on [
			each [
				try {
					reduced.set(reducerFn.apply(reduced.get, $1))
				} finally {
					stream.next
				}
			]
			finish [
				if($1 == 0) {
					val result = reduced.getAndSet(initial)
					if(result != null) {
						newStream.push($0, result)
					} else {
						stream.next
					}
				} else {
					newStream.finish($0, $1 - 1)
				}
			]
			error [
				// on errors, skip the whole reduction and propagate an error
				// println('error! skipping the stream ' + $1.message)
				reduced.set(null)
				stream.skip
				newStream.error($0, $1)
			]
			closed [ newStream.close ]
		]
		stream.operation = 'reduce(initial=' + initial + ')'
		newStream.controls(stream)
		newStream
	}

	def static <I, O, R> scan(IStream<I, O> stream, R initial, (R, O)=>R reducerFn) {
		stream.scan(initial) [ p, r, it | reducerFn.apply(p, it) ]
	}
	
	def static <I, O, R> scan(IStream<I, O> stream, R initial, (R, I, O)=>R reducerFn) {
		val reduced = new AtomicReference<R>(initial)
		val newStream = new SubStream<I, R>
		stream.on [
			each [
				val result = reducerFn.apply(reduced.get, $0, $1)
				reduced.set(result)
				if(result != null) newStream.push($0, result)
				stream.next
			]
			finish [
				reduced.set(initial) 
				newStream.finish($0, $1)
			]
			error [ newStream.error($0, $1) ]
			closed [ newStream.close ]
		]
		stream.operation = 'scan(initial=' + initial + ')'
		newStream.controls(stream)
		newStream
	}

	/**
	 * Streams true if all stream values match the test function
	 */
	def static <I, O> all(IStream<I, O> stream, (O)=>boolean testFn) {
		val s = stream.reduce(true) [ acc, it | acc && testFn.apply(it) ]
		stream.operation = 'all'
		s
	}

	/**
	 * Streams true if no stream values match the test function
	 */
	def static <I, O> none(IStream<I, O> stream, (O)=>boolean testFn) {
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
	def static <I, O> any(IStream<I, O> stream, (O)=>boolean testFn) {
		val anyMatch = new AtomicBoolean(false)
		val newStream = new SubStream<I, Boolean>
		stream.on [
			each [
				// if we get a match, we communicate directly and tell the stream we are done
				if(testFn.apply($1)) {	
					anyMatch.set(true)
					newStream.push($0, true)
					stream.skip
				}
				stream.next
			]
			finish [
				if($1 == 0) {
					val matched = anyMatch.get
					anyMatch.set(false)
					if(!matched) newStream.push($0, false)
				} else {
					newStream.finish($0, $1 - 1)
				}
			]
			error [ newStream.error($0, $1) ]
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
	def static <I, O> first(IStream<I, O> stream, (O)=>boolean testFn) {
		stream.first [ r, it | testFn.apply(it) ]
	}

	/**
	 * Streams the first value that matches the testFn
	 * <p>
	 * Note that this is not a normal reduction, since no finish is needed to fire a value.
	 * The moment testFn gives off true, the value is streamed and the rest of the incoming 
	 * values are skipped.
	 */
	def static <I, O> first(IStream<I, O> stream, (I, O)=>boolean testFn) {
		val match = new AtomicReference<O>
		val newStream = new SubStream<I, O>
		stream.on [
			each [
				// if we get a match, we communicate directly and tell the stream we are done
				if(testFn.apply($0, $1)) {	
					match.set($1)
					newStream.push($0, $1)
					stream.skip
				}
				stream.next
			]
			finish [
				if($1 == 0) {
					match.set(null)
				} else {
					newStream.finish($0, $1 - 1)
				}
			]
			error [ newStream.error($0, $1) ]
			closed [ newStream.close ]
		]
		stream.operation = 'first'
		newStream.controls(stream)
		newStream
	}

	/** 
	 * Returns an atomic reference which updates as new values come in.
	 * This way you can always get the latest value that came from the stream.
	 * @return the latest value, self updating. may be null if no value has yet come in.
	 */
	def static <I, O> latest(IStream<I, O> stream) {
		val value = new AtomicReference<O>
		stream.latest(value).onEach [
			// do nothing
		]
		value
	}
	
	/**
	 * Keeps an atomic reference that you pass updated with the latest values
	 * that comes from the stream.
	 */
	def static <I, O> latest(IStream<I, O> stream, AtomicReference<O> latestValue) {
		val newStream = new SubStream<I, O>
		stream.on [
			each [ latestValue.set($1) newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
			finish [ newStream.finish($0, $1) ]
		]
		stream.operation = 'latest'
		newStream.controls(stream)
		newStream
	}
	
	// MONITORING ///////////////////////////////////////////////////////////////

	def static <I, O> monitor(IStream<I, O> stream, StreamMonitor monitor) {
		stream.monitor(stream.operation, monitor)
	}
	
	def static <I, O> monitor(IStream<I, O> stream, String name, StreamMonitor monitor) {
		val stats = new StreamStats
		monitor.add(name, stats)
		stream.monitor(stats)
	}

	def static <I, O> monitor(IStream<I, O> stream, StreamStats stats) {
		val splitter = stream.split
		splitter.stream.on [ extension builder |

			stats.startTS = now

			each [ from, value |
				stats => [
					if(firstEntryTS == 0) firstEntryTS = now
					if(firstValueTS == 0) firstValueTS = now
					lastEntryTS = now
					lastValueTS = now
					lastValue = value
					valueCount = valueCount + 1
				]
				builder.stream.next
			]
			
			error [ from, t |
				stats => [
					if(firstEntryTS == 0) firstEntryTS = now
					if(firstErrorTS == 0) firstErrorTS = now
					lastEntryTS = now
					lastErrorTS = now
					lastError = t
					errorCount = errorCount + 1
				]
				builder.stream.next
			]
			
			finish [ from, t |
				stats => [
					if(firstEntryTS == 0) firstEntryTS = now
					if(firstFinishTS == 0) firstFinishTS = now
					lastEntryTS = now
					lastFinishTS = now
					finishCount = finishCount + 1
				]
				builder.stream.next
			]
			
			closed [
				stats => [
					lastEntryTS = now
					closeTS = now
				]
			]
			
			builder.stream.next
		]

		splitter.stream
	}

	// OTHER //////////////////////////////////////////////////////////////////
	
	/** 
	 * Complete a task when the stream finishes or closes, 
	 * or give an error on the task when the stream gives an error.
	 */	
	def static void pipe(IStream<?, ?> stream, Task task) {
		stream.on [
			closed [ task.complete ]
			finish [ stream.close task.complete ]
			error [ stream.close task.error($1) ]
			each [ /* discard values */]
		]
		stream.operation = 'pipe'
	}

	def static Task toTask(IStream<?, ?> stream) {
		val task = new Task
		stream.pipe(task)
		task
	}
	
	// From Xtend-tools
	private def static <T> List<T> concat(Iterable<? extends T> list, T value) {
		if(value != null) ImmutableList.builder.add
		if(value != null) ImmutableList.builder.addAll(list).add(value).build
	}
	
	private def static long now() {
		System.currentTimeMillis
	}
}
