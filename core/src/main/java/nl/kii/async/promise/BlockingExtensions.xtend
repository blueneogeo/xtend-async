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

final class BlockingExtensions {
	
	// BLOCKING ///////////////////////////////////////////////////////////////////////
	
	/** Wrap the promise into a future that can block. */
	@Blocking
	def static <IN, OUT> Future<OUT> asFuture(Promise<IN, OUT> promise) {
		new PromisedFuture(promise)
	}

	/** 
	 * Deprecated: use block(promise) instead.
	 * <p>
	 * Blocks the current thread to wait for the value
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Deprecated
	@Blocking
	def static <IN, OUT> await(Promise<IN, OUT> promise, Period timeout) throws TimeoutException {
		block(promise, timeout)
	}

	/** 
	 * Blocks the current thread to wait for the value for a maximum specified period.
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Blocking
	def static <IN, OUT> block(Promise<IN, OUT> promise, Period timeout) throws TimeoutException {
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
	 * Deprecated: use block(promise) instead.
	 * <p>
	 * Blocks the current thread to wait for the value
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Deprecated
	@Blocking
	def static <IN, OUT> await(Promise<IN, OUT> promise) throws TimeoutException {
		block(promise)
	}
	
	/** 
	 * Blocks the current thread to wait for the value
	 * @throws TimeoutException when the process was waiting for longer than the passed timeout period
	 */
	@Blocking
	def static <IN, OUT> block(Promise<IN, OUT> promise) throws TimeoutException {
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
	 * Deprecated: use Promises.newPromise(service, callable) instead.
	 * <p> 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	@Deprecated
	@MultiThreaded
	def static <IN, OUT> Promise<OUT, OUT> promise(ExecutorService service, Callable<OUT> callable) {
		Promises.newPromise(service, callable)
	}	

	/** 
	 * Deprecated: use Promises.newTask(service, runnable) instead.
	 * <p> 
	 * Execute the runnable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| doSomeHeavyLifting ].then [ println('done!') ]
	 */
	@Deprecated
	@MultiThreaded
	def static Task task(ExecutorService service, Runnable runnable) {
		Promises.newTask(service, runnable)
	}

	/** 
	 * Deprecated: use Promises.newTimer(schedulingService) instead.
	 * <p> 
	 * Create a timer function to be used in delay methods.
	 * A timer function takes a period and returns a task that completes when that period has expired.
	 */
	@Deprecated
	@MultiThreaded
	def static (Period)=>Task timerFn(ScheduledExecutorService schedulingService) {
		Promises.newTimer(schedulingService)
	}
	
}
