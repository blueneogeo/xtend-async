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
import nl.kii.async.options.AsyncOptions
import nl.kii.promise.IPromise
import nl.kii.promise.SubPromise
import nl.kii.promise.SubTask
import nl.kii.promise.Task
import nl.kii.stream.annotation.Hot
import nl.kii.stream.annotation.Push
import nl.kii.stream.annotation.Starter
import nl.kii.stream.annotation.Unsorted
import nl.kii.stream.message.Entries
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.StreamEvent
import nl.kii.stream.message.Value
import nl.kii.util.AssertionException
import nl.kii.util.Opt
import nl.kii.util.Period
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static extension com.google.common.io.ByteStreams.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.util.DateExtensions.*
import static extension nl.kii.util.OptExtensions.*
import static extension nl.kii.util.ThrowableExtensions.*
import nl.kii.stream.message.Closed

class StreamExtensions {
	
	// CREATION ///////////////////////////////////////////////////////////////
	
	/** Create a stream of the given type */
	@Pure
	def static <T> stream(Class<T> type) {
		new Stream<T>
	}
	
	/** stream the data of a map as a list of key->value pairs */
	@Pure
	def static <K, V> stream(Map<K, V> data) {
		data.entrySet.map [ key -> value ].stream
	}

	/** Create a stream of values out of a Promise of a list. If the promise throws an error,  */
	def static <I, O, L extends Iterable<O>> stream(IPromise<I, L> promise) {
		val newStream = new Stream<O>(promise.options)
		promise
			.then [	in, list | list.stream.pipe(newStream) ]
			.on(Throwable) [ in, e | newStream.error(e) ]
		newStream => [ options.operation = 'stream' ]
	}

	/** 
	 * Connect the output of one stream to another stream. 
	 * Retains flow control.
	 * @returns a task that completes onces the streams are closed.
	 */	
	def static <I, O> Task pipe(IStream<I, O> inputStream, Stream<O> outputStream) {
		val task = new Task(outputStream.options)
		inputStream.on [
			each [ in, it | outputStream.push(it) ]
			error [ in, e | outputStream.error(e) ]
			closed [ outputStream.close task.complete ]
		]
		outputStream.controls(inputStream)
		task => [ options.operation = 'pipe' ]
	}

	/** stream an list, ending with a finish. makes an immutable copy internally. */
	@Deprecated	
	def static <T> streamList(List<T> list) {
		list.iterator.stream
	}

	/** stream an interable, ending with a finish */	
	def static <T> Stream<T> stream(Iterable<T> iterable) {
		iterable.iterator.stream
	}
	
	/** stream an iterable, ending with a finish */
	def static <T> stream(Iterator<T> iterator) {
		val stream = new Stream<T>
		stream.when [
			next [
				if(iterator.hasNext) {
					stream.push(iterator.next)
				} else {
					stream.close
				}
			]
		]
		stream.options.operation = 'iterate'
		stream
	}
	
	/** Forwards commands given to the newStream directly to the parent. */
	def static <I1, I2, O1, O2> controls(IStream<I1, O2> newStream, IStream<I2, O1> parent) {
		newStream.when [
			next [ parent.next ]
			close [ parent.close ]
		]
	}
	
	/** Set the concurrency of the stream, letting you keep chaining by returning the stream. */
	def static <I, O> IStream<I, O> concurrency(IStream<I, O> stream, int value) {
		stream.options.concurrency = value
		stream
	}
	
	/** Modify the stream options for the above stream */
	def static <I, O> IStream<I, O> options(IStream<I, O> stream, (AsyncOptions)=>void optionsModifierFn) {
		optionsModifierFn.apply(stream.options)
		stream
	}
	
	
	/** stream a standard Java inputstream. closing the stream closes the inputstream. */
	@Push
	def static Stream<List<Byte>> stream(InputStream stream) {
		val newStream = new Stream<List<Byte>>
		val started = new AtomicBoolean
		newStream.when [
			next [
				// when next is called, start piping the data from the inputstream to the new stream
				// but only once
				if(!started.getAndSet(true)) {
					stream.readBytes(new ByteProcessor {
						
						override getResult() { newStream.close null }
						
						override processBytes(byte[] buf, int off, int len) throws IOException {
							if(!newStream.open) return false
							newStream.push(buf)
							true
						}
						
					})
				}
			]
			close [ stream.close ]
		]
		newStream
	}
	
