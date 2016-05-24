package nl.kii.async.stream

import com.google.common.collect.Queues
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Queue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.ObservableOperation
import nl.kii.async.Observer
import nl.kii.async.annotation.Backpressure
import nl.kii.async.annotation.Cold
import nl.kii.async.annotation.Hot
import nl.kii.async.annotation.MultiThreaded
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Unsorted
import nl.kii.async.promise.Deferred
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import nl.kii.util.Opt

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*
import static extension nl.kii.util.ThrowableExtensions.*

final class StreamExtensions {

	// CREATION ////////////////////////////////////////////////////////////////////////////////

	/** 
	 * Create a stream out of an iterator. The iterator will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Backpressure
	def static <OUT> Stream<OUT, OUT> stream(Iterator<? extends OUT> iterator) {
		new Sink<OUT> {
			override onNext() {
				if(iterator.hasNext) {
					// value instead of push saves a call on the stacktrace
					val nextValue = iterator.next
					value(nextValue, nextValue)
				} else {
					complete
				}
			}
			override onClose() {
			}
		}
	}

	/** 
	 * Create a stream out of an iterator. The iterator will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Backpressure
	def static <OUT> Stream<OUT, OUT> stream(Iterable<? extends OUT> iterable) {
		iterable.iterator.stream
	}

	/**
	 * Create a stream out of a closure. Every time the stream calls for the next value,
	 * it will call the closure. If the closure returns null, the stream will complete.
	 */
	@Cold @Backpressure
	def static <OUT> Stream<OUT, OUT> stream(=>OUT nextValueFn) {
		new Sink<OUT> {
			
			override onNext() {
				// value instead of push saves a call on the stacktrace
				val nextValue = nextValueFn.apply
				if(nextValue != null) value(nextValue, nextValue) else complete
			}
			
			override onClose() {
				// do nothing
			}
			
		}
	}

	/** Create a stream sink of a certain type, without support for backpressure. */
	@Cold @Backpressure
	def static <OUT> Sink<OUT> sink(Class<OUT> type) {
		new Sink<OUT> {
			
			override onNext() {
				// do nothing, no support for backpressure
			}
			
			override onClose() {
				// do nothing
			}
			
		}
	}

	/** Push a list of values onto a stream. Will make sure this list is nicely iterated. */
	@Cold @Backpressure @Unsorted
	def static <OUT> void push(Source<OUT, OUT> source, List<OUT> values) {
		all(source, values.iterator.stream)
	} 

	/** 
	 * Merge multiple streams into one.
	 * If the incoming streams are hot, this will simply merge all values into the returned stream.
	 * If the incoming streams are cold and have backpressure, this stream will start with the first
	 * stream, and when it finishes, move onto the next. In effect it will concatinate the streams
	 * into a single stream.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> all(Stream<IN, OUT>... streams) {
		val currentStreamIndex = new AtomicInteger(0)
		val currentStream = new AtomicReference<Stream<IN, OUT>>(streams.head) 
		val mergedStream = new Source<IN, OUT> {
			
			override onNext() {
				currentStream.get.next
			}
			
			override onClose() {
				for(stream : streams) {
					stream.close
				}
				super.close
			}
			
		}
		for(stream : streams) {
			stream.observer = new Observer<IN, OUT> {
				
				override value(IN in, OUT value) {
					mergedStream.value(in, value)
				}
				
				override error(IN in, Throwable t) {
					mergedStream.error(in, t)
				}
				
				override complete() {
					// we are done with the current stream, move to the next
					currentStreamIndex.incrementAndGet
					// are we done with all streams?
					if(currentStreamIndex.get < streams.size) {
						// set the next stream as the current stream
						currentStream.set(streams.get(currentStreamIndex.get))
					} else {
						// all streams have completed, we are done
						mergedStream.complete
					}
				}
				
			}
		}
		mergedStream
	} 

	// STARTING ////////////////////////////////////////////////////////////////////////////////

	/** 
	 * Start the stream, by calling stream.next to request the first value, and next on every next value.
	 * If any error comes along the stream, the streaming stops. If you want to prevent this from happening,
	 * you need to catch the error within the stream chain.
	 * @return a task that either completes when the stream completes/closes, or that has an error.
	 */	
	@Hot @Backpressure
	def static <IN, OUT> Task start(Stream<IN, OUT> stream) {
		stream.asTask => [ stream.next ]
	}
	
