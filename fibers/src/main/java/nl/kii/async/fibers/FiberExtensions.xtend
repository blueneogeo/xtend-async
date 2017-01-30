package nl.kii.async.fibers
import static extension nl.kii.async.stream.StreamExtensions.*
import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.FiberAsync
import co.paralleluniverse.fibers.FiberScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.Strand
import co.paralleluniverse.strands.SuspendableCallable
import co.paralleluniverse.strands.SuspendableRunnable
import java.util.concurrent.TimeUnit
import nl.kii.async.observable.Observer
import nl.kii.async.promise.Input
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import nl.kii.async.stream.Stream
import nl.kii.util.Period
import co.paralleluniverse.fibers.SuspendExecution

final class FiberExtensions {

	private new() { }

	/** Await the next value from the stream. For now, do not use until bytecode injection issues are resolved */
	@Suspendable
	def static <IN, OUT> OUT awaitNext(Stream<IN, OUT> stream) {
		val promise = new Input<OUT>
		stream.observer = new Observer<IN, OUT> {
			
			@Suspendable
			override value(IN in, OUT value) {
				promise.set(value)
			}
			
			@Suspendable
			override error(IN in, Throwable t) {
				promise.error(t)
			}
			
			@Suspendable
			override complete() {
				stream.close
			}
			
		}
		stream.next
		if(!stream.open) return null
		// ask for the next value from the stream and wait for the promise to resolve
		// Fiber.currentFiber().scheduler.async [ stream.next ]
		promise.await
	}

	/** Iterate a stream in a blocking way. For now, do not use until bytecode injection issues are resolved */
	@Deprecated
	def static <T> awaitEach(Stream<?, T> stream) {
		new Iterable<T> {
			
			@Suspendable
			override iterator() {
				new nl.kii.async.fibers.StreamIterator(stream)
			}
			
		}
	}
	
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
	def static <OUT> Promise<OUT, OUT> async(SuspendableCallable<OUT> function) {
		val input = new Input<OUT>
		val fiber = new Fiber(new SuspendableRunnable {
			
			override run() throws SuspendExecution, InterruptedException {
				try {
					input.set(function.run)
				} catch(Throwable t) {
					input.error(t)
				}
			} 

		})
		fiber.start
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
	def static <OUT> Task async(SuspendableRunnable action) {
		val task = new Task
		val fiber = new Fiber(new SuspendableRunnable {
			
			override run() throws SuspendExecution, InterruptedException {
				try {
					action.run
					task.complete
				} catch(Throwable t) {
					task.error(t)
				}
			} 

		})
		fiber.start
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
	def static <OUT> Promise<OUT, OUT> async(FiberScheduler scheduler, SuspendableCallable<OUT> function) {
		val input = new Input<OUT>
		val fiber = new Fiber(scheduler, new SuspendableRunnable {
			
			override run() throws SuspendExecution, InterruptedException {
				try {
					input.set(function.run)
				} catch(Throwable t) {
					input.error(t)
				}
			} 
		})
		fiber.start
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
	def static <OUT> Task async(FiberScheduler scheduler, SuspendableRunnable action) {
		val task = new Task
		val fiber = new Fiber(scheduler, new SuspendableRunnable {
			
			override run() throws SuspendExecution, InterruptedException {
				try {
					action.run
					task.complete
				} catch(Throwable t) {
					task.error(t)
				}
			} 

		})
		fiber.start
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
	def static void sleep(Period delay) {
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
			
			override requestAsync() {
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

	/**
	 * Suspends the fiber until all values of a stream have come in and collected into a list,
	 * and the stream completed or closed.
	 * Shortcut for the common stream.collect.await.
	 * <p>
	 * NOTE: This method can only be used from within another fibre, and within a method either
	 * annotated with the Suspendable annotation, or if the method throws SuspendedException.
	 */
	@Suspendable
	def static <OUT> list(Stream<?, OUT> stream) {
		list(stream, null)
	} 
	
	/**
	 * Suspends the fiber until all values of a stream have come in and collected into a list,
	 * and the stream completed or closed.
	 * Shortcut for the common stream.collect.await.
	 * <p>
	 * NOTE: This method can only be used from within another fibre, and within a method either
	 * annotated with the Suspendable annotation, or if the method throws SuspendedException.
	 */
	@Suspendable
	def static <OUT> list(Stream<?, OUT> stream, Period timeout) {
		stream.collect.await(timeout)
	} 
	
}
