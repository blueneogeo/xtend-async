package nl.kii.async.promise

import nl.kii.async.annotation.MultiThreaded
import java.util.concurrent.ExecutorService
import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import nl.kii.util.Period
import java.util.concurrent.TimeUnit

/** Creates new Promises */
final class Promises {
	
	private new() { }
	
	/** Create a new input, letting you use type inference from the rest of your code to find the type. */
	def static <OUT> newInput() {
		new Input<OUT>
	}
	
	/** Create a fulfilled promise of the passed value */
	def static <OUT> newPromise(OUT value) {
		new Input<OUT>(value)
	}

	/** Create a fulfilled promise of the passed input and value */
	def static <IN, OUT> newPromise(IN from, OUT value) {
		new Deferred<IN, OUT> => [ value(from, value) ]
	}

	/** 
	 * Execute the callable in the background and return as a promise.
	 * Lets you specify the executorservice to run on.
	 * <pre>
	 * val service = Executors.newSingleThreadExecutor
	 * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
	 */
	@MultiThreaded
	def static <IN, OUT> Promise<OUT, OUT> newPromise(ExecutorService service, Callable<OUT> callable) {
		val promise = new Input<OUT>
		val processor = new Runnable {
			
			override run() {
				try {
					val result = callable.call
					promise.set(result)
				} catch(Throwable t) {
					promise.error(t)
				}
			}
			
		}
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
	def static Task newTask(ExecutorService service, Runnable runnable) {
		val task = new Task
		val processor = new Runnable {
			
			override run() {
				try {
					runnable.run
					task.complete
				} catch(Throwable t) {
					task.error(t)
				}
			}
			
		}
		service.submit(processor)
		task
	}

	/** 
	 * Create a timer function to be used in delay methods.
	 * A timer function takes a period and returns a task that completes when that period has expired.
	 */
	@MultiThreaded
	def static (Period)=>Task newTimer(ScheduledExecutorService executor) {
		[ period |
			val task = new Task
			executor.schedule([ task.complete ], period.ms, TimeUnit.MILLISECONDS)
			task
		]
	}

}
