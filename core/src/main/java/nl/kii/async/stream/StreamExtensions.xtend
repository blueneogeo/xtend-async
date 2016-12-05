package nl.kii.async.stream

import co.paralleluniverse.fibers.SuspendExecution
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.fibers.instrument.DontInstrument
import com.google.common.collect.Queues
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Queue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Cold
import nl.kii.async.annotation.Controlled
import nl.kii.async.annotation.Hot
import nl.kii.async.annotation.Lossy
import nl.kii.async.annotation.MultiThreaded
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Uncontrolled
import nl.kii.async.annotation.Unsorted
import nl.kii.async.observable.ObservableOperation
import nl.kii.async.observable.Observer
import nl.kii.async.promise.Deferred
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import nl.kii.async.publish.BasicPublisher
import nl.kii.async.publish.Publisher
import nl.kii.util.Opt
import nl.kii.util.Period

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.util.IterableExtensions.*
import static extension nl.kii.util.OptExtensions.*
import static extension nl.kii.util.ThrowableExtensions.*

final class StreamExtensions {

	private new() { }

	// CREATION ////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * Deprecated: use Streams.newSink instead.
	 * <p>
	 * Create a stream sink of a certain type, without support for backpressure.
	 * This version without specifying the OUT type allows you to use the Xtend
	 * inferrer to infer the types for you. */
	@Cold @Controlled
	@Deprecated
	def static <OUT> Sink<OUT> newSink() {
		Streams.newSink
	}

	/** 
	 * Deprecated: use Streams.newSource instead.
	 * <p>
	 * Create a stream source of a certain type, without support for backpressure.
	 * This version without specifying IN and OUT types allows you to use the Xtend
	 * inferrer to infer the types for you.
	 */
	@Cold @Controlled
	@Deprecated
	def static <IN, OUT> Source<IN, OUT> newSource() {
		Streams.newSource
	}

	/** 
	 * Create a stream out of an iterator. The iterator will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> stream(Iterator<? extends OUT> iterator) {
		Streams.newStream(iterator)
	}

	/** 
	 * Create a stream out of an iterator. The iterator will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> each(Iterator<? extends OUT> iterator) {
		Streams.newStream(iterator)
	}

	/** 
	 * Create a stream out of an iterator. The iterator will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 * <p>
	 * This is an alias for StreamExtensions.stream(iterator). If you use Java8,
	 * the Collection.stream method overrides it, so this provides an alterative
	 * way of accessing it in Java8.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> each(Iterable<? extends OUT> iterable) {
		Streams.newStream(iterable)
	}

	/** 
	 * Create a stream out of an iterable. The iterable will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> stream(Iterable<? extends OUT> iterable) {
		Streams.newStream(iterable)
	}

	/**
	 * Deprecated: use Streams.newStream instead
	 * <p>
	 * Create a stream out of a closure. Every time the stream calls for the next value,
	 * it will call the closure. If the closure returns null, the stream will complete.
	 */
	@Deprecated
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> stream(=>OUT nextValueFn) {
		Streams.newStream(nextValueFn)
	}

	/**
	 * Deprecated: use Streams.newPeriodicStream instead.
	 * <p>
	 * Create a periodically emitting stream. The value in the stream is the count of the value, starting at 1.
	 * @param timerFn function that can be given a period and returns a task which completes after that period
	 * @param interval the period between values from the periodic stream
	 */
	 @Deprecated
	 @Cold @Controlled
	 def static <OUT> Stream<Long, Long> periodic((Period)=>Task timerFn, Period interval) {
	 	Streams.newPeriodicStream(timerFn, interval)
	}

	/**
	 * Deprecated: use Streams.newPeriodicStream instead.
	 * <p>
	 * Create a periodically emitting stream. The value in the stream is the count of the value, starting at 1.
	 * @param timerFn function that can be given a period and returns a task which completes after that period
	 * @param interval the period between values from the periodic stream
	 * @param maxAmount the maximum amount of values to emit
	 */
	 @Deprecated
	 @Cold @Controlled
	 def static <OUT> Stream<Long, Long> periodic((Period)=>Task timerFn, Period interval, int maxAmount) {
	 	Streams.newPeriodicStream(timerFn, interval, maxAmount)
	 }

	// STARTING ////////////////////////////////////////////////////////////////////////////////

	/** 
	 * Start the stream, by calling stream.next to request the first value, and next on every next value.
	 * If any error comes along the stream, the streaming stops. If you want to prevent this from happening,
	 * you need to catch the error within the stream chain.
	 * @return a task that either completes when the stream completes/closes, or that has an error.
	 */	
	@Hot @Controlled @Suspendable
	def static <IN, OUT> Task start(Stream<IN, OUT> stream) {
		val streamingTask = stream.asTask
		stream.next
		streamingTask
	}
	
	// CONCURRENCY /////////////////////////////////////////////////////////////////////////////

	/**
	 * This operator will try to pull the maxConcurrentProcesses amount of values from the stream
	 * at the same time (by calling stream.next). This way, you can set that you want that amount
	 * of values to be processed in parallel.
	 * <p>
	 * This only works with controlled streams, uncontrolled streams will behave as normal and simply
	 * push their values through.
	 */	
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> parallel(Stream<IN, OUT> stream, int maxConcurrentProcesses) {
		val processes = new AtomicInteger(1) // since we start a stream with next and we decrement on next, start on 1

		val pipe = new Pipe<IN, OUT> {

			@Suspendable
			override next() {
				// assumption: when we call next, we finished the previous process
				processes.decrementAndGet 
				stream.next
			}

			@Suspendable
			override void close() {
				super.close
				stream.close
			}
			
			@Suspendable
			override pause() {
				stream.pause
			}
			
			@Suspendable
			override resume() {
				stream.resume
				stream.next
			}
			
			@Suspendable
			override isOpen() {
				stream.isOpen
			}
			
		}
		
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				val open = processes.incrementAndGet
				pipe.value(in, value)
				if(open < maxConcurrentProcesses) stream.next
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			@Suspendable
			override complete() {
				pipe.complete
			}
			
		}

		pipe
	}	

	// SYNCHRONIZATION /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Wraps synchronize calls around a stream, making it thread-safe.
	 * This comes at a small performance cost.
	 */
	@Cold @Controlled @MultiThreaded //@DontInstrument
	def static <IN, OUT> Stream<IN, OUT> synchronize(Stream<IN, OUT> stream) {
		val pipe = new Pipe<IN, OUT> {
			
			@DontInstrument
			override synchronized isOpen() {
				stream.isOpen
			}
			
			@DontInstrument
			override synchronized next() {
				stream.next
			}
			
			@DontInstrument
			override synchronized pause() {
				stream.pause
			}
			
			@DontInstrument
			override synchronized resume() {
				stream.resume
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			@DontInstrument
			override synchronized value(IN in, OUT value) {
				pipe.value(in, value)
			}
			
			@DontInstrument
			override synchronized error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			@DontInstrument
			override synchronized complete() {
				pipe.complete
			}
			
		}
		pipe
	}
	
	// OPERATORS ///////////////////////////////////////////////////////////////////////////////
	
	/** Add a value to a stream */
	@Suspendable
	def static <IN> >> (IN input, Sink<IN> sink) {
		sink.push(input)
		sink
	}
	
	/** Add a value to a stream */
	@Suspendable
	def static <IN> << (Sink<IN> sink, IN input) {
		sink.push(input)
		sink
	}

	/** Add a list of values to a stream */
	@Suspendable
	def static <IN> >> (List<IN> values, Sink<IN> sink) {
		for(value : values) sink.push(value)
		sink
	}
	
	/** Add a list of values to a stream */
	@Suspendable
	def static <IN> << (Sink<IN> sink, List<IN> values) {
		for(value : values) sink.push(value)
		sink
	}
	
	/** Lets you easily pass an error to the stream using the >> operator */
	@Suspendable
	def static <IN> >> (Throwable t, Sink<IN> sink) {
		sink.push(t)
		sink
	}

	/** Lets you easily pass an error to the stream using the << operator */
	@Suspendable
	def static <IN> << (Sink<IN> sink, Throwable t) {
		sink.push(t)
		sink
	}

	// OBSERVATION /////////////////////////////////////////////////////////////////////////////

	/** 
	 * Lets you perform an operation on a stream, by creating a new pipe that you can write to,
	 * and that is configured to send control messages (next, pause, resume, isOpen, close)
	 * up to the original stream.
	 * <p>
	 * In order to create your new operation, implement the closure that takes the newly generated
	 * pipe. In your closure, you listen to the original stream by setting an observer to the
	 * stream, and based on incoming values, you write to the pipe.
	 * <p>
	 * The operation method returns the new pipe, cast into a Stream, so the receiver can not
	 * accidentally write to it.
	 * <p>
	 * Note: the output of your new stream does not have to be the same as the output of the original
	 * stream (but the input does). The OUT1 is from the original stream. The OUT2 is for the outgoing
	 * new pipe, and is inferred from what you do with the pipe by Xtend.
	 * 
	 * @param stream the stream to operate on
	 * @param operationFn a closure that passes a new Pipe that has been preconfigured to 
	 */
	@Cold @Controlled
	def static <IN, OUT1, OUT2> Stream<IN, OUT2> operation(Stream<IN, OUT1> stream, (Pipe<IN, OUT2>)=>void operationFn) {
		val pipe = new Pipe<IN, OUT2> {

			@Suspendable
			override next() { 
				stream.next
			}

			@Suspendable
			override void close() {
				super.close
				stream.close
			}
			
			@Suspendable
			override pause() {
				stream.pause
			}
			
			@Suspendable
			override resume() {
				stream.resume
				stream.next
			}
			
			@Suspendable
			override isOpen() {
				stream.isOpen
			}
			
		}
		operationFn.apply(pipe)
		pipe
	}

	/** 
	 * Observe a stream with an observer, without touching the stream itself.
	 * Allows you to have more than one observer of a stream.
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> observeWith(Stream<IN, OUT> stream, Observer<IN, OUT>... observers) {
		stream.operation [ pipe |
			ObservableOperation.observeWith(stream, #[pipe] + observers.toList)
		]
	}

	@Cold @Uncontrolled @NoBackpressure
	def static <IN, OUT> Publisher<OUT> publisher(Stream<IN, OUT> stream) {
		val publisher = new BasicPublisher<OUT> {
			
			@Suspendable
			override start() {
				super.start
				stream.next
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				publisher.publish(value)
				stream.next
			}
			
			@Suspendable
			override error(IN in, Throwable error) {
				publisher.publish(error)
				stream.next
			}
			
			@Suspendable
			override complete() {
				publisher.closeSubscriptions
				stream.close
			}
			
		}
		publisher
	} 
	
	// FORWARDING //////////////////////////////////////////////////////////////////////////////

	/** 
	 * Connect the output of one stream to another stream. Retains flow control. 
	 * @return a task that completes once the streams are completed or closed
	 */
	@Cold @Controlled
	def static <IN, OUT> Task pipe(Stream<IN, OUT> input, Source<IN, OUT> output) {
		val task = new Task
		input.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				output.value(in, value)
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				output.error(in, t)
			}
			
			@Suspendable
			override complete() {
				output.complete
				input.close
				task.complete
			}
			
		}
		output.controllable = new Controllable {
			
			@Suspendable
			override next() {
				input.next
			}
			
			@Suspendable
			override pause() {
				input.pause
			}
			
			@Suspendable
			override resume() {
				input.resume
			}
			
			@Suspendable
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
	 * @return a task that completes once the streams are completed or closed
	 */
	@Cold @Controlled
	def static <IN, OUT> Task pipe(Stream<IN, OUT> input, Sink<OUT> output) {
		val task = new Task
		input.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				output.value(null, value)
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				output.error(null, t)
			}
			
			@Suspendable
			override complete() {
				output.complete
				input.close
				task.complete
			}
			
		}
		output.controllable = new Controllable {
			
			@Suspendable
			override next() {
				input.next
			}
			
			@Suspendable
			override pause() {
				input.pause
			}
			
			@Suspendable
			override resume() {
				input.resume
			}
			
			@Suspendable
			override close() {
				input.close
			}
			
		}
		task
	}
	
	/** 
	 * Connect the output of one stream to the input of a pipe. 
	 * <p>
	 * Since the pipe you connect to cannot be controlled (you need a source for that),
	 * you lose backpressure and control, it simply forwards incoming values.
	 * @return a task that completes once the streams are completed or closed
	 */
	@Cold @Uncontrolled @NoBackpressure
	def static <IN, OUT> Task forward(Stream<IN, OUT> input, Pipe<IN, OUT> output) {
		val task = new Task
		input.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				output.value(null, value)
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				output.error(null, t)
			}
			
			@Suspendable
			override complete() {
				output.complete
				input.close
				task.complete
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
	
	// MERGING /////////////////////////////////////////////////////////////////////////////////
	
	/** Push a list of values onto a stream. Will make sure this list is nicely iterated. */
	@Cold @Controlled
	def static <OUT> void push(Source<OUT, OUT> source, List<? extends OUT> values) {
		merge(source, values.iterator.stream)
	} 

	/** Push a list of values onto a stream. Will make sure this list is nicely iterated. */
	@Cold @Controlled
	def static <OUT> void push(Source<OUT, OUT> source, Iterable<? extends OUT> values) {
		merge(source, values.stream)
	}
	
	/** 
	 * Merge multiple streams into one.
	 * If the incoming streams are hot, this will simply merge all values into the returned stream.
	 * If the incoming streams are cold and have backpressure, this stream will start with the first
	 * stream, and when it finishes, move onto the next. In effect it will concatinate the streams
	 * into a single stream.
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> merge(Stream<IN, OUT>... streams) {
		val currentStreamIndex = new AtomicInteger(0)
		val currentStream = new AtomicReference<Stream<IN, OUT>>(streams.head) 
		
		val target = new Source<IN, OUT> {
			
			@Suspendable
			override onNext() {
				currentStream.get.next
			}
			
			@Suspendable
			override onClose() {
				for(stream : streams) {
					stream.close
				}
			}
			
			@Suspendable
			override pause() {
				super.pause
				streams.forEach [ pause ]
			}
			
			@Suspendable
			override resume() {
				super.resume
				streams.forEach [ resume ]
			}
			
		}
		
		for(stream : streams) {
			stream.observer = new Observer<IN, OUT> {
				
				@Suspendable
				override value(IN in, OUT value) {
					target.value(in, value)
				}
				
				@Suspendable
				override error(IN in, Throwable t) {
					target.error(in, t)
				}
				
				@Suspendable
				override complete() {
					// we are done with the current stream, move to the next
					val newIndex = currentStreamIndex.incrementAndGet
					// are we done with all streams?
					if(newIndex < streams.size) {
						val nextStream = streams.get(newIndex)
						// set the next stream as the current stream
						currentStream.set(nextStream)
						// trigger next on the new current stream
						nextStream.next
					} else {
						// all streams have completed, we are done
						target.complete
						// make sure we close all merged streams
						for(stream: streams) { stream.close }
					}
				}
				
			}
		}
		
		target
	} 	
	
	// ERROR HANDLING //////////////////////////////////////////////////////////////////////////

	@Cold @Controlled
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> on(Stream<IN, OUT> stream, Class<ERROR> errorClass, boolean swallow, (IN, ERROR)=>void errorFn) {
		stream.operation [ pipe |
			ObservableOperation.onError(stream, pipe, errorClass, swallow, errorFn)
		]
	}

	@Cold @Controlled
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> on(Stream<IN, OUT> stream, Class<ERROR> errorClass, (ERROR)=>void errorFn) {
		stream.on(errorClass, false) [ in, error | errorFn.apply(error) ]
	}	

	@Cold @Controlled
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> on(Stream<IN, OUT> stream, Class<ERROR> errorClass, (IN, ERROR)=>void errorFn) {
		stream.on(errorClass, false, errorFn)
	}	

	@Cold @Controlled
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> effect(Stream<IN, OUT> stream, Class<ERROR> errorClass, (ERROR)=>void errorFn) {
		stream.on(errorClass, true) [ in, error | errorFn.apply(error) ]
	}	

	@Cold @Controlled
	def static <IN, OUT, ERROR extends Throwable> Stream<IN, OUT> effect(Stream<IN, OUT> stream, Class<ERROR> errorClass, (IN, ERROR)=>void errorFn) {
		stream.on(errorClass, true, errorFn)
	}
	
	@Cold @Unsorted @Controlled
	def static <ERROR extends Throwable, IN, OUT, PROMISE extends Promise<Object, Object>> perform(Stream<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>PROMISE handler) {
		stream.perform(errorType) [ i, e | handler.apply(e) ]
	}
	
	@Cold @Unsorted @Controlled
	def static <ERROR extends Throwable, IN, OUT, PROMISE extends Promise<Object, Object>> Stream<IN, OUT> perform(Stream<IN, OUT> stream, Class<ERROR> errorType, (IN, ERROR)=>PROMISE handler) {
		stream.operation [ pipe |
			stream.observer = new Observer<IN, OUT> {
				
				@Suspendable
				override value(IN in, OUT value) {
					pipe.value(in, value)
				}
				
				@Suspendable
				override error(IN in, Throwable error) {
					try {
						if(error.matches(errorType)) {
							val promise = handler.apply(in, error as ERROR)
							promise.observer = new Observer<Object, Object> {
								
								@Suspendable
								override value(Object unused, Object value) {
									stream.next
								}
								
								@Suspendable
								override error(Object unused, Throwable t) {
									pipe.error(in, error)
								}
								
								@Suspendable
								override complete() {
									// do nothing
								}
								
							}
						} else {
							pipe.error(in, error)
						}
					} catch(SuspendExecution suspend) {
						throw suspend						
					} catch(Throwable t) {
						pipe.error(in, t)
					}
				}
				
				@Suspendable
				override complete() {
					pipe.complete
				}
				
			}
		]
	}

	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	@Cold @Controlled
	def static <ERROR extends Throwable, IN, OUT> map(Stream<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>OUT mappingFn) {
		stream.map(errorType) [ input, err | mappingFn.apply(err) ]
	}

	/** Map an error back to a value. Swallows the error. */
	@Cold @Controlled
	def static <ERROR extends Throwable, IN, OUT> Stream<IN, OUT> map(Stream<IN, OUT> stream, Class<ERROR> errorType, (IN, ERROR)=>OUT mappingFn) {
		stream.operation [ pipe |
			stream.observer = new Observer<IN, OUT> {
				
				@Suspendable
				override value(IN in, OUT value) {
					pipe.value(in, value)
				}
				
				@Suspendable
				override error(IN in, Throwable error) {
					try {
						if(error.matches(errorType)) {
							val value = mappingFn.apply(in, error as ERROR)
							pipe.value(in, value)
						} else {
							pipe.error(in, error)
						}
					} catch(SuspendExecution suspend) {
						throw suspend
					} catch(Throwable t) {
						pipe.error(in, t)
					}
				}
				
				@Suspendable
				override complete() {
					pipe.complete
				}
				
			}
		]
	}

	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	@Cold @Controlled
	def static <ERROR extends Throwable, IN, IN2, OUT, PROMISE extends Promise<IN2, OUT>> Stream<IN, OUT> call(Stream<IN, OUT> stream, Class<ERROR> errorType, (ERROR)=>PROMISE onErrorPromiseFn) {
		stream.call(errorType) [ IN in, ERROR err | onErrorPromiseFn.apply(err) as Promise<Object, OUT> ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	@Cold @Controlled
	def static <ERROR extends Throwable, IN, OUT, IN2, PROMISE extends Promise<IN2, OUT>> Stream<IN, OUT> call(Stream<IN, OUT> stream, Class<ERROR> errorType, (IN, ERROR)=>PROMISE onErrorPromiseFn) {
		stream.operation [ pipe |
			ObservableOperation.onErrorCall(stream, pipe, errorType, onErrorPromiseFn)
		]
	}
	
	// BUFFERING ///////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Adds a buffer to the stream. Usually this is placed at the top of the stream, so
	 * everything in the chain below it gets buffered. When the chain cannot keep up with the data
	 * being thrown in at the top, the buffer will call pause on the stream and disregard any
	 * incoming values. When next is called from the bottom to indicate the stream can process again,
	 * the stream will resume the input and process from the buffer.
	 * <p>
	 * @param stream the stream to be buffered
	 * @param maxSize the maximum size of the buffer before it pauses the stream. 
	 * 	Set to 0 or negative to disable buffering and return the original stream
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> buffer(Stream<IN, OUT> stream, int maxSize) {
		if(maxSize <= 0) return stream
		val Queue<Pair<IN, OUT>> buffer = Queues.newArrayDeque
		val ready = new AtomicBoolean(false)
		val completed = new AtomicBoolean(false)
		val pipe = new Pipe<IN, OUT> {
			
			@Suspendable
			override next() {
				// get the next value from the queue to stream
				val nextValue = buffer.poll
				if(nextValue != null) {
					// we have a buffered value, stream it
					ready.set(false)
					value(nextValue.key, nextValue.value)
					// we have one more slot in the queue now, so resume the stream if it was paused
					if(!stream.isOpen) {
						stream.resume
					}
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
			
			@Suspendable
			override isOpen() {
				stream.isOpen
			}
			
			@Suspendable
			override pause() {
				stream.pause
			}
			
			@Suspendable
			override resume() {
				stream.resume
				stream.next
			}
			
			@Suspendable
			override close() {
				super.close
				stream.close
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
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
			
			@Suspendable
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			@Suspendable
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
	
	@Cold @Controlled
	def static <IN, OUT, MAP> Stream<IN, MAP> map(Stream<IN, OUT> stream, (OUT)=>MAP mapFn) {
		stream.map [ in, out | mapFn.apply(out) ]
	}

	@Cold @Controlled
	def static <IN, OUT, MAP> Stream<IN, MAP> map(Stream<IN, OUT> stream, (IN, OUT)=>MAP mapFn) {
		stream.operation [ pipe |
			ObservableOperation.map(stream, pipe, mapFn)
		]
	}

	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> effect(Stream<IN, OUT> stream, (OUT)=>void effectFn) {
		stream.map [ in, out | effectFn.apply(out) return out ]
	}

	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> effect(Stream<IN, OUT> stream, (IN, OUT)=>void effectFn) {
		stream.map [ in, out | effectFn.apply(in, out) return out ]
	}

	/** 
	 * Transform the input of a stream based on the existing input.
	 */	
	@Cold @Controlled
	def static <IN1, IN2, OUT> Stream<IN2, OUT> mapInput(Stream<IN1, OUT> stream, (IN1)=>IN2 inputMapFn) {
		stream.mapInput [ in1, out | inputMapFn.apply(in1) ]
	}

	/** 
	 * Transform the input of a stream based on the existing input and output.
	 * <p>
	 * The input mapping function is passed both an input and an output parameter. The output
	 * is only available when a normal value comes in, and not for errors.
	 */	
	@Cold @Controlled
	def static <IN1, IN2, OUT> Stream<IN2, OUT> mapInput(Stream<IN1, OUT> stream, (IN1, Opt<OUT>)=>IN2 inputMapFn) {
		val pipe = new Pipe<IN2, OUT> {

			@Suspendable
			override next() { 
				stream.next
			}

			@Suspendable
			override close() {
				super.close
				stream.close
			}
			
			@Suspendable
			override pause() {
				stream.pause
			}
			
			@Suspendable
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
	
	// FILTERING //////////////////////////////////////////////////////////////////////////////
		
	
	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	@Cold @Controlled
	def static <IN, OUT> filter(Stream<IN, OUT> stream, (OUT)=>boolean filterFn) {
		stream.filter [ in, out, index, passed | filterFn.apply(out) ]
	}

	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for.
	 */
	@Cold @Controlled
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
	@Cold @Controlled
	def static <IN, OUT> filter(Stream<IN, OUT> stream, (OUT, Long, Long)=>boolean filterFn) {
		stream.filter [ in, out, index, passed | filterFn.apply(out, index, passed) ]
	}

	/** Disposes undefined optionals and continues the stream with the remaining optionals unboxed. */
	@Cold @Controlled
	def static <IN, OUT> filterDefined(Stream<IN, Opt<OUT>> stream) {
		stream.filter [ defined ].map [ value ]
	}


	/**
	 * Filter items in a stream to only the ones that the filterFn
	 * returns a true for. This version also counts the number of
	 * items passed into the stream (the index) and the number of
	 * items passed by this filter so far. Both of these numbers
	 * are reset by a finish.
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> filter(Stream<IN, OUT> stream, (IN, OUT, Long, Long)=>boolean filterFn) {
		stream.operation [ pipe |
			val index = new AtomicLong(0)
			val passed = new AtomicLong(0)
			stream.observer = new Observer<IN, OUT> {
				
				@Suspendable
				override value(IN in, OUT value) {
					val i = index.incrementAndGet
					if(filterFn.apply(in, value, i, passed.get)) {
						passed.incrementAndGet
						pipe.value(in, value)
					} else {
						stream.next
					}
				}
				
				@Suspendable
				override error(IN in, Throwable t) {
					pipe.error(in, t)
				}
				
				@Suspendable
				override complete() {
					pipe.complete
				}
				
			}
		]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	@Cold @Controlled
	def static <IN, OUT> until(Stream<IN, OUT> stream, (OUT)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(out) ]
	}
	
	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	@Cold @Controlled
	def static <IN, OUT> until(Stream<IN, OUT> stream, (IN, OUT)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(in, out) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * It is exclusive, meaning that if the value from the
	 * stream matches the untilFn, that value will not be passed.
	 */
	@Cold @Controlled
	def static <IN, OUT> until(Stream<IN, OUT> stream, (IN, OUT, Long)=>boolean untilFn) {
		stream.until [ in, out, index, passed | untilFn.apply(in, out, index) ]
	}

	/**
	 * Stream until the until condition Fn returns true. 
	 * When this happens, the stream will finish. Any other encountered Finish is upgraded one level.
	 * @param stream the stream to process
	 * @param untilFn a function that gets the stream value input, output, index and the amount of passed values so far.
	 * @return a new substream that contains all the values up to the moment untilFn was called and an additional level 0 finish.
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> until(Stream<IN, OUT> stream, (IN, OUT, Long, Long)=>boolean untilFn) {
		stream.operation [ pipe |
			ObservableOperation.until(stream, pipe, untilFn)
		]
	}
	
	 /**
	  * Skips the head and returns a stream of the trailing entries.
	  * Alias for Stream.skip(1) 
	  */
	@Cold @Controlled
	def static <IN, OUT> tail(Stream<IN, OUT> stream) {
		stream.skip(1)
	}
	
	@Cold @Controlled
	/**
	 * Skip an amount of items from the stream, and process only the ones after that.
	 */
	def static <IN, OUT> Stream<IN, OUT> skip(Stream<IN, OUT> stream, int amount) {
		stream.filter [ it, index, passed | index > amount ] 
	}

	@Cold @Controlled
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
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, Pair<Integer, OUT>> index(Stream<IN, OUT> stream) {
		stream.operation [ pipe |
			val counter = new AtomicInteger(0)
			stream.observer = new Observer<IN, OUT> {
			
				@Suspendable
				override value(IN in, OUT value) {
					pipe.value(in, counter.incrementAndGet -> value)
				}
				
				@Suspendable
				override error(IN in, Throwable t) {
					pipe.error(in, t)
				}
				
				@Suspendable
				override complete() {
					pipe.complete
				}
			
			}
		]
	}
	
	/** 
	 * Flatten a stream of lists of items into a stream of the items in those lists.
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, OUT> separate(Stream<IN, List<OUT>> stream) {
		val ignoreNextCount = new AtomicInteger(0)
		val pipe = new Pipe<IN, OUT> {
			
			@Suspendable
			override next() {
				// ignore one less next until there is nothing left to ignore
				if(ignoreNextCount.decrementAndGet <= 0) {
					// stop ignoring and stream the next
					ignoreNextCount.set(0)
					stream.next
				}
			}
			
			@Suspendable
			override isOpen() {
				stream.isOpen
			}
			
			@Suspendable
			override pause() {
				stream.pause
			}
			
			@Suspendable
			override resume() {
				stream.resume
			}
			
			@Suspendable
			override close() {
				super.close
				stream.close
			}
			
		}
		stream.observer = new Observer<IN, List<OUT>> {
			
			@Suspendable
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
			
			@Suspendable
			override error(IN in, Throwable t) {
				pipe.error(in, t)
			}
			
			@Suspendable
			override complete() {
				pipe.complete
			}
			
		}
		pipe
	}
	
	@Cold @Controlled
	def static <IN, OUT, REDUCED> Stream<IN, REDUCED> scan(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, OUT)=>REDUCED reducerFn) {
		stream.scan(initial) [ p, r, it | reducerFn.apply(p, it) ]
	}

	@Cold @Controlled
	def static <IN, OUT, REDUCED> Stream<IN, REDUCED> scan(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, IN, OUT)=>REDUCED reducerFn) {
		val reduced = new AtomicReference<REDUCED>(initial)
		stream.operation [ pipe |
			stream.observer = new Observer<IN, OUT> {
				
				@Suspendable
				override value(IN in, OUT value) {
					val result = reducerFn.apply(reduced.get, in, value)
					reduced.set(result)
					if(result != null) pipe.value(in, result)
					else stream.next
				}
				
				@Suspendable
				override error(IN in, Throwable t) {
					pipe.error(in, t)
				}
				
				@Suspendable
				override complete() {
					pipe.complete
				}
				
			}
		]
	}
	
	/** Flatten a stream of streams into a single stream. Expects streams to be ordered and preserves order, resolving one stream at a time. */
	@Cold @Uncontrolled
	def static <IN, IN2, OUT, STREAM extends Stream<IN2, OUT>> Stream<IN, OUT> flatten(Stream<IN, STREAM> stream) {
		stream.operation [ pipe |
			ObservableOperation.flatten(stream, pipe)
		]
	}

	// CALL AND PERFORM ///////////////////////////////////////////////////////////////////////
	
	/** 
	 * Converts a stream of delayed values into a normal stream.
	 * If concurrency is set to other than 1, the stream will start pulling in next values
	 * automatically, making it a hot stream.
	 * @param maxConcurrency sets how many deferred processes get resolved in parallel. 0 for unlimited.
	 */
	@Cold @Unsorted @Controlled
	def static <IN, OUT> Stream<IN, OUT> resolve(Stream<IN, ? extends Promise<?, OUT>> stream) {
		stream.operation [ pipe |
			ObservableOperation.flatten(stream, pipe)
		]
	}

	@Cold @Controlled
	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Stream<IN, MAP> call(Stream<IN, OUT> stream, (OUT)=>PROMISE mapFn) {
		stream.call [ in, out | mapFn.apply(out) ]
	}

	@Cold @Unsorted @Controlled
	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Stream<IN, MAP> call(Stream<IN, OUT> stream, int maxConcurrency, (OUT)=>PROMISE mapFn) {
		stream.call [ in, out | mapFn.apply(out) ]
	}

	@Cold @Unsorted @Controlled
	def static <IN, OUT, MAP, PROMISE extends Promise<?, MAP>> Stream<IN, MAP> call(Stream<IN, OUT> stream, (IN, OUT)=>PROMISE mapFn) {
		stream.map(mapFn).resolve
	}

	/** Perform some side-effect action based on the stream. */
	@Cold @Controlled
	def static <IN, OUT, PROMISE extends Promise<?, ?>> Stream<IN, OUT> perform(Stream<IN, OUT> stream, (OUT)=>Promise<?, ?> promiseFn) {
		stream.call [ in, value | promiseFn.apply(value).map [ value ] ]
	}

	/** Perform some side-effect action based on the stream. */
	@Cold @Controlled
	def static <IN, OUT, PROMISE extends Promise<?, ?>> Stream<IN, OUT> perform(Stream<IN, OUT> stream, (IN, OUT)=>Promise<?, ?> promiseFn) {
		stream.call [ in, value | promiseFn.apply(in, value).map [ value ] ]
	}

	// REDUCTION /////////////////////////////////////////////////////////////////////////////

	/** 
	 * Pulls in all the values from the stream, fails if the stream contains an error, and completes when the stream completes.
	 */
	@Cold
	def static <IN, OUT> Task asTask(Stream<IN, OUT> stream) {
		val task = new Task {
			
			val started = new AtomicBoolean(false)
			
			@Suspendable
			override next() {
				// allow this only once
				if(started.compareAndSet(false, true)) {
					stream.next
				}
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				// do nothing with the value, just ask for the next one
				stream.next
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				task.error(t)
			}
			
			@Suspendable
			override complete() {
				task.complete
				stream.close
			}
			
		}
		task
	}

	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed.
	 */
	@Hot @Suspendable
	def static <IN, OUT, REDUCED> Promise<Long, REDUCED> reduce(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, OUT)=>REDUCED reducerFn) {
		stream.reduce(initial) [ reduced, in, out | reducerFn.apply(reduced, out) ]	
	}
	
	/**
	 * Reduce a stream of values to a single value, and pass a counter in the function.
	 * Errors in the stream are suppressed.
	 * @return a promise that has the amount the amount of items that were processed as input, and the reduced value as output. 
	 */
	@Hot @Suspendable
	def static <IN, OUT, REDUCED> Promise<Long, REDUCED> reduce(Stream<IN, OUT> stream, REDUCED initial, (REDUCED, IN, OUT)=>REDUCED reducerFn) {
		val promise = new Deferred<Long, REDUCED> {
			
			@Suspendable
			override next() {
				stream.next
			}
			
		}
		val reduced = new AtomicReference<REDUCED>(initial)
		val counter = new AtomicLong
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				try {
					counter.incrementAndGet
					reduced.set(reducerFn.apply(reduced.get, in, value))
				} finally {
					stream.next
				}
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				reduced.set(null)
				promise.error(counter.get, t)
			}
			
			@Suspendable
			override complete() {
				val result = reduced.get
				if(result != null) { 
					promise.value(counter.get, result)
				} else if(initial != null) {
					promise.value(counter.get, initial)
				} else {
					promise.error(counter.get, new Exception('no value came from the stream to reduce'))
				}
				stream.close
			}
			
		}
		stream.next
		promise
	}
	
	/**
	 * Promises a list of all items from a stream. Starts the stream.
	 */
	@Hot @Suspendable
	def static <IN, OUT> collect(Stream<IN, OUT> stream) {
		stream.reduce(newArrayList as List<OUT>) [ list, it | list.concat(it) ]
	}
	
	/**
	 * Promises a map of all inputs and outputs from a stream. Starts the stream.
	 */
	@Hot @Suspendable
	def static <IN, OUT> Promise<Long, Map<IN, OUT>> collectInOut(Stream<IN, OUT> stream) {
		stream.reduce(newHashMap as Map<IN, OUT>) [ list, in, out | list.put(in, out) list ]
	}

	/**
	 * Concatenate a lot of strings into a single string, separated by a separator string.
	 * <pre>
	 * (1..3).stream.join('-').then [ println(it) ] // prints 1-2-3
	 */	
	@Hot @Suspendable
	def static <IN, OUT> join(Stream<IN, OUT> stream, String separator) {
		stream.reduce('') [ acc, it | acc + (if(acc != '') separator else '') + it.toString ]
	}

	/**
	 * Add the value of all the items in the stream until a finish.
	 */
	@Hot @Suspendable
	def static <IN, OUT extends Number> sum(Stream<IN, OUT> stream) {
		stream.reduce(0D) [ acc, it | acc + doubleValue ]
	}

	/**
	 * Average the items in the stream until a finish.
	 */
	@Hot @Suspendable
	def static <IN, OUT extends Number> average(Stream<IN, OUT> stream) {
		stream
			.index
			.reduce(0 -> 0D) [ acc, it | key -> (acc.value + value.doubleValue) ]
			.map [ value / key ]
	}
	
	/**
	 * Count the number of items passed in the stream until a finish.
	 */
	@Hot @Suspendable
	def static <IN, OUT> count(Stream<IN, OUT> stream) {
		stream.reduce(0) [ acc, it | acc + 1 ]
	}

	/**
	 * Gives the maximum value found on the stream.
	 * Values must implement Comparable
	 */
	@Hot @Suspendable
	def static <IN, OUT extends Comparable<OUT>> max(Stream<IN, OUT> stream) {
		stream.reduce(null) [ Comparable<OUT> acc, it | if(acc != null && acc.compareTo(it) > 0) acc else it ]
	}

	/**
	 * Gives the minimum value found on the stream.
	 * Values must implement Comparable
	 */
	@Hot @Suspendable
	def static <IN, OUT extends Comparable<OUT>> min(Stream<IN, OUT> stream) {
		stream.reduce(null) [ Comparable<OUT> acc, it | if(acc != null && acc.compareTo(it) < 0) acc else it ]
	}

	@Hot @Suspendable
	def static <IN, OUT> all(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		stream.reduce(true) [ acc, it | acc && testFn.apply(it) ]
	}

	/**
	 * Promises true if no stream values match the test function.
	 * Starts the stream.
	 */
	@Hot @Suspendable
	def static <IN, OUT> none(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		stream.reduce(true) [ acc, it | acc && !testFn.apply(it) ]
	}

	/**
	 * Promises true if any of the values match the passed testFn.
	 * Starts the stream.
	 */
	@Hot @Suspendable
	def static <IN, OUT> any(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		val promise = new Deferred<IN, Boolean>
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				if(testFn.apply(value)) {	
					promise.value(in, true)
					stream.close
				} else {
					stream.next
				}
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				promise.error(in, t)
				stream.close
			}
			
			@Suspendable
			override complete() {
				stream.close
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
	@Hot @Suspendable
	def static <IN, OUT> Promise<IN, OUT> head(Stream<IN, OUT> stream) {
		stream.first [ true ]
	}
	
	 /**
	  * Start the stream and promise the first value coming from the stream.
	  * Closes the stream once it has the value or an error.
	  */
	@Hot @Suspendable
	def static <IN, OUT> Promise<IN, OUT> first(Stream<IN, OUT> stream) {
		stream.first [ true ]
	}
	
	/**
	 * Promises the first value that matches the testFn.
	 * Starts the stream.
	 */
	@Hot @Suspendable
	def static <IN, OUT> first(Stream<IN, OUT> stream, (OUT)=>boolean testFn) {
		stream.first [ r, it | testFn.apply(it) ]
	}

	/**
	 * Promises the first value that matches the testFn.
	 * Starts the stream.
	 */
	@Hot @Suspendable
	def static <IN, OUT> Promise<IN, OUT> first(Stream<IN, OUT> stream, (IN, OUT)=>boolean testFn) {
		val promise = new Deferred<IN, OUT> {
			
			@Suspendable
			override next() {
				stream.next
			}
			
		}
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				if(testFn.apply(in, value)) {	
					promise.value(in, value)
					stream.close
				} else {
					stream.next
				}
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				promise.error(in, t)
				stream.close
			}
			
			@Suspendable
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
	@Hot @Suspendable
	def static <IN, OUT> Promise<IN, OUT> last(Stream<IN, OUT> stream) {
		val promise = new Deferred<IN, OUT> {
			
			@Suspendable
			override next() {
				stream.next
			}
			
		}
		val last = new AtomicReference<Pair<IN, OUT>>
		
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				if(!promise.fulfilled) last.set(in->value) 
				stream.next
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				stream.next
			}
			
			@Suspendable
			override complete() {
				stream.close
				if(!promise.fulfilled && last.get != null) {
					promise.value(last.get.key, last.get.value)
				} else {
					promise.error(null, new Exception('stream closed without passing a value, no last entry found.'))
				}
			}
			
		}
		stream.next
		promise
	}

	// TIMING ///////////////////////////////////////////////////////////////////
	
	/** 
	 * Adds delay to each observed value.
	 * If the observable is completed, it will only send complete to the observer once all delayed values have been sent.
	 * <p>
	 * Better than using stream.perform [ timerFn.apply(period) ] since uncontrolled streams then can be completed before
	 * all values have been pushed.
	 */
	@Cold @Controlled
	def static <IN, OUT> delay(Stream<IN, OUT> stream, Period delay, (Period)=>Task timerFn) {
		stream.operation [ pipe |
			ObservableOperation.delay(stream, pipe, delay, timerFn)
		]
	}

	/**
	 */
	@Cold @Controlled
	def static <IN, OUT> Stream<IN, Stream<IN, OUT>> window(Stream<IN, OUT> stream, Period interval) {
		stream.operation [ pipe |
			ObservableOperation.window(stream, pipe, interval)
		]
	}

	/**
	 */
	@Cold @Controlled @Lossy
	def static <IN, OUT> sample(Stream<IN, OUT> stream, Period interval) {
		stream.window(interval).map [ window | window.last ].resolve
	}
	
	/**
	 * Stream only [amount] values per [period]. Anything more will be rejected and the next value asked.
	 * Errors and complete are streamed immediately.
	 */
	@Cold @Controlled @Lossy
	def static <IN, OUT> throttle(Stream<IN, OUT> stream, Period minimumInterval) {
		stream.operation [ pipe |
			ObservableOperation.throttle(stream, pipe, minimumInterval)
		]
	}

	/**
	 * Limit the output rate of values to 1 value per interval. If an entry comes in sooner, a timer
	 * is set so that after the remaining interval time has expired, it is still output.
	 * <p>
	 * If you use ratelimit with an uncontrolled input stream, you will need to buffer that stream. 
	 */
	@Cold @Controlled
	def static <IN, OUT> ratelimit(Stream<IN, OUT> stream, Period minimumInterval, (Period)=>Task timerFn) {
		stream.operation [ pipe |
			ObservableOperation.ratelimit(stream, pipe, minimumInterval, timerFn)
		]
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

	// MULTITHREADED //////////////////////////////////////////////////////////////////

	/**
	 * Deprecated: use Stream.newPeriodicStream() instead.
	 * <p>
	 * Create a stream that periodically pushes a count, starting at 1, upto the set limit.
	 * Start the stream by calling next, after which it will auto-push values without needing next.
	 * @param executor the scheduler to use
	 * @param interval the period between values pushed onto the stream
	 * @param limit the maximum amount of counts to stream. If set to 0 or less, it will stream forever.
	 */
	@Deprecated
	@Cold @Uncontrolled @MultiThreaded
	def static Stream<Long, Long> periodic(ScheduledExecutorService executor, Period interval, int limit) {
		Streams.newPeriodicStream(executor, interval, limit)
	}

	/**
	 * Deprecated: use Stream.newPeriodicStream() instead.
	 * <p>
	 * Create a stream that periodically pushes a count, starting at 1, upto the set limit.
	 * Start the stream by calling next, after which it will auto-push values without needing next.
	 * @param executor the scheduler to use
	 * @param interval the period between values pushed onto the stream
	 */
	@Deprecated
	@Cold @Uncontrolled @MultiThreaded
	def static Stream<Long, Long> periodic(ScheduledExecutorService executor, Period interval) {
		Streams.newPeriodicStream(executor, interval)
	}

}
