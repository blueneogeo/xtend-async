package nl.kii.async.fibers

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.FiberAsync
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.SuspendableCallable
import java.util.concurrent.TimeUnit
import nl.kii.async.observable.Observer
import nl.kii.async.promise.Input
import nl.kii.async.promise.Promise
import nl.kii.util.Period
import co.paralleluniverse.strands.Strand
import co.paralleluniverse.fibers.FiberScheduler
import co.paralleluniverse.strands.SuspendableRunnable
import nl.kii.async.promise.Task

class FiberExtensions {
	
	/**
	 * Perform a function in the background using a Fiber.
	 * Uses the default fiber scheduler. 
	 * <p>
	 * NOTE: any method within the fiber that tries to suspend or await will have to be annotated with
	 * the Suspendable annotation or have a throws SuspendedException.
	 * <p> 
	 * @Param function the function to perform, which returns a value
	 * @Return a promise of the value returned by the function, or has an error if the function threw an error
	 */
	@Suspendable
	def static <OUT> Promise<OUT, OUT> async(SuspendableCallable<OUT> function) {
		val input = new Input<OUT>
		new Fiber [
			try {
				input.set(function.run)
			} catch(Throwable t) {
				input.error(t)
			} 
		].start
		input
	}

	/**
	 * Perform an action in the background using a Fiber.
	 * Uses the default fiber scheduler. 
	 * <p>
	 * NOTE: any method within the fiber that tries to suspend or await will have to be annotated with
	 * the Suspendable annotation or have a throws SuspendedException.
	 * <p> 
	 * @Param action to perform
	 * @Return a task that completes when the action completes, or has an error if the action threw an error
	 */
	@Suspendable
	def static <OUT> Task async(SuspendableRunnable action) {
		val task = new Task
		new Fiber [
			try {
				action.run
				task.complete
			} catch(Throwable t) {
				task.error(t)
			} 
		].start
		task
	}

	/**
	 * Perform a function in the background using a Fiber.
	 * Uses the specified scheduler.
	 * <p>
	 * NOTE: any method within the fiber that tries to suspend or await will have to be annotated with
	 * the Suspendable annotation or have a throws SuspendedException.
	 * <p> 
	 * @Param function the function to perform, which returns a value
	 * @Return a promise of the value returned by the function, or has an error if the function threw an error
	 */
	@Suspendable
	def static <OUT> Promise<OUT, OUT> async(FiberScheduler scheduler, SuspendableCallable<OUT> function) {
		val input = new Input<OUT>
		new Fiber(scheduler) [
			try {
				input.set(function.run)
			} catch(Throwable t) {
				input.error(t)
			} 
		].start
		input
	}

	/**
	 * Perform an action in the background using a Fiber.
	 * Uses the specified scheduler. 
	 * <p>
	 * NOTE: any method within the fiber that tries to suspend or await will have to be annotated with
	 * the Suspendable annotation or have a throws SuspendedException.
	 * <p> 
	 * @Param action to perform
	 * @Return a task that completes when the action completes, or has an error if the action threw an error
	 */
	@Suspendable
	def static <OUT> Task async(FiberScheduler scheduler, SuspendableRunnable action) {
		val task = new Task
		new Fiber(scheduler) [
			try {
				action.run
				task.complete
			} catch(Throwable t) {
				task.error(t)
			} 
		].start
		task
	}

	/**
	 * Suspends the Strand (Thread or Fiber) until the promise completes, then gives you the value from the promise.
	 * <p>
	 * NOTE: This method can only be used from within another fibre, and within a method either
	 * annotated with the Suspendable annotation, or if the method throws SuspendedException.
	 * <p> 
	 * @Param promise the promise to wait to complete
	 * @Throws Throwable any error coming from the promise when it fails
	 */
	@Suspendable
	def static <OUT> OUT await(Promise<?, OUT> promise) {
		await(promise, null)
	}
	
	/**
	 * Waits by calling Strand.sleep for the indicated period.
	 * Since a strand can be both a thread and a fiber, it can work in both circumstances.
	 * @Param delay the delay period
	 */
	@Suspendable
	def static void wait(Period delay) {
		Strand.sleep(delay.ms)
	}
	
	/**
	 * Suspends the fiber until the promise completes, then gives you the value from the promise.
	 * <p>
	 * NOTE: This method can only be used from within another fibre, and within a method either
	 * annotated with the Suspendable annotation, or if the method throws SuspendedException.
	 * <p> 
	 * @Param promise the promise to wait to complete
	 * @Param timeout the maximum amount of time to wait for the promise to complete
	 * @Throws TimeoutException if the timeout expires
	 * @Throws Throwable any error coming from the promise when it fails
	 */
	@Suspendable	
	def static <IN, OUT> OUT await(Promise<IN, OUT> promise, Period timeout) {
		val waiter = new FiberAsync<OUT, Throwable> {
			
			override protected requestAsync() {
				promise.observer = new Observer<IN, OUT> {
					
					override value(IN in, OUT value) {
						asyncCompleted(value)
					}
					
					override error(Object in, Throwable t) {
						asyncFailed(t)
					}
					
					override complete() {
						// do nothing
					}
					
				}
			}
			
		}
		try {
			if(timeout != null) {
				waiter.run(timeout.ms, TimeUnit.MILLISECONDS)
			} else {
				waiter.run
			}
		} catch(Throwable t) {
			if(t.cause != null) {
				throw t.cause
			} else {
				throw t
			}
		}
		
	}
	
}
