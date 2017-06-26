package nl.kii.async.stream

import co.paralleluniverse.fibers.Suspendable
import java.util.Date
import java.util.Iterator
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Cold
import nl.kii.async.annotation.Controlled
import nl.kii.async.annotation.MultiThreaded
import nl.kii.async.annotation.Uncontrolled
import nl.kii.async.observable.Observer
import nl.kii.async.promise.Task
import nl.kii.util.Period

import static extension nl.kii.util.DateExtensions.*

/**
 * Creates new streams.
 */
final class Streams {

	/**
	 * Create a stream out of a closure. Every time the stream calls for the next value,
	 * it will call the closure. If the closure returns null, the stream will complete.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> newStream(=>OUT nextValueFn) {
		new Sink<OUT> {
			
			@Suspendable
			override onNext() {
				// value instead of push saves a call on the stacktrace
				val nextValue = nextValueFn.apply
				if(nextValue !== null) value(nextValue, nextValue) else complete
			}
			
			@Suspendable
			override onClose() {
				// do nothing
			}
			
		}
	}
	
	/** Create a stream sink of a certain type, without support for backpressure.
	 * This version without specifying the OUT type allows you to use the Xtend
	 * inferrer to infer the types for you. */
	@Cold @Controlled
	def static <OUT> Sink<OUT> newSink() {
		new Sink<OUT> {
			
			@Suspendable
			override onNext() {
				// do nothing, no support for backpressure
			}
			
			@Suspendable
			override onClose() {
				// do nothing
			}
			
		}
	}

	/** 
	 * Create a stream source of a certain type, without support for backpressure.
	 * This version without specifying IN and OUT types allows you to use the Xtend
	 * inferrer to infer the types for you.
	 */
	@Cold @Controlled
	def static <IN, OUT> Source<IN, OUT> newSource() {
		new Source<IN, OUT> {
			
			@Suspendable
			override onNext() {
				// do nothing, no support for backpressure
			}
			
			@Suspendable
			override onClose() {
				// do nothing
			}
			
		}
	}

	/**
	 * Create a stream of infinite values, starting at 1, and incrementing for each next value. 
	 */
	@Cold @Controlled
	def static Stream<Long, Long> newCountingStream() {
		new Sink<Long> {
			
			val counter = new AtomicLong
			
			@Suspendable
			override onNext() {
				push(counter.incrementAndGet)
			}
			
			@Suspendable
			override onClose() {
				// do nothing
			}
			
		}
	}

/** 
	 * Create a stream out of an iterator. The iterator will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> newStream(Iterator<? extends OUT> iterator) {
		new Sink<OUT> {
			
			@Suspendable
			override onNext() {
				if(iterator.hasNext) {
					// value instead of push saves a call on the stacktrace
					val nextValue = iterator.next
					value(nextValue, nextValue)
				} else {
					complete
				}
			}
			
			@Suspendable
			override onClose() {
			}
		}
	}

	/** 
	 * Create a stream out of an iterable. The iterable will be lazily evaluated,
	 * meaning that the next value will only be requested when the stream requests
	 * a next value.
	 */
	@Cold @Controlled
	def static <OUT> Stream<OUT, OUT> newStream(Iterable<? extends OUT> iterable) {
		newStream(iterable.iterator)
	}
	
	/**
	 * Create a periodically emitting stream. The value in the stream is the count of the value, starting at 1.
	 * @param timerFn function that can be given a period and returns a task which completes after that period
	 * @param interval the period between values from the periodic stream
	 */
	 @Cold @Controlled
	 def static <OUT> Stream<Long, Long> newPeriodicStream((Period)=>Task timerFn, Period interval) {
	 		timerFn.newPeriodicStream(interval, 0)
	}

	/**
	 * Create a periodically emitting stream. The value in the stream is the count of the value, starting at 1.
	 * @param timerFn function that can be given a period and returns a task which completes after that period
	 * @param interval the period between values from the periodic stream
	 * @param maxAmount the maximum amount of values to emit
	 */
	 @Cold @Controlled
	 def static <OUT> Stream<Long, Long> newPeriodicStream((Period)=>Task timerFn, Period interval, int maxAmount) {
	 	val sink = new Sink<Long> {
	 		val that = this
		 	val started = new AtomicBoolean(false)
		 	val counter = new AtomicLong(0)
		 	val last = new AtomicReference<Date>
			
			@Suspendable
			override onNext() {
				if(maxAmount > 0 && counter.get >= maxAmount) {
					this.complete
					this.close
					return
				}
				counter.incrementAndGet
				// first next starts, after that, next is ignored until pause/resume
				if(started.compareAndSet(false, true)) {
					last.set(now)
					// first time, fire off immediately
					this.push(counter.get)
				} else {
					val expiredSinceLastPush = now - last.get
					// fire off with a delay
					timerFn.apply(interval - expiredSinceLastPush).observer = new Observer<Void, Void> {
						
						@Suspendable
						override value(Void in, Void value) {
							last.set(now)
							that.push(counter.get)
						}
						
						@Suspendable
						override error(Void in, Throwable t) {
							that.push(t)
						}
						
						@Suspendable
						override complete() {
							// do nothing
						}
						
					}
				}
			}
			
			@Suspendable
			override resume() {
				super.resume
				started.set(false)
				next
			}
			
			@Suspendable
			override pause() {
				super.pause
			}
			
			@Suspendable
			override onClose() {
				close
			}
	 		
	 	}
	 	sink
	 }
	 
	/**
	 * <p>
	 * Create a stream that periodically pushes a count, starting at 1, upto the set limit.
	 * Start the stream by calling next, after which it will auto-push values without needing next.
	 * @param executor the scheduler to use
	 * @param interval the period between values pushed onto the stream
	 * @param limit the maximum amount of counts to stream. If set to 0 or less, it will stream forever.
	 */
	@Cold @Uncontrolled @MultiThreaded
	def static Stream<Long, Long> newPeriodicStream(ScheduledExecutorService executor, Period interval, int limit) {
		new Sink<Long> {

			val counter = new AtomicLong(0)
			val Runnable pushFn = [
				this.push(counter.incrementAndGet)
				if(limit > 0 && counter.get >= limit) {
					complete
					onClose
				} 
			]
			val repeaterFn = [| executor.scheduleAtFixedRate(pushFn, 0, interval.ms, TimeUnit.MILLISECONDS) ]
			val repeater = new AtomicReference<ScheduledFuture<?>>
			
			@Suspendable
			override onNext() {
				if(repeater.get === null) {
					repeater.set(repeaterFn.apply)
				}
			}
			
			@Suspendable
			override onClose() {
				super.close
				repeater.get?.cancel(true)
			}
			
			@Suspendable
			override pause() {
				super.pause
				repeater.get?.cancel(true)
				repeater.set(null)
			}

			@Suspendable
			override resume() {
				super.resume
				super.next
			}
			
		}
	}

	/**
	 * Create a stream that periodically pushes a count, starting at 1, upto the set limit.
	 * Start the stream by calling next, after which it will auto-push values without needing next.
	 * @param executor the scheduler to use
	 * @param interval the period between values pushed onto the stream
	 */
	@Cold @Uncontrolled @MultiThreaded
	def static Stream<Long, Long> newPeriodicStream(ScheduledExecutorService executor, Period interval) {
		newPeriodicStream(executor, interval, 0)
	}	 
	
}