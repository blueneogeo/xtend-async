package nl.kii.async.collections

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.promise.Input
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import nl.kii.async.stream.Sink
import nl.kii.async.stream.Stream
import nl.kii.util.Period

import static java.util.concurrent.TimeUnit.*

class ExecutorExtensions {
	
	/** 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	def static <T> Promise<?, T> promise(ExecutorService service, Callable<T> callable) {
		val promise = new Input<T>
		val Runnable processor = [|
			try {
				val result = callable.call
				promise.set(result)
			} catch(Throwable t) {
				promise.error(t)
			}
		]
		service.submit(processor)
		promise
	}	

	/** 
	 * Execute the runnable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| doSomeHeavyLifting ].then [ println('done!') ]
	 */
	def static Task task(ExecutorService service, Runnable runnable) {
		val task = new Task
		val Runnable processor = [|
			try {
				runnable.run
				task.complete
			} catch(Throwable t) {
				task.error(t)
			}
		]
		service.submit(processor)
		task
	}
	
	def static (Period)=>Task timer(ScheduledExecutorService executor) {
		[ period |
			val task = new Task
			executor.schedule([ task.complete ], period.ms, TimeUnit.MILLISECONDS)
			task
		]
	}
	
	/** 
	 * Create a timer stream, that pushes out the time in ms since starting, every periodMs ms.
	 * Note: this breaks the single threaded model!
	 */	
	def static Stream<?, Long> streamEvery(ScheduledExecutorService scheduler, int periodMs) {
		streamEvery(scheduler, periodMs, -1)
	}
	
	/** 
	 * Create a timer stream, that pushes out the time in ms since starting, every periodMs ms.
	 * It will keep doing this for forPeriodMs time. Set forPeriodMs to -1 to stream forever.
	 * Note: this breaks the single threaded model!
	 */	
	def static Stream<?, Long> streamEvery(ScheduledExecutorService scheduler, int periodMs, int forPeriodMs) {
		val task = new AtomicReference<ScheduledFuture<?>>
		val newStream = new Sink<Long> {
			override onNext() { }
			override onClose() { }
		}
		val start = System.currentTimeMillis
		val Runnable pusher = [|
			val now = System.currentTimeMillis
			val expired = forPeriodMs > 0 && now - start > forPeriodMs 
			if(newStream.open && !expired) {
				newStream.push(now - start)
			} else task.get.cancel(false)
		]
		task.set(scheduler.scheduleAtFixedRate(pusher, 0, periodMs, MILLISECONDS))
		newStream
	}

}
