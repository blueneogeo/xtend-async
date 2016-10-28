package nl.kii.async.promise

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import nl.kii.async.annotation.Blocking
import nl.kii.async.annotation.MultiThreaded
import nl.kii.util.Period
import co.paralleluniverse.fibers.instrument.DontInstrument

class BlockingExtensions {
	
	
	// BLOCKING ///////////////////////////////////////////////////////////////////////
	
	/** Wrap the promise into a future that can block. */
	@Blocking
	@DontInstrument
	def static <IN, OUT> Future<OUT> asFuture(Promise<IN, OUT> promise) {
		new PromisedFuture(promise)
	}

	/** 
	 * Blocks the current thread to wait for the value for a maximum specified period.
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Blocking
	@DontInstrument
	def static <IN, OUT> await(Promise<IN, OUT> promise, Period timeout) throws TimeoutException {
		try {
			new PromisedFuture(promise).get(timeout.ms, TimeUnit.MILLISECONDS)
		} catch(Throwable t) {
			if(t.cause != null) {
				throw t.cause
			} else {
				throw t
			}
		}
	}

	/** 
	 * Blocks the current thread to wait for the value
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Blocking
	@DontInstrument
	def static <IN, OUT> await(Promise<IN, OUT> promise) throws TimeoutException {
		try {
			new PromisedFuture(promise).get
		} catch(Throwable t) {
			if(t.cause != null) {
				throw t.cause
			} else {
				throw t
			}
		}
	}

	// MULTITHREADED //////////////////////////////////////////////////////////////////

	/** 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	@MultiThreaded
	@DontInstrument
	def static <IN, OUT> Promise<OUT, OUT> promise(ExecutorService service, Callable<OUT> callable) {
		val promise = new Input<OUT>
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
	@MultiThreaded
	@DontInstrument
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

	/** 
	 * Create a timer function to be used in delay methods.
	 * A timer function takes a period and returns a task that completes when that period has expired.
	 */
	@MultiThreaded
	@DontInstrument
	def static (Period)=>Task timerFn(ScheduledExecutorService executor) {
		[ period |
			val task = new Task
			executor.schedule([ task.complete ], period.ms, TimeUnit.MILLISECONDS)
			task
		]
	}
	
}
