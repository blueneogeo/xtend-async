package nl.kii.async.annotation

import java.lang.annotation.Target
import nl.kii.async.processors.AsyncProcessor
import org.eclipse.xtend.lib.macro.Active

/**
 * Marks a method as an asynchronous method, meaning that it promises a result in the future.
 * <p>
 * Async methods can return immediately and return a Promise or Task (a Promise<Boolean>).
 * You can use the .then and .onError on the task to handle the result of the method.
 * <p>
 * Async is an active annotation that creates a shadow function which is the actual function to call.
 * This shadow function automatically creates the Promise or Task that is in the parameters and
 * returns it. It also catches any thrown errors in the method code and puts reports it as an
 * error to the Promise or Task so you may listen to it.
 * <p>
 * <h3>example:</h3>
 * <pre>
 * @Async 
 * def increment(int number, Promise<Integer> promise) {
 *     promise << number + 1
 * }
 * </pre>
 * <p>
 * You can then use this code like this:
 * <p>
 * <pre>
 * increment(3).then [ println(it) ] // prints 4
 * </pre>
 * <p>
 * The created code by the @Asyc is a wrapper method that fills in the Promise for you
 * and performs the wrapper, and is like follows: 
 * </p>
 * <pre>
 * def Promise<Integer> increment(int number) {
 *     val promise = new Promise<Integer>
 *     try {
 *         increment(number, promise) // call to the original method
 *     } catch(Exception e) {
 *         promise.error(e)
 *     } finally {
 *         return promise
 *     }
 * }
 * </pre>
 * <p>
 * By default, the Async annotations support multithreaded execution.
 * If you want to signal that the method is not threadsafe or meant for multithreaded
 * execution, you can pass false: @Async(false).
 * 
 * Unless you mark the annotation as not threadsafe, the annotation also adds an executor method, 
 * that lets you easily run the method on an ExecutorService:
 * <pre>
 * val exec = Executors.newCachedTheadPool
 * exec.increment(3).then [ println(it) ] // prints 4, but is run on a different thread
 * </pre>
 * <p>
 */
@Active(AsyncProcessor)
@Target(METHOD)
annotation Async {
	/** 
	 * Indicates the async method supports multithreading. 
	 * If set to true, the executor method will be added.
	 */
	boolean value = false
}