	// SYNCHRONIZATION /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Wraps synchronize calls around a stream, making it thread-safe.
	 * This comes at a small performance cost.
	 */
	@Cold @Backpressure @MultiThreaded
	def static <IN, OUT> Stream<IN, OUT> synchronize(Stream<IN, OUT> stream) {
		val pipe = new Pipe<IN, OUT> {
			
			override synchronized isOpen() {
				stream.isOpen
			}
			
			override synchronized next() {
				stream.next
			}
			
			override synchronized pause() {
				stream.pause
			}
			
			override synchronized resume() {
				stream.resume
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			override synchronized value(IN in, OUT value) {
				pipe.value(in, value)
			}
			
			override synchronized error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			override synchronized complete() {
				pipe.complete
			}
			
		}
		pipe
	}
	
	// OPERATORS ///////////////////////////////////////////////////////////////////////////////
	
	/** Add a value to a stream */
	def static <T> >> (T input, Sink<T> sink) {
		sink.push(input)
		sink
	}
	
	/** Add a value to a stream */
	def static <T> << (Sink<T> sink, T input) {
		sink.push(input)
		sink
	}

	/** Add a list of values to a stream */
	def static <T> >> (List<T> values, Sink<T> sink) {
		for(value : values) sink.push(value)
		sink
	}
	
	/** Add a list of values to a stream */
	def static <T> << (Sink<T> sink, List<T> values) {
		for(value : values) sink.push(value)
		sink
	}

	/** Lets you easily pass an Error<T> to the stream using the >> operator */
	def static <I, O> >> (Throwable t, Sink<O> sink) {
		sink.push(t)
		sink
	}

	/** Lets you easily pass an Error<T> to the stream using the << operator */
	def static <I, O> << (Sink<O> sink, Throwable t) {
		sink.push(t)
		sink
	}
	
	// FORWARDING //////////////////////////////////////////////////////////////////////////////

	/** 
	 * Connect the output of one stream to another stream. Retains flow control. 
	 * @returns a task that completes once the streams are completed or closed
	 */
	@Cold @Backpressure
	def static <IN, OUT> Task pipe(Stream<IN, OUT> input, Source<IN, OUT> output) {
		val task = new Task
		input.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				output.value(in, value)
			}
			
			override error(IN in, Throwable t) {
				output.error(in, t)
			}
			
			override complete() {
				output.complete
				task.complete
			}
			
		}
		output.controllable = new Controllable {
			
			override next() {
				input.next
			}
			
			override pause() {
				input.pause
			}
			
			override resume() {
				input.resume
			}
			
			override close() {
				input.close
			}
			
		}
		task
	}

	/** 
	 * Connect the output of one stream to another stream. Retains flow control.
	 * The difference with pipe is that you lose the input. However that makes it
	 * easier to couple to other streams (since they do not need to match type).
	 * @returns a task that completes once the streams are completed or closed
	 */
	@Cold @Backpressure
	def static <IN, OUT> Task forward(Stream<IN, OUT> input, Source<?, OUT> output) {
		val task = new Task
		input.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				output.value(null, value)
			}
			
			override error(IN in, Throwable t) {
				output.error(null, t)
			}
			
			override complete() {
				output.complete
				task.complete
			}
			
		}
		output.controllable = new Controllable {
			
			override next() {
				input.next
			}
			
			override pause() {
				input.pause
			}
			
			override resume() {
				input.resume
			}
			
			override close() {
				input.close
			}
			
		}
		task
	}
	
	/** 
	 * Complete a task when the stream finishes or closes, 
	 * or give an error on the task when the stream gives an error.
	 */	
	@Hot
	def static <IN, OUT> void completes(Stream<IN, OUT> stream, Task task) {
		stream.asTask.completes(task)
		task.next
	}
	
	// ERROR HANDLING //////////////////////////////////////////////////////////////////////////

	@Cold @Backpressure
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> on(Stream<IN, OUT> stream, Class<ERROR> errorClass, boolean swallow, (IN, ERROR)=>void errorFn) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.onError(stream, pipe, errorClass, swallow, errorFn)
		pipe
	}	

	@Cold @Backpressure
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> on(Stream<IN, OUT> stream, Class<ERROR> errorClass, (ERROR)=>void errorFn) {
		stream.on(errorClass, false) [ in, error | errorFn.apply(error) ]
	}	

	@Cold @Backpressure
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> effect(Stream<IN, OUT> stream, Class<ERROR> errorClass, (ERROR)=>void errorFn) {
		stream.on(errorClass, true) [ in, error | errorFn.apply(error) ]
	}	

	@Cold @Backpressure
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> effect(Stream<IN, OUT> stream, Class<ERROR> errorClass, (IN, ERROR)=>void errorFn) {
		stream.on(errorClass, true, errorFn)
	}
	
	@Cold @Unsorted @Backpressure
	def static <ERROR extends Throwable, IN, OUT> perform(Stream<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>Promise<Object, Object> handler) {
		stream.perform(errorType) [ i, e | handler.apply(e) ]
	}
	
	@Cold @Unsorted @Backpressure
	def static <ERROR extends Throwable, IN, OUT> Stream<IN, OUT> perform(Stream<IN, OUT> stream, Class<ERROR> errorType, (IN, ERROR)=>Promise<Object, Object> handler) {
		val pipe = Pipe.connect(stream)
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				pipe.value(in, value)
			}
			
			override error(IN in, Throwable error) {
				try {
					if(error.matches(errorType)) {
						val promise = handler.apply(in, error as ERROR)
						promise.observer = new Observer<Object, Object> {
							
							override value(Object unused, Object value) {
								stream.next
							}
							
							override error(Object unused, Throwable t) {
								pipe.error(in, error)
							}
							
							override complete() {
								// do nothing
							}
							
						}
					} else {
						pipe.error(in, error)
					}
				} catch(Throwable t) {
					pipe.error(in, t)
				}
			}
			
			override complete() {
				pipe.complete
			}
			
		}
		pipe
	}

	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	@Cold @Backpressure
	def static <ERROR extends Throwable, IN, OUT> map(Stream<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>OUT mappingFn) {
		stream.map(errorType) [ input, err | mappingFn.apply(err) ]
	}

	/** Map an error back to a value. Swallows the error. */
	@Cold @Backpressure
	def static <ERROR extends Throwable, IN, OUT> Stream<IN, OUT> map(Stream<IN, OUT> stream, Class<ERROR> errorType, (IN, ERROR)=>OUT mappingFn) {
		val pipe = Pipe.connect(stream)
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				pipe.value(in, value)
			}
			
			override error(IN in, Throwable error) {
				try {
					if(error.matches(errorType)) {
						val value = mappingFn.apply(in, error as ERROR)
						pipe.value(in, value)
					} else {
						pipe.error(in, error)
					}
				} catch(Throwable t) {
					pipe.error(in, t)
				}
			}
			
			override complete() {
				pipe.complete
			}
			
		}
		pipe
	}

	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	@Cold @Backpressure
	def static <ERROR extends Throwable, IN, IN2, OUT> Stream<IN, OUT> call(Stream<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>Promise<IN2, OUT> onErrorPromiseFn) {
		stream.call(errorType) [ IN in, ERROR err | onErrorPromiseFn.apply(err) as Promise<Object, OUT> ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	@Cold @Backpressure
	def static <ERROR extends Throwable, IN, OUT, IN2> Stream<IN, OUT> call(Stream<IN, OUT> stream, Class<ERROR> errorType, (IN, ERROR)=>Promise<IN2, OUT> onErrorPromiseFn) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.onErrorCall(stream, pipe, errorType, onErrorPromiseFn)
		pipe
	}
	
	// OBSERVATION /////////////////////////////////////////////////////////////////////////////

	/** 
	 * Observe a stream with an observer, without touching the stream itself.
	 * Allows you to have more than one observer of a stream.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> observeWith(Stream<IN, OUT> stream, Observer<IN, OUT>... observers) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.observeWith(stream, #[pipe] + observers.toList)
		pipe
	}
	
	// BUFFERING ///////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Adds a buffer to the stream. Usually this is placed at the top of the stream, so
	 * everything in the chain below it gets buffered. When the chain cannot keep up with the data
	 * being thrown in at the top, the buffer will call pause on the stream and disregard any
	 * incoming values. When next is called from the bottom to indicate the stream can process again,
	 * the stream will resume the input and process from the buffer.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> buffer(Stream<IN, OUT> stream, int maxSize) {
		val Queue<Pair<IN, OUT>> buffer = Queues.newArrayDeque
		val ready = new AtomicBoolean(false)
		val completed = new AtomicBoolean(false)
		val pipe = new Pipe<IN, OUT> {
			
			override next() {
				// get the next value from the queue to stream
				val nextValue = buffer.poll
				if(nextValue != null) {
					// we have a buffered value, stream it
					ready.set(false)
					value(nextValue.key, nextValue.value)
					// we have one more slot in the queue now, so resume the stream if it was paused
					if(!stream.isOpen) stream.resume
				} else if(completed.get) {
					// the stream was completed, so now that all values were streamed, complete the pipe as well
					complete
				} else {
					// otherwise remember that we are ready to stream a value
					ready.set(true)
					// and ask for the next from the stream
					stream.next
				}
			}
			
			override isOpen() {
				stream.isOpen
			}
			
			override pause() {
				stream.pause
			}
			
			override resume() {
				stream.resume
				stream.next
			}
			
			override close() {
				super.close
				stream.close
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				if(buffer.empty && ready.compareAndSet(true, false)) {
					// if there is nothing in the buffer and the pipe is ready, push it out immediately
					pipe.value(in, value)
				} else {
					// if the buffer still has space
					if(buffer.size < maxSize) {
						// buffer the value
						buffer.add(in -> value)
						// if the buffer is now full, pause the stream
						if(buffer.size >= maxSize) {
							stream.pause
						}
						// if the pipe is ready for a value, push out the last value in the buffer queue
						if(ready.compareAndSet(true, false)) {
							val nextValue = buffer.poll
							pipe.value(nextValue.key, nextValue.value)
						}
					} else {
						// the stream is still pushing even though the buffer is full
						stream.pause
					}
				}
			}
			
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			override complete() {
				if(buffer.empty) {
					pipe.complete
				} else {
					completed.set(true)
				}
			}
			
		}
		pipe
	}
	
	// MAPPING AND EFFECT //////////////////////////////////////////////////////////////////////

	@Cold @Backpressure
	def static <IN, OUT, MAP> Stream<IN, MAP> map(Stream<IN, OUT> stream, (IN, OUT)=>MAP mapFn) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.map(stream, pipe, mapFn)
		pipe
	}
	
	@Cold @Backpressure
	def static <IN, OUT, MAP> Stream<IN, MAP> map(Stream<IN, OUT> stream, (OUT)=>MAP mapFn) {
		stream.map [ in, out | mapFn.apply(out) ]
	}

	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> effect(Stream<IN, OUT> stream, (OUT)=>void effectFn) {
		stream.map [ in, out | effectFn.apply(out) return out ]
	}

	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> effect(Stream<IN, OUT> stream, (IN, OUT)=>void effectFn) {
		stream.map [ in, out | effectFn.apply(in, out) return out ]
	}

	/** 
	 * Transform the input of a stream based on the existing input and output.
	 * <p>
	 * The input mapping function is passed both an input and an output parameter. The output
	 * is only available when a normal value comes in, and not for errors.
	 */	
	@Cold @Backpressure
	def static <IN1, IN2, OUT> Stream<IN2, OUT> mapInput(Stream<IN1, OUT> stream, (IN1, Opt<OUT>)=>IN2 inputMapFn) {
		val pipe = new Pipe<IN2, OUT> {

			override next() { 
				stream.next
			}

			override close() {
				super.close
				stream.close
			}
			
			override pause() {
				stream.pause
			}
			
			override resume() {
				stream.resume
			}
			
			override isOpen() {
				stream.isOpen
			}
			
		}
		ObservableOperation.mapInput(stream, pipe, inputMapFn)
		pipe
	}

	/** 
	 * Transform the input of a stream based on the existing input.
	 */	
	@Cold @Backpressure
	def static <IN1, IN2, OUT> Stream<IN2, OUT> mapInput(Stream<IN1, OUT> stream, (IN1)=>IN2 inputMapFn) {
		stream.mapInput [ in1, out | inputMapFn.apply(in1) ]
	}
	
	// FILTERING //////////////////////////////////////////////////////////////////////////////

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> filter(Stream<IN, OUT> stream, (IN, OUT, Long, Long)=>boolean filterFn) {
		val pipe = Pipe.connect(stream)
		val index = new AtomicLong(0)
		val passed = new AtomicLong(0)
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				val i = index.incrementAndGet
				if(filterFn.apply(in, value, i, passed.get)) {
					passed.incrementAndGet
					pipe.value(in, value)
				} else {
					stream.next
				}
			}
			
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			override complete() {
				pipe.complete
			}
			
		}
		pipe
	}
		
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	@Cold @Backpressure
	def static <IN, OUT> filter(Stream<IN, OUT> stream, (OUT)=>boolean filterFn) {
		stream.filter [ in, out, index, passed | filterFn.apply(out) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	@Cold @Backpressure
	def static <IN, OUT> filter(Stream<IN, OUT> stream, (IN, OUT)=>boolean filterFn) {
		stream.filter [ in, out, index, passed | filterFn.apply(in, out) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	@Cold @Backpressure
	def static <IN, OUT> filter(Stream<IN, OUT> stream, (OUT, Long, Long)=>boolean filterFn) {
		stream.filter [ in, out, index, passed | filterFn.apply(out, index, passed) ]
	}

	/** Disposes undefined optionals and continues the stream with the remaining optionals unboxed. */
	@Cold @Backpressure
	def static <IN, OUT> filterDefined(Stream<IN, Opt<OUT>> stream) {
		stream.filter [ defined ].map [ value ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	@Cold @Backpressure
	def static <IN, OUT> until(Stream<IN, OUT> stream, (OUT)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(out) ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	@Cold @Backpressure
	def static <IN, OUT> until(Stream<IN, OUT> stream, (IN, OUT)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(in, out) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	@Cold @Backpressure
	def static <IN, OUT> until(Stream<IN, OUT> stream, (IN, OUT, Long)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(in, out, index) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * When this happens, the stream will finish. Any other encountered Finish is upgraded one level.
	 * @Param stream: the stream to process
	 * @Param untilFn: a function that gets the stream value input, output, index and the amount of passed values so far.
	 * @Return a new substream that contains all the values up to the moment untilFn was called and an additional level 0 finish.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> until(Stream<IN, OUT> stream, (IN, OUT, Long, Long)=>boolean untilFn) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.until(stream, pipe, untilFn)
		pipe
	}
	
	 /**
	  * Skips the head and returns a stream of the trailing entries.
	  * Alias for Stream.skip(1) 
	  */
	@Cold @Backpressure
	def static <IN, OUT> tail(Stream<IN, OUT> stream) {
		stream.skip(1)
	}
	
	@Cold @Backpressure
	/**
	 * Skip an amount of items from the stream, and process only the ones after that.
	 */
	def static <IN, OUT> Stream<IN, OUT> skip(Stream<IN, OUT> stream, int amount) {
		stream.filter [ it, index, passed | index > amount ] 
	}

	@Cold @Backpressure
	/**
	 * Take only a set amount of items from the stream. 
	 */
	def static <IN, OUT> take(Stream<IN, OUT> stream, int amount) {
		stream.until [ in, out, index, passed | index > amount ]
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////////////////////	

	/**
	 * Keep count of how many items have been streamed so far, and for each
	 * value from the original stream, push a pair of count->value.
	 * Finish(0) resets the count.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, Pair<Integer, OUT>> index(Stream<IN, OUT> stream) {
		val pipe = Pipe.connect(stream)
		val counter = new AtomicInteger(0)
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				pipe.value(in, counter.incrementAndGet -> value)
			}
			
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			override complete() {
				pipe.complete
			}
			
		}
		pipe
	}
	
	/** 
	 * Flatten a stream of lists of items into a stream of the items in those lists.
	 */
	@Cold @Backpressure
	def static <IN, OUT> Stream<IN, OUT> separate(Stream<IN, List<OUT>> stream) {
		val ignoreNextCount = new AtomicInteger(0)
		val pipe = new Pipe<IN, OUT> {
			
			override next() {
				// ignore one less next until there is nothing left to ignore
				if(ignoreNextCount.decrementAndGet <= 0) {
					// stop ignoring and stream the next
					ignoreNextCount.set(0)
					stream.next
				}
			}
			
			override isOpen() {
				stream.isOpen
			}
			
			override pause() {
				stream.pause
			}
			
			override resume() {
				stream.resume
			}
			
			override close() {
				super.close
				stream.close
			}
			
		}
		stream.observer = new Observer<IN, List<OUT>> {
			
			override value(IN in, List<OUT> list) {
				// push all values in the list separately onto the stream
				// however we need to take into account that the listening
				// pipe will call next and that may not get the next list
				// from the stream. So we need to tell the pipe how many
				// times it should ignore 'next'. Once for every time
				// we push into the pipe, but we do want one next call at
				// the end, so one less than that
				ignoreNextCount.set(list.size - 1)
				for(value : list) {
					pipe.value(in, value)
				}
			}
			
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			override complete() {
				pipe.complete
			}
			
		}
		pipe
	}
	
	@Cold @Backpressure
	def static <IN, OUT, REDUCED> Stream<IN, REDUCED> scan(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, IN, OUT)=>REDUCED reducerFn) {
		val reduced = new AtomicReference<REDUCED>(initial)
		val newStream = Pipe.connect(stream)
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				val result = reducerFn.apply(reduced.get, in, value)
				reduced.set(result)
				if(result != null) newStream.value(in, result)
				else stream.next
			}
			
			override error(IN in, Throwable t) {
				newStream.error(in, t)
			}
			
			override complete() {
				newStream.complete
			}
			
		}
		newStream
	}
	
	@Cold @Backpressure
	def static <IN, OUT, REDUCED> Stream<IN, REDUCED> scan(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, OUT)=>REDUCED reducerFn) {
		stream.scan(initial) [ p, r, it | reducerFn.apply(p, it) ]
	}

	/** Flatten a stream of streams into a single stream. Expects streams to be ordered and preserves order, resolving one stream at a time. */
	@Cold @NoBackpressure
	def static <IN, IN2, OUT, STREAM extends Stream<IN2, OUT>> Stream<IN, OUT> flatten(Stream<IN, STREAM> stream) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.flatten(stream, pipe, 1)
		pipe
	}

	// CALL AND PERFORM ///////////////////////////////////////////////////////////////////////
	
	/** 
	 * Converts a stream of delayed values into a normal stream.
	 * If concurrency is set to other than 1, the stream will start pulling in next values
	 * automatically, making it a hot stream.
	 * @Param maxConcurrency: sets how many deferred processes get resolved in parallel. 0 for unlimited.
	 */
	@Cold @Unsorted @Backpressure
	def static <IN, OUT> Stream<IN, OUT> resolve(Stream<IN, ? extends Promise<?, OUT>> stream, int maxConcurrency) {
		val pipe = Pipe.connect(stream)
		ObservableOperation.flatten(stream, pipe, maxConcurrency)
		pipe
	}

	@Cold @Unsorted @Backpressure
	def static <IN, OUT, MAP> Stream<IN, MAP> call(Stream<IN, OUT> stream, int maxConcurrency, (IN, OUT)=>Promise<?, MAP> mapFn) {
		stream.map(mapFn).resolve(maxConcurrency)
	}

	@Cold @Unsorted @Backpressure
	def static <IN, OUT, MAP> Stream<IN, MAP> call(Stream<IN, OUT> stream, int maxConcurrency, (OUT)=>Promise<?, MAP> mapFn) {
		stream.call(maxConcurrency) [ in, out | mapFn.apply(out) ]
	}

	@Cold @Backpressure
	def static <IN, OUT, MAP> Stream<IN, MAP> call(Stream<IN, OUT> stream, (OUT)=>Promise<?, MAP> mapFn) {
		stream.call(1) [ in, out | mapFn.apply(out) ]
	}

	/** Perform some side-effect action based on the stream. */
	@Cold @Backpressure
	def static <IN, OUT, MAP> perform(Stream<IN, OUT> stream, (OUT)=>Promise<?, ?> promiseFn) {
		stream.call(1) [ in, value | promiseFn.apply(value).map [ value ] as Promise<?, Object> ]
	}

	/** Perform some side-effect action based on the stream. */
	@Cold @Backpressure
	def static <IN, OUT> perform(Stream<IN, OUT> stream, (IN, OUT)=>Promise<?, ?> promiseFn) {
		stream.call(1) [ in, value | promiseFn.apply(in, value).map [ value ] as Promise<?, Object> ]
	}

	/** 
	 * Perform some asynchronous side-effect action based on the stream.
	 * Perform at most 'concurrency' calls in parallel.
	 */
	@Cold @Unsorted @Backpressure
	def static <IN, OUT> perform(Stream<IN, OUT> stream, int concurrency, (OUT)=>Promise<?, ?> promiseFn) {
		stream.call(concurrency) [ in, value | promiseFn.apply(value).map [ value ] as Promise<?, Object> ]
	}

	/** 
	 * Perform some asynchronous side-effect action based on the stream.
	 * Perform at most 'concurrency' calls in parallel.
	 */
	@Cold @Unsorted @Backpressure
	def static <IN, OUT> Stream<IN, OUT> perform(Stream<IN, OUT> stream, int concurrency, (IN, OUT)=>Promise<?, ?> promiseFn) {
		stream.call(concurrency) [ in, value | promiseFn.apply(in, value).map [ value ] ]
	}
	
	// REDUCTION /////////////////////////////////////////////////////////////////////////////

	/** 
	 * Pulls in all the values from the stream, fails if the stream contains an error, and completes when the stream completes.
	 */
	@Cold
	def static <IN, OUT> Task asTask(Stream<IN, OUT> stream) {
		val task = new Task {
			
			val started = new AtomicBoolean(false)
			
			override next() {
				// allow this only once
				if(started.compareAndSet(false, true)) {
					stream.next
				}
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				// do nothing with the value, just ask for the next one
				stream.next
			}
			
			override error(IN in, Throwable t) {
				// stream.close
				task.error(t)
			}
			
			override complete() {
				// stream.close
				task.complete
			}
			
		}
		task
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed.
	 */
	@Hot
	def static <IN, OUT, REDUCED> Promise<Long, REDUCED> reduce(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, OUT)=>REDUCED reducerFn) {
		stream.reduce(initial) [ reduced, in, out | reducerFn.apply(reduced, out) ]	
	}
	
	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed.
	 * @return a promise that has the amount the amount of items that were processed as input, and the reduced value as output. 
	 */
	@Hot
	def static <IN, OUT, REDUCED> Promise<Long, REDUCED> reduce(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, IN, OUT)=>REDUCED reducerFn) {
		val promise = new Deferred<Long, REDUCED> {
			
			override next() {
				stream.next
			}
			
		}
		val reduced = new AtomicReference<REDUCED>(initial)
		val counter = new AtomicLong
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				try {
					counter.incrementAndGet
					reduced.set(reducerFn.apply(reduced.get, in, value))
				} finally {
					stream.next
				}
			}
			
			override error(IN in, Throwable t) {
				reduced.set(null)
				promise.error(counter.get, t)
			}
			
			override complete() {
				val result = reduced.get
				if(result != null) { 
					promise.value(counter.get, result)
				} else if(initial != null) {
					promise.value(counter.get, initial)
				} else {
					promise.error(counter.get, new Exception('no value came from the stream to reduce'))
				}
			}
			
		}
		stream.next
		promise
	}
	
	/**
	 * Promises a list of all items from a stream. Starts the stream.
	 */
	@Hot
	def static <IN, OUT> collect(Stream<IN, OUT> stream) {
		stream.reduce(newArrayList as List<OUT>) [ list, it | list.concat(it) ]
	}
	
	/**
	 * Promises a map of all inputs and outputs from a stream. Starts the stream.
	 */
	@Hot
	def static <IN, OUT> Promise<Long, Map<IN, OUT>> collectInOut(Stream<IN, OUT> stream) {
		stream.reduce(newHashMap as Map<IN, OUT>) [ list, in, out | list.put(in, out) list ]
	}

	/**
	 * Concatenate a lot of strings into a single string, separated by a separator string.
	 * <pre>
	 * (1..3).stream.join('-').then [ println(it) ] // prints 1-2-3
	 */	
	@Hot
	def static <IN, OUT> join(Stream<IN, OUT> stream, String separator) {
		stream.reduce('') [ acc, it | acc + (if(acc != '') separator else '') + it.toString ]
	}

	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	@Hot
	def static <IN, OUT extends Number> sum(Stream<IN, OUT> stream) {
		stream.reduce(0D) [ acc, it | acc + doubleValue ]
	}

	/**
	 * Average the items in the stream until a finish.
	 */
	@Hot
	def static <IN, OUT extends Number> average(Stream<IN, OUT> stream) {
		stream
			.index
			.reduce(0 -> 0D) [ acc, it | key -> (acc.value + value.doubleValue) ]
			.map [ value / key ]
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	@Hot
	def static <IN, OUT> count(Stream<IN, OUT> stream) {
		stream.reduce(0) [ acc, it | acc + 1 ]
	}

	/**
	 * Gives the maximum value found on the stream.
	 * Values must implement Comparable
	 */
	@Hot
	def static <IN, OUT extends Comparable<OUT>> max(Stream<IN, OUT> stream) {
		stream.reduce(null) [ Comparable<OUT> acc, it | if(acc != null && acc.compareTo(it) > 0) acc else it ]
	}

	/**
	 * Gives the minimum value found on the stream.
	 * Values must implement Comparable
	 */
	@Hot
	def static <IN, OUT extends Comparable<OUT>> min(Stream<IN, OUT> stream) {
		stream.reduce(null) [ Comparable<OUT> acc, it | if(acc != null && acc.compareTo(it) < 0) acc else it ]
	}

	@Hot
	def static <IN, OUT> all(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		stream.reduce(true) [ acc, it | acc && testFn.apply(it) ]
	}

	/**
	 * Promises true if no stream values match the test function.
	 * Starts the stream.
	 */
	@Hot
	def static <IN, OUT> none(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		stream.reduce(true) [ acc, it | acc && !testFn.apply(it) ]
	}

	/**
	 * Promises true if any of the values match the passed testFn.
	 * Starts the stream.
	 */
	@Hot
	def static <IN, OUT> any(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		val promise = new Deferred<IN, Boolean>
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				if(testFn.apply(value)) {	
					promise.value(in, true)
					stream.close
				} else {
					stream.next
				}
			}
			
			override error(IN in, Throwable t) {
				promise.error(in, t)
				stream.close
			}
			
			override complete() {
				promise.value(null, false)
			}
			
		}
		stream.next
		promise
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Alias for Stream.first
	  * Closes the stream once it has the value or an error.
	  */
	@Hot
	def static <IN, OUT> Promise<IN, OUT> head(Stream<IN, OUT> stream) {
		stream.first [ true ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Closes the stream once it has the value or an error.
	  */
	@Hot
	def static <IN, OUT> Promise<IN, OUT> first(Stream<IN, OUT> stream) {
		stream.first [ true ]
	}
	
	/**
	 * Promises the first value that matches the testFn.
	 * Starts the stream.
	 */
	@Hot
	def static <IN, OUT> first(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		stream.first [ r, it | testFn.apply(it) ]
	}

	/**
	 * Promises the first value that matches the testFn.
	 * Starts the stream.
	 */
	@Hot
	def static <IN, OUT> Promise<IN, OUT> first(Stream<IN, OUT> stream, (IN, OUT)=>boolean testFn) {
		val promise = new Deferred<IN, OUT> {
			
			override next() {
				stream.next
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				if(testFn.apply(in, value)) {	
					promise.value(in, value)
					stream.close
				} else {
					stream.next
				}
			}
			
			override error(IN in, Throwable t) {
				promise.error(in, t)
				stream.close
			}
			
			override complete() {
				if(!promise.fulfilled) {
					promise.error(null, new Exception('StreamExtensions.first: no value was streamed before the stream was closed.'))
				}
			}
			
		}
		stream.next
		promise
	}
	
	 /**
	  * Start the stream and promise the last value coming from the stream.
	  * Will keep asking next on the stream until it gets to the last value!
	  * Skips any stream errors, and closes the stream when it is done.
	  */
	@Hot
	def static <IN, OUT> Promise<IN, OUT> last(Stream<IN, OUT> stream) {
		val promise = new Deferred<IN, OUT> {
			
			override next() {
				stream.next
			}
			
		}
		val last = new AtomicReference<Pair<IN, OUT>>
		
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				if(!promise.fulfilled) last.set(in->value) 
				stream.next
			}
			
			override error(IN in, Throwable t) {
				stream.next
			}
			
			override complete() {
				if(!promise.fulfilled && last.get != null) {
					promise.value(last.get.key, last.get.value)
					stream.close
				} else promise.error(null, new Exception('stream closed without passing a value, no last entry found.'))
			}
			
		}
		stream.next
		promise
	}

	// ASSERTION ////////////////////////////////////////////////////////////////
	
	/**
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <IN, OUT> check(Stream<IN, OUT> stream, String checkDescription, (OUT)=>boolean checkFn) {
		stream.check(checkDescription) [ in, out | checkFn.apply(out) ]
	}

	/** 
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <IN, OUT> check(Stream<IN, OUT> stream, String checkDescription, (IN, OUT)=>boolean checkFn) {
		stream.effect [ in, out |
			if(!checkFn.apply(in, out)) throw new Exception(
			'stream.check ("' + checkDescription + '") failed for checked value: ' + out + '. Input was: ' + in)
		]
	}

}