	/** create an unending stream of random integers in the range you have given */
	def static streamRandom(IntegerRange range) {
		val randomizer = new Random
		val newStream = new Stream<Integer>
		newStream.when [
			next [
				if(newStream.open) {
					val next = range.start + randomizer.nextInt(range.size)
					newStream.push(next)
				}
			]
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
	def static <I, O> void setEntryHandler(IStream<I, O> stream, StreamEntryHandler<I, O> handler) {
		stream.onChange [ entry |
			switch it : entry {
				Value<I, O>: handler.onValue(from, value)
				Error<I, O>: handler.onError(from, error)
				Closed<I, O>: handler.onClosed
			}
		]
	}
	
	/** Listen to commands given to the stream. */
	def static <I, O> void setEventHandler(IStream<I, O> stream, StreamEventHandler handler) {
		stream.onEvent [ notification |
			switch notification {
				case next: handler.onNext
				case pause: handler.onPause
				case resume: handler.onResume
				case overflow: handler.onOverflow
				case close: handler.onClose
			}
		]
	}

	/** 
	 * Push any entries coming from the stream into an observer stream.
	 * The observer is being pushed to only, no other flow control.
	 */
	def static <I, O> IStream<I, O> addObserver(IStream<I, O> stream, SubStream<I, O> observer) {
		val newStream = new SubStream(stream)
		stream.onChange [ 
			observer.apply(it)
			newStream.apply(it)
		]
		newStream.controls(stream)
		newStream
	}

	/**
	 * Lets you observe a stream with a closure that gets a push-only observer stream.
	 * The observer is being pushed to only, no other flow control.
	 * <p>
	 * Example:
	 * <pre>
	 * (1..10).stream
	 *  .map['hi'+it]
	 * 	.observe [ collect.effect[println(it)].start ] // prints [hi1,hi2,hi3] when the stream is finished 
	 *  .pipe(otherStream)
	 * <pre>
	 */
	def static <I, O> observe(IStream<I, O> stream, (IStream<I, O>)=>void observeStreamFn) {
		val observer = new SubStream(stream)
		val newStream = stream.addObserver(observer)
		observeStreamFn.apply(observer)
		newStream
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
	def static <I, O> void on(IStream<I, O> stream, (StreamEntryResponder<I, O>)=>void handlerFn) {
		stream.on [ s, builder | handlerFn.apply(builder) ]
	}

	def static <I, O> void on(IStream<I, O> stream, (IStream<I, O>, StreamEntryResponder<I, O>)=>void handlerFn) {
		val handler = new StreamEntryResponder<I, O> => [
			it.stream = stream
			// by default, do nothing but call the next item from the stream
			each [ stream.next ]
			error [ stream.next ]
			closed [ stream.next ]
		]
		// observing first so the handlerFn is last and can issue stream commands
		stream.entryHandler = handler 
		handlerFn.apply(stream, handler)
	}

	/**
	 * Convenient builder to easily respond to commands given to a stream.
	 * Defaults for each action is to propagate it to the stream.
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
	def static <I, O> void when(IStream<I, O> stream, (StreamEventResponder)=>void handlerFn) {
		val handler = new StreamEventResponder
		handlerFn.apply(handler)
		stream.eventHandler = handler
	}

	/** Lets you respond to the closing of the stream */
	def static <I, O> onClosed(IStream<I, O> stream, (Void)=>void handler) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
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

	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Add a value to a stream */
	def static <T> >> (T input, Stream<T> stream) {
		stream.push(input)
		stream
	}
	
	/** Add a value to a stream */
	def static <T> << (Stream<T> stream, T input) {
		stream.push(input)
		stream
	}

	/** Add a list of values to a stream */
	def static <T> >> (List<T> values, Stream<T> stream) {
		for(value : values) stream.push(value)
		stream
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
	def static <I, O> >> (Throwable t, Stream<O> stream) {
		stream.apply(new Error<I, O>(null, t))
		stream
	}

	/** Lets you easily pass an Error<T> to the stream using the << operator */
	def static <I, O> << (Stream<O> stream, Throwable t) {
		stream.apply(new Error<I, O>(null, t))
		stream
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////

	/**
	 * Lets you take a stream and fully transform it into another stream (including input type).
	 * You need to provide a closure that takes an entry from the stream and gives you the result
	 * stream to modify with that entry.
	 * <p>
	 * You normally use a switch to decide how to act on each kind on entry, and then apply an operation
	 * on the passed stream based on that behavior.
	 */	
	def static <I, O, I2, O2, S extends IStream<I2, O2>> S transform(IStream<I, O> stream, (Entry<I, O>, SubStream<I2, O2>)=>void mapFn) {
		val newStream = new SubStream<I2, O2>(stream)
		stream.onChange [ entry | mapFn.apply(entry, newStream) ]
		newStream.controls(stream)
		newStream as S => [ options.operation = 'transform' ]
	}

	/** 
	 * Transform the input of a stream based on the existing input and output.
	 * <p>
	 * The input mapping function is passed both an input and an output parameter. The output
	 * is only available when a normal value comes in, and not for errors or finishes, in which
	 * case it is none.
	 */	
	def static <I, O, T> IStream<T, O> mapInput(IStream<I, O> stream, (I, Opt<O>)=>T inputMappingFn)	{
		val newStream = new SubStream<T, O>(stream)
		stream.on [
			each [ in, it | newStream.push(inputMappingFn.apply(in, some(it)), it) ]
			error [ in, e | newStream.error(inputMappingFn.apply(in, none), e) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	/** 
	 * Creates a new stream without the input data of the original stream.
	 */
	def static <T> IStream<T, T> removeInput(IStream<?, T> stream) {
		stream.mapInput [ in, out | out.orNull ]
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
	def static <I, O, R> IStream<I, R> map(IStream<I, O> stream, (I, O)=>R mappingFn) {
		val newStream = new SubStream<I, R>(stream)
		stream.on [
			each [
				val mapped = mappingFn.apply($0, $1)
				newStream.push($0, mapped)
			]
			error [	newStream.error($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		newStream => [ options.operation = 'map' ]
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
	 * returns a true for.
	 */
	def static <I, O> filter(IStream<I, O> stream, (I, O)=>boolean filterFn) {
		stream.filter [ from, it, index, passed | filterFn.apply(from, it) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	def static <I, O> filter(IStream<I, O> stream, (O, Long, Long)=>boolean filterFn) {
		stream.filter [ from, it, index, passed | filterFn.apply(it, index, passed) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	def static <I, O> IStream<I, O> filter(IStream<I, O> stream, (I, O, Long, Long)=>boolean filterFn) {
		val newStream = new SubStream(stream)
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
			closed [ newStream.close ]
		]
		newStream.controls(stream)
		stream.options.operation = 'filter'
		newStream
	}
		
	/** Disposes undefined optionals and continues the stream with the remaining optionals unboxed. */
	def static <I, O> filterDefined(IStream<I, Opt<O>> stream) {
		stream
			.filter [ defined ]
			.map [ value ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <I, O> until(IStream<I, O> stream, (O)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(out) ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <I, O> until(IStream<I, O> stream, (I, O)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(in, out) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	def static <I, O> until(IStream<I, O> stream, (I, O, Long)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(in, out, index) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * When this happens, the stream will finish. Any other encountered Finish is upgraded one level.
	 * @Param stream: the stream to process
	 * @Param inclusive: if true, the value that returned true in untilFn will also be streamed
	 * @Param finishesStream: if true, when the condition of untilFn is met, until will finish the stream with a finish(0) 
	 * @Param untilFn: a function that gets the stream value input, output, index and the amount of passed values so far.
	 * @Return a new substream that contains all the values up to the moment untilFn was called and an additional level 0 finish.
	 */
	def static <I, O> IStream<I, O> until(IStream<I, O> stream, (I, O, Long, Long)=>boolean untilFn) {
		val newStream = new SubStream(stream)
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)	
		stream.on [
			each [
				val i = index.incrementAndGet
				if(untilFn.apply($0, $1, i, passed.get)) {
					newStream.close
				} else {
					passed.incrementAndGet
					newStream.push($0, $1)
				}
			]
			error [ 
				newStream.error($0, $1)
			]
			closed [ 
				newStream.close
			]
		]
		stream.options.operation = 'until'
		newStream.controls(stream)
		newStream
	}
	
	 /**
	  * Skips the head and returns a stream of the trailing entries.
	  * Alias for Stream.skip(1) 
	  */
	def static <I, O> tail(IStream<I, O> stream) {
		stream.skip(1)
	}
	
	/**
	 * Skip an amount of items from the stream, and process only the ones after that.
	 */
	def static <I, O> IStream<I, O> skip(IStream<I, O> stream, int amount) {
		stream.filter [ it, index, passed | index > amount ] 
			=> [ stream.options.operation = 'skip(amount=' + amount + ')' ]
	}

	/**
	 * Take only a set amount of items from the stream. 
	 */
	def static <I, O> take(IStream<I, O> stream, int amount) {
		stream.until [ in, out, index, passed | index > amount ]
			=> [ stream.options.operation = 'take(amount=' + amount + ')' ]
	}
	

	/**
	 * Flatten a stream of streams into a single stream.
	 * This allows you to still collect data.
	 */
	@Hot
	@Unsorted
	def static <I, O, S extends IStream<I, O>> IStream<I, O> flatten(IStream<?, S> stream) {
		val newStream = new SubStream(stream)
		stream.on [ 
			each [ in, substream |
				substream.on [
					each [ in2, value | newStream.push(in2, value) substream.next ]
					error [ in2, error | newStream.error(in2, error) substream.next ]
					closed [ substream.next ]
				]
				substream.next
			]
			error [ in2, e | newStream.error(null, e) ]
			closed [ newStream.close ]
		]
		stream.options.operation = 'flatten'
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
		stream.map(mapFn).flatten => [ stream.options.operation = 'flatmap' ]
	}
	
	/**
	 * Keep count of how many items have been streamed so far, and for each
	 * value from the original stream, push a pair of count->value.
	 * Finish(0) resets the count.
	 */
	def static <I, O> IStream<I, Pair<Integer, O>> index(IStream<I, O> stream) {
		val newStream = new SubStream<I, Pair<Integer, O>>(stream)
		val counter = new AtomicInteger(0)
		stream.on [
			each [ newStream.push($0, counter.incrementAndGet -> $1) ]
			error [ newStream.error($0, $1) ]
			closed [ newStream.close ]
		]
		stream.options.operation = 'index'
		newStream.controls(stream)
		newStream
	}
	
	/** 
	 * Flatten a stream of lists of items into a stream of the items in those lists.
	 */
	def static <I, O> IStream<I, O> separate(IStream<I, List<O>> stream) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ r, list |
				// apply multiple entries at once for a single next
				val entries = list.map [ new Value(r, it) ]
				newStream.apply(new Entries(entries))
			]
			error [ newStream.error($0, $1) ]
			closed [newStream.close ]
		]
		stream.options.operation = 'separate'
		newStream.controls(stream)
		newStream
	}

	// FLOW CONTROL ///////////////////////////////////////////////////////////

	def static <I, O> buffer(IStream<I, O> stream, int maxSize) {
		switch stream {
			BaseStream<I, O>: stream.options.maxQueueSize = maxSize
		}
		stream
	}	

	/**
	 * Tell the stream what size its buffer should be, and what should happen in case
	 * of a buffer overflow.
	 */	
	def static <I, O> IStream<I, O> buffer(IStream<I, O> stream, int maxSize, =>void onOverflow) {
		switch stream {
			BaseStream<I, O>: stream.options.maxQueueSize = maxSize
		}
		val newStream = new SubStream(stream)
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
			closed [ newStream.close ]
		]
		newStream.when [
			next[ stream.next ]
			close [ stream.close ]
			overflow [ onOverflow.apply ]
		]
		stream.when [
			overflow [ onOverflow.apply ]
		]
		newStream
	}
	
	/** 
	 * Delay each entry coming through for a time period.
	 * Note: this will set concurrency to 1! (one entry at a time)
	 */
	def static <I, O> wait(IStream<I, O> stream, Period period, (Period)=>Task timerFn) {
		stream.perform(1) [ timerFn.apply(period) ]
	}
	
	/**
	 * Only allows one value for every timeInMs milliseconds. All other values are dropped.
	 */
	def static <I, O> throttle(IStream<I, O> stream, Period period) {
		// -1 means allow, we want to allow the first incoming value
		val startTime = new AtomicLong(-1) 
		stream.filter [ from, it |
			val now = System.currentTimeMillis
			if(startTime.get == -1 || now - startTime.get > period.ms) {
				// period has expired, reset the period and pass one
				startTime.set(now)
				true
			} else {
				// we are dismissing data from processing! report it as overflow
				stream.apply(StreamEvent.overflow)
				false
			}
		] => [ stream.options.operation = 'throttle(period=' + period + ')' ]
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
	def static <I, O> ratelimitOLD(IStream<I, O> stream, Period period, (Period)=>Task timerFn) {
		// check if we really need to ratelimit at all!
		if(period.ms <= 0) return stream
		// -1 means allow, we want to allow the first incoming value
		val lastNextSentMs = new AtomicLong(-1)
		val isTiming = new AtomicBoolean
		val newStream = new SubStream(stream)
		stream.onChange [ newStream.apply(it) ]
		newStream.when [
			next [
				val justNow = now
				// perform a next right now?
				if(lastNextSentMs.get == -1 || justNow - lastNextSentMs.get > period.ms) {
					lastNextSentMs.set(justNow)
					stream.next
				// or, if we are not already timing, set up a delayed next on the timerFn
				} else if(!isTiming.get) {
					// delay the next for the remaining time
					val elapsed = justNow - lastNextSentMs.get
					val remaining = period.ms - elapsed
					isTiming.set(true)
					timerFn.apply(new Period(remaining)).then [
						isTiming.set(false)
						lastNextSentMs.set(now)
						stream.next
					]
				// if there is already a timer running
				}
			]
			close [ stream.close ]
			overflow [ stream.apply(StreamEvent.overflow) ]
		]
		newStream => [ stream.options.operation = 'ratelimit(period=' + period + ')' ]
	}
	
	def static <I, O> ratelimit(IStream<I, O> stream, Period period, (Period)=>Task timerFn) {
		val started = new AtomicBoolean(false)
		stream.perform(1) [
			if(started.compareAndSet(false, true)) complete
			else timerFn.apply(period)
		] as IStream<I, O>
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
		val newStream = new SubStream(stream)
		stream.on [
			each [
				newStream.push($0, $1)
				delay.set(period.ms)
			]
			error [
				// delay the error for the period
				timerFn.apply(new Period(delay.get)).then [ newStream.error($0, $1) ]
				// double the delay for a next error, up to the maxiumum
				val newDelay = Math.min(delay.get * factor, maxPeriod.ms)
				delay.set(newDelay)
			]
			closed [newStream.close ]
		]
		newStream.controls(stream)
		newStream => [ stream.options.operation = 'onErrorBackoff(period=' + period + ', factor=' + factor + ', maxPeriod=' + maxPeriod + ')' ]
	}

	/** 
	 * Splits a stream of values up into a stream of lists of those values, 
	 * where each new list is started at each period interval.
	 * <p>
	 * FIX: somehow ratelimit and window in a single stream do not time well together
	 */
	@Hot @Starter
	def static <I, O> IStream<I, List<O>> window(IStream<I, O> stream, Period period, (Period)=>Task timerFn) {
		val newStream = new SubStream<I, List<O>>(stream)
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
			error [ newStream.error($0, $1) ]
			closed [newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}
	
	/** 
	 * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
	 * <p>
	 * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
	 */
	@Hot @Starter
	def static <I, O> IStream<I, O> synchronizeWith(IStream<I, O> stream, IStream<?, ?> timerStream) {
		val newStream = new SubStream(stream)
		timerStream.on [
			error [ newStream.error(null, $1) timerStream.next ]
			each [ 
				if(stream.open) stream.next else timerStream.close
				timerStream.next
			]
			closed [stream.close ]
		]
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
			closed [newStream.close ]
		]
		newStream.when [
			close [ stream.close ]
		]
		stream.options.operation = 'forEvery'
		timerStream.next
		newStream
	}
	
	// RESOLVING //////////////////////////////////////////////////////////////
	
	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * It only asks the next promise from the stream when the previous promise has been resolved.  
	 */
	@Unsorted
	def static <I, I2, O> resolve(IStream<I, ? extends IPromise<I2, O>> stream) {
		stream.resolve(stream.options.concurrency) => [ stream.options.operation = 'resolve' ]
	}

	/** 
	 * Resolves a stream of processes, meaning it waits for promises to finish and return their
	 * values, and builds a stream of that.
	 * <p>
	 * Allows concurrent promises to be resolved in parallel.
	 * Passing a concurrency of 0 means all incoming promises will be called concurrently.
	 */
	@Unsorted
	def static <I, O> IStream<I, O> resolve(IStream<I, ? extends IPromise<?, O>> stream, int concurrency) {
		val newStream = new SubStream(stream)
		val isClosed = new AtomicBoolean(false)
		val processes = new AtomicInteger(0)
		val closeIfAllProcessesFinished = [|
			// close the new stream only when all async processes have finished
			if(processes.decrementAndGet == 0 && isClosed.compareAndSet(true, false)) {
				newStream.close
			} 
		]
		stream.on [
			// consider each incoming promise a new parallel process
			each [ r, promise |
				// listen for the process to complete
				promise
					// in case of a processing value, push it to the listening stream
					.then [ 
						newStream.push(r, it)
						closeIfAllProcessesFinished.apply
					]
					// in case of a processing error, report it to the listening stream
					.on(Throwable) [ 
						newStream.error(r, it)
						closeIfAllProcessesFinished.apply
					]
				// if we have space for more parallel processes, ask for the next value
				// concurrency of 0 is unlimited concurrency
				if(concurrency > processes.incrementAndGet || concurrency == 0) {
					stream.next
				}
			]

			// forward errors to the listening stream directly
			error [ newStream.error($0, $1) ]

			// only close the new stream when all processes are done
			closed [
				if(processes.get == 0) {
					// we are not parallel processing, you may inform the listening stream
					newStream.close
				} else {
					// we are still busy, so remember to call finish when we are done
					isClosed.set(true)
				}
			]
		]
		newStream.when [
			next [ stream.next ]
			// next [ if(concurrency > processes.incrementAndGet || concurrency == 0) stream.next ]
			close [ stream.close ]
		]
		stream.options.operation = 'resolve(concurrency=' + concurrency + ')'
		newStream
	}
	
	// CALL ////////////////////////////////////////////////////////////////

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.call(stream.concurrency)
	 */	
	@Unsorted
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, (O)=>P promiseFn) {
		stream.call(stream.options.concurrency, promiseFn) => [ stream.options.operation = 'call' ]
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.call(stream.concurrency)
	 */	
	@Unsorted
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, (I, O)=>P promiseFn) {
		stream.call(stream.options.concurrency, promiseFn)
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.map(mappingFn).resolve(concurrency)
	 */	
	@Unsorted
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, int concurrency, (O)=>P promiseFn) {
		stream.call(concurrency) [ i, o | promiseFn.apply(o) ]
	}

	/**
	 * Make an asynchronous call.
	 * This is an alias for stream.map(mappingFn).resolve(concurrency)
	 */	
	@Unsorted
	def static <I, O, R, P extends IPromise<?, R>> call(IStream<I, O> stream, int concurrency, (I, O)=>P promiseFn) {
		stream.map(promiseFn).resolve(concurrency)
			=> [ stream.options.operation = 'call(concurrency=' + concurrency + ')' ]
	}

	// MONITORING ERRORS //////////////////////////////////////////////////////

	/** Catch errors of the specified type, call the handler, and pass on the error. */
	def static <T extends Throwable, I, O> on(IStream<I, O> stream, Class<T> errorType, (T)=>void handler) {
		stream.on(errorType, false) [ in, it | handler.apply(it as T) ]
	}

	/** Catch errors of the specified type, call the handler, and pass on the error. */
	def static <T extends Throwable, I, O> on(IStream<I, O> stream, Class<T> errorType, (I, T)=>void handler) {
		stream.on(errorType, false) [ in, it | handler.apply(in, it as T) ]
	}

	/** 
	 * Catch errors of the specified type coming from the stream, and call the handler with the error.
	 * If swallow is true, the error will be caught and not be passed on (much like you expect a normal Java catch to work),
	 * and the next value will be requested from the stream.
	 */	
	def static <T extends Throwable, I, O> on(IStream<I, O> stream, Class<T> errorType, boolean swallow, (T)=>void handler) {
		stream.on(errorType, swallow) [ in, it | handler.apply(it as T) ]
	}
	
	/** 
	 * Catch errors of the specified type coming from the stream, and call the handler with the error.
	 * If swallow is true, the error will be caught and not be passed on (much like you expect a normal Java catch to work),
	 * and the next value will be requested from the stream.
	 */	
	def static <T extends Throwable, I, O> IStream<I, O> on(IStream<I, O> stream, Class<T> errorType, boolean swallow, (I, T)=>void handler) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						handler.apply(from, err as T)
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
			closed [newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}
	
	// TRANSFORM ERRORS INTO A SIDEEFFECT /////////////////////////////////////

	/** Catch errors of the specified type, call the handler, and swallow them from the stream chain. */
	def static <T extends Throwable, I, O> effect(IStream<I, O> stream, Class<T> errorType, (T)=>void handler) {
		stream.on(errorType, true) [ in, it | handler.apply(it) ]
	}
	
	/** Catch errors of the specified type, call the handler, and swallow them from the stream chain. */
	def static <T extends Throwable, I, O> effect(IStream<I, O> stream, Class<T> errorType, (I, T)=>void handler) {
		stream.on(errorType, true) [ in, it | handler.apply(in, it) ]
	}

	// TRANSFORM ERRORS INTO AN ASYNCHRONOUS SIDEEFFECT ////////////////////////

	@Unsorted
	def static <T extends Throwable, I, O, R, P extends IPromise<?, R>> perform(IStream<I, O> stream, Class<T> errorType, (T)=>P handler) {
		stream.perform(errorType) [ i, e | handler.apply(e) ]
	}
	
	@Unsorted
	def static <T extends Throwable, I, O, R, P extends IPromise<?, R>> IStream<I, O> perform(IStream<I, O> stream, Class<T> errorType, (I, T)=>P handler) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						handler.apply(from, err as T)
							.then [ stream.next ]
							.on(Throwable) [ newStream.error(from, it) ]
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			closed [newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	def static <T extends Throwable, I, O> map(IStream<I, O> stream, Class<T> errorType, (T)=>O mappingFn) {
		stream.map(errorType) [ input, err | mappingFn.apply(err) ]
	}

	/** Map an error back to a value. Swallows the error. */
	def static <T extends Throwable, I, O> IStream<I, O> map(IStream<I, O> stream, Class<T> errorType, (I, T)=>O mappingFn) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						val value = mappingFn.apply(from, err as T)
						newStream.push(from, value)
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			closed [newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}

	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	@Unsorted
	def static <T extends Throwable, I, O> call(IStream<I, O> stream, Class<T> errorType, (T)=>IPromise<?, O> mappingFn) {
		stream.call(errorType) [ input, err | mappingFn.apply(err) ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	@Unsorted
	def static <T extends Throwable, I, O> IStream<I, O> call(IStream<I, O> stream, Class<T> errorType, (I, T)=>IPromise<?, O> mappingFn) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ newStream.push($0, $1) ]
			error [ from, err |
				try {
					if(err.matches(errorType)) {
						mappingFn.apply(from, err as T)
							.then [ newStream.push(from, it) ]
							.on(Throwable) [ newStream.error(from, it) ]
					} else {
						newStream.error(from, err)
					}
				} catch(Throwable t) {
					newStream.error(from, t)
				}
			]
			closed [newStream.close ]
		]
		newStream.controls(stream)
		newStream
	}
		
	// ENDPOINTS //////////////////////////////////////////////////////////////

	/** 
	 * Start the stream by asking for the next value, and keep asking for the next value
	 * until the stream ends. Errors do not break the stream.
	 * @return a task that either contains an error if the stream had errors, or completes if the stream completes.
	 * Note: It is a SubTask so you can use the input of the error in the task to find out which stream input 
	 * caused the error. 
	 */
	@Starter
	def static <I, O> SubTask<I> start(IStream<I, O> stream) {
		val task = new SubTask<I>(stream.options)
		stream.on [
			each [ stream.next ]
			error [ println('x') task.error($0, $1) stream.next ]
			closed [
				task.complete(null)
				stream.close
			]
		] 
		stream.next // this kicks off the stream
		task
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
		] => [ stream.options.operation = 'effect' ]
	}

	/** Perform some side-effect action based on the stream. */
	@Unsorted
	def static <I, O> perform(IStream<I, O> stream, (O)=>IPromise<?,?> promiseFn) {
		stream.perform(stream.options.concurrency, promiseFn)
	}

	/** Perform some side-effect action based on the stream. */
	@Unsorted
	def static <I, O> perform(IStream<I, O> stream, (I, O)=>IPromise<?,?> promiseFn) {
		stream.perform(stream.options.concurrency, promiseFn)
	}

	/** 
	 * Perform some asynchronous side-effect action based on the stream.
	 * Perform at most 'concurrency' calls in parallel.
	 */
	@Unsorted
	def static <I, O> perform(IStream<I, O> stream, int concurrency, (O)=>IPromise<?, ?> promiseFn) {
		stream.perform(concurrency) [ i, o | promiseFn.apply(o) ]
	}

	/** 
	 * Perform some asynchronous side-effect action based on the stream.
	 * Perform at most 'concurrency' calls in parallel.
	 */
	@Unsorted
	def static <I, O> perform(IStream<I, O> stream, int concurrency, (I, O)=>IPromise<?,?> promiseFn) {
		stream.call(concurrency) [ i, o | promiseFn.apply(i, o).map [ o ] ]
			=> [ stream.options.operation = 'perform(concurrency=' + concurrency + ')' ]
	}

	// AGGREGATIONS ///////////////////////////////////////////////////////////

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed. Starts the stream.
	 */
	@Starter
	def static <I, O, R> IPromise<R, R> reduce(IStream<I, O> stream, R initial, (R, O)=>R reducerFn) {
		stream.reduce(initial) [ r, i, o | reducerFn.apply(r, o) ]
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed. Starts the stream.
	 */
	@Starter
	def static <I, O, R> IPromise<R, R> reduce(IStream<I, O> stream, R initial, (R, I, O)=>R reducerFn) {
		val promise = new SubPromise<R, R>(stream.options)
		val reduced = new AtomicReference<R>(initial)
		stream.on [
			each [
				try {
					reduced.set(reducerFn.apply(reduced.get, $0, $1))
				} finally {
					stream.next
				}
			]
			error [
				reduced.set(null)
				promise.error(initial, $1)
			]
			closed [
				val result = reduced.get
				if(result != null) promise.set(initial, result)
				else if(initial != null) promise.set(initial, initial)
				else promise.error(initial, new Exception('no value came from the stream to reduce'))
			]
		]
		stream.next
		promise => [ options.operation = 'reduce(initial=' + initial + ')' ]
	}

	/**
	 * Promises a list of all items from a stream. Starts the stream.
	 */
	@Starter
	def static <I, O> collect(IStream<I, O> stream) {
		stream.reduce(newArrayList as List<O>) [ list, it | list.concat(it) ]
			=> [ options.operation = 'collect' ]
	}

	/**
	 * Concatenate a lot of strings into a single string, separated by a separator string.
	 * <pre>
	 * (1..3).stream.join('-').then [ println(it) ] // prints 1-2-3
	 */	
	@Starter
	def static <I, O> join(IStream<I, O> stream, String separator) {
		stream.reduce('') [ acc, it | acc + (if(acc != '') separator else '') + it.toString ]
			=> [ stream.options.operation = 'join' ]
	}

	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	@Starter
	def static <I, O extends Number> sum(IStream<I, O> stream) {
		stream.reduce(0D) [ acc, it | acc + doubleValue ]
			=> [ stream.options.operation = 'sum' ]
	}

	/**
	 * Average the items in the stream until a finish.
	 */
	@Starter
	def static <I, O extends Number> average(IStream<I, O> stream) {
		stream
			.index
			.reduce(0 -> 0D) [ acc, it | key -> (acc.value + value.doubleValue) ]
			.map [ value / key ]
			=> [ stream.options.operation = 'average' ]
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	@Starter
	def static <I, O> count(IStream<I, O> stream) {
		stream.reduce(0) [ acc, it | acc + 1 ]
			=> [ stream.options.operation = 'count' ]
	}

	/**
	 * Gives the maximum value found on the stream.
	 * Values must implement Comparable
	 */
	@Starter
	def static <I, O extends Comparable<O>> max(IStream<I, O> stream) {
		stream.reduce(null) [ Comparable<O> acc, it | if(acc != null && acc.compareTo(it) > 0) acc else it ]
			=> [ options.operation = 'max' ]
	}

	/**
	 * Gives the minimum value found on the stream.
	 * Values must implement Comparable
	 */
	@Starter
	def static <I, O extends Comparable<O>> min(IStream<I, O> stream) {
		stream.reduce(null) [ Comparable<O> acc, it | if(acc != null && acc.compareTo(it) < 0) acc else it ]
		 => [ options.operation = 'min ' ]
	}

	def static <I, O, R> IStream<I, R> scan(IStream<I, O> stream, R initial, (R, O)=>R reducerFn) {
		stream.scan(initial) [ p, r, it | reducerFn.apply(p, it) ]
	}
	
	def static <I, O, R> IStream<I, R> scan(IStream<I, O> stream, R initial, (R, I, O)=>R reducerFn) {
		val reduced = new AtomicReference<R>(initial)
		val newStream = new SubStream<I, R>(stream)
		stream.on [
			each [
				val result = reducerFn.apply(reduced.get, $0, $1)
				reduced.set(result)
				if(result != null) newStream.push($0, result)
				else stream.next
			]
			error [ 
				newStream.error($0, $1)
			]
			closed [
				newStream.close
			]
		]
		stream.options.operation = 'scan(initial=' + initial + ')'
		newStream.controls(stream)
		newStream
	}

	/**
	 * Promises true if all stream values match the test function.
	 * Starts the stream.
	 */
	@Starter
	def static <I, O> all(IStream<I, O> stream, (O)=>boolean testFn) {
		val s = stream.reduce(true) [ acc, it | acc && testFn.apply(it) ]
		stream.options.operation = 'all'
		s
	}

	/**
	 * Promises true if no stream values match the test function.
	 * Starts the stream.
	 */
	@Starter
	def static <I, O> none(IStream<I, O> stream, (O)=>boolean testFn) {
		val s = stream.reduce(true) [ acc, it | acc && !testFn.apply(it) ]
		stream.options.operation = 'none'
		s
	}

	/**
	 * Promises true if any of the values match the passed testFn.
	 * Starts the stream.
	 */
	@Starter
	def static <I, O> any(IStream<I, O> stream, (O)=>boolean testFn) {
		val promise = new SubPromise<I, Boolean>(stream.options)
		stream.on [
			each [
				if(testFn.apply($1)) {	
					promise.set($0, true)
					stream.close
				} else {
					stream.next
				}
			]
			error [ 
				promise.error($0, $1)
				stream.close
			]
			closed [
				promise.set(null, false)
			]
		]
		stream.next
		promise => [ options.operation = 'any' ]
	}

	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Alias for Stream.first
	  * Closes the stream once it has the value or an error.
	  */
	@Starter
	def static <I, O> IPromise<I, O> head(IStream<I, O> stream) {
		stream.first [ true ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Closes the stream once it has the value or an error.
	  */
	@Starter
	def static <I, O> IPromise<I, O> first(IStream<I, O> stream) {
		stream.first [ true ]
	}
	
	/**
	 * Promises the first value that matches the testFn.
	 * Starts the stream.
	 */
	@Starter
	def static <I, O> first(IStream<I, O> stream, (O)=>boolean testFn) {
		stream.first [ r, it | testFn.apply(it) ]
	}

	/**
	 * Promises the first value that matches the testFn.
	 * Starts the stream.
	 */
	@Starter
	def static <I, O> IPromise<I, O> first(IStream<I, O> stream, (I, O)=>boolean testFn) {
		val promise = new SubPromise<I, O>(stream.options)
		stream.on [
			each [
				if(testFn.apply($0, $1)) {	
					promise.set($0, $1)
					stream.close
				} else {
					stream.next
				}
			]
			error [ 
				promise.error($0, $1)
				stream.close
			]
			closed [
				if(!promise.fulfilled) promise.error(null, new Exception('StreamExtensions.first: no value was streamed before the stream was closed.'))
			]
		]
		stream.next
		promise => [ options.operation = 'first' ]
	}
	
	/**
	 * Start the stream and and promise the first value from it.
	 */
	@Starter
	def static <I, O> then(IStream<I, O> stream, Procedure1<O> listener) {
		stream.first.then(listener)
			=> [ stream.options.operation = 'then' ]
	}
	
	 /**
	  * Start the stream and promise the last value coming from the stream.
	  * Will keep asking next on the stream until it gets to the last value!
	  * Skips any stream errors, and closes the stream when it is done.
	  */
	@Starter
	def static <I, O> IPromise<I, O> last(IStream<I, O> stream) {
		val promise = new SubPromise<I, O>(stream.options)
		val last = new AtomicReference<Pair<I, O>>
		stream.on [
			each [ 
				if(!promise.fulfilled) last.set($0->$1) 
				stream.next
			]
			closed [
				if(!promise.fulfilled && last.get != null) {
					promise.set(last.get.key, last.get.value)
					stream.close
				} else promise.error(null, new Exception('stream closed without passing a value, no last entry found.'))
			]
			error [	stream.next ]
		]
		stream.next
		promise
	}

	/** 
	 * Returns an atomic reference which updates as new values come in.
	 * This way you can always get the latest value that came from the stream.
	 * Starts the stream.
	 * @return the latest value, self updating. may be null if no value has yet come in.
	 */
	@Starter
	def static <I, O> AtomicReference<O> latest(IStream<I, O> stream) {
		val value = new AtomicReference<O>
		stream.latest(value).next
		value
	}
	
	/**
	 * Keeps an atomic reference that you pass updated with the latest values
	 * that comes from the stream.
	 */
	def static <I, O> IStream<I, O> latest(IStream<I, O> stream, AtomicReference<O> latestValue) {
		val newStream = new SubStream(stream)
		stream.on [
			each [ latestValue.set($1) newStream.push($0, $1) ]
			error [ newStream.error($0, $1) ]
			closed [newStream.close ]
		]
		stream.options.operation = 'latest'
		newStream.controls(stream)
		newStream
	}
	
	// OTHER //////////////////////////////////////////////////////////////////
	
	/** 
	 * Complete a task when the stream finishes or closes, 
	 * or give an error on the task when the stream gives an error.
	 */	
	@Starter
	def static void completes(IStream<?, ?> stream, Task task) {
		stream.on [
			each [ /* discard values */]
			error [ stream.close task.error($1) ]
			closed [task.complete ]
		]
		stream.next
		stream.options.operation = 'pipe'
	}

	@Starter
	def static Task toTask(IStream<?, ?> stream) {
		val task = new Task(stream.options)
		stream.completes(task)
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
