package nl.kii.promise

import java.util.List
import java.util.Map
import nl.kii.promise.internal.PromiseException
import nl.kii.promise.internal.SubPromise
import nl.kii.stream.Stream
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static extension nl.kii.stream.StreamExtensions.*

class PromiseExtensions {
	
	// CREATING PROMISES AND TASKS ////////////////////////////////////////////
	
	/** Create a promise of the given type */
	def static <T> promise(Class<T> type) {
		new Promise<T>
	}

	/** Create a promise of a list of the given type */
	def static <T> promiseList(Class<T> type) {
		new Promise<List<T>>
	}

	/** Create a promise of a map of the given key and value types */
	def static <K, V> promiseMap(Pair<Class<K>, Class<V>> type) {
		new Promise<Map<K, V>>
	}
	
	/** 
	 * Create a promise that immediately resolves to the passed value. 
	 */
	def static <T> promise(T value) {
		new Promise<T>(value)
	}

	def static <I, O> promise(I from, O value) {
		new SubPromise<I, O> => [ set(from, value) ]
	}
	
	/** Create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Promise<Pair<K, V>>
	}

	/** Distribute work using an asynchronous method */	
	def static <I, I2, O, P extends IPromise<I2, O>> IPromise<I, List<O>> call(List<I> data, int concurrency, (I)=>P operationFn) {
		stream(data)
			.call(concurrency, operationFn)
			.collect // see it as a list of results
			.first
			=> [ operation = 'call(concurrency=' + concurrency + ')' ]
	}

	/** Shortcut for quickly creating a completed task */	
	def static Task complete() {
		new Task => [ complete ]
	}

	/** Shortcut for quickly creating a promise with an error */	
	def static Task error(String message) {
		new Task => [ it.error(message) ]
	}

	/** Shortcut for quickly creating a promise with an error */	
	def static <T> Promise<T> error(Class<T> cls, String message) {
		new Promise<T> => [ it.error(message) ]
	}
	
	/** 
	 * Create a new Task that completes when all wrapped tasks are completed.
	 * Errors created by the tasks are propagated into the resulting task.
	 */
	def static Task all(IPromise<?, ?>... promises) {
		all(promises.toList)
	}

	/** 
	 * Create a new Task that completes when all wrapped tasks are completed.
	 * Errors created by the tasks are propagated into the resulting task.
	 */
	def static Task all(Iterable<? extends IPromise<?, ?>> promises) {
		promises.map[asTask].stream.call[ Task it | it ].collect.first.asTask
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <I, O, P extends IPromise<I, O>> Task any(P... promises) {
		any(promises.toList)
	}
	
	/** 
	 * Create a new Task that completes when any of the wrapped tasks are completed
	 * Errors created by the promises are propagated into the resulting task
	 */
	def static <I, O> Task any(List<? extends IPromise<I, O>> promises) {
		val Task task = new Task
		for(promise : promises) {
			promise
				.on(Throwable) [ task.error(it) ]
				.then [ task.complete ]
		}
		task
	}

	// COMPLETING TASKS ///////////////////////////////////////////////////////

	/** Always call onResult, whether the promise has been either fulfilled or had an error. */
	def static <I, O> always(IPromise<I, O> promise, Procedures.Procedure1<Entry<?, O>> resultFn) {
		promise.on(Throwable) [ resultFn.apply(new Error(null, it)) ]
		promise.then [ resultFn.apply(new Value(null, it)) ]
		promise
	}
	
	/** Tell the promise it went wrong */
	def static <I, O> error(IPromise<I, O> promise, String message) {
		promise.error(new Exception(message))
	}

	/** Tell the promise it went wrong, with the cause throwable */
	def static <I, O> error(IPromise<I, O> promise, String message, Throwable cause) {
		promise.error(new Exception(message, cause))
	}

	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	def static <I, O> >> (I value, IPromise<I, O> promise) {
		promise.set(value)
		promise
	}
	
	/** Fulfill a promise */
	def static <I, O> << (IPromise<I, O> promise, I value) {
		promise.set(value)
		promise
	}
	
	/** All/And */
	def static Task && (IPromise<?, ?> p1, IPromise<?, ?> p2) {
		all(p1, p2)
	}
	
	/** Any/Or */
	def static <I, O> Task || (IPromise<I, O> p1, IPromise<I, O> p2) {
		any(p1, p2)
	}
	
	// MAPPING ////////////////////////////////////////////////////////////////
	
	/** Transform the value of a promise */ 
	def static <I, O, R> map(IPromise<I, O> promise, (O)=>R mappingFn) {
		promise.map [ r, it | mappingFn.apply(it) ]
	}

	/** Transform the value of a promise */ 
	def static <I, O, R> map(IPromise<I, O> promise, (I, O)=>R mappingFn) {
		val newPromise = new SubPromise<I, R>(promise)
		promise
			.on(Throwable) [ r, it | newPromise.error(r, it) ]
			.then [ r, it | newPromise.set(r, mappingFn.apply(r, it)) ]
			.on(Throwable) [ newPromise.error(it) ]
		newPromise => [ operation = 'map' ]
	}
	
	// ASYNC MAPPING //////////////////////////////////////////////////////////
	
	/** Asynchronously transform the value of a promise */ 
	def static <I, O, R, P extends IPromise<?, R>> call(IPromise<I, O> promise, (O)=>P promiseFn) {
		promise.map(promiseFn).resolve
			=> [ operation = 'call' ]
	}

	/** Asynchronously transform the value of a promise */ 
	def static <I, O, R, P extends IPromise<?, R>> call(IPromise<I, O> promise, (I, O)=>P promiseFn) {
		promise.map(promiseFn).resolve
			=> [ operation = 'call' ]
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////
	
	/** Perform some side-effect action based on the promise. */
	def static <I, O> effect(IPromise<I, O> promise, (O)=>void listener) {
		promise.effect [ r, it | listener.apply(it) ]
	}

	/** Perform some side-effect action based on the promise. */
	def static <I, O> effect(IPromise<I, O> promise, (I, O)=>void listener) {
		promise.map [ r, it | listener.apply(r, it) return it ]
			=> [ operation = 'effect' ]
	}
	
	// ASYNC SIDEEFFECTS //////////////////////////////////////////////////////
	
	/** Asynchronously perform some side effect based on the result of the promise */
	def static <I, O> perform(IPromise<I, O> promise, (O)=>IPromise<?,?> promiseFn) {
		promise.perform [ i, o | promiseFn.apply(o) ]
	}
	
	/** Asynchronously perform some side effect based on the result of the promise */
	def static <I, O> perform(IPromise<I, O> promise, (I, O)=>IPromise<?,?> promiseFn) {
		promise
			.map[ i, o | promiseFn.apply(i, o).map[o] ]
			.resolve
			=> [ operation = 'perform' ]
	}
	
	// MONITORING ERRORS //////////////////////////////////////////////////////
	
	
	
	/** Listen for an error coming from the promise. Does not swallow the error. */
	def static <I, O> on(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>void handler) {
		promise.on(errorType, false) [ from, it | handler.apply(it) ] 
	}

	/** Listen for an error coming from the promise. Does not swallow the error. */
	def static <I, O> on(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>void handler) {
		promise.on(errorType, false, handler)
	}

	/** Listen for an error coming from the promise. Swallows the the error. */
	def static <I, O> effect(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>void handler) {
		promise.on(errorType, true) [ from, it | handler.apply(it) ] 
	}

	/** Listen for an error coming from the promise. Swallows the error. */
	def static <I, O> effect(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>void handler) {
		promise.on(errorType, true, handler)
	}
	
	// TRANSFORMING ERRORS /////////////////////////////////////////////////////

	/** 
	 * Map an error to a new PromiseException with a message, 
	 * passing the value, and with the original error as the cause.
	 */
	def static <I, O> map(IPromise<I, O> promise, Class<? extends Throwable> errorType, String message) {
		promise.effect(errorType) [ from, e | throw new PromiseException(message, from, e) ]
	}

	/** Map an error back to a value. Swallows the error. */
	def static <I, O> map(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>O mappingFn) {
		promise.map(errorType) [ from, it | mappingFn.apply(it) ]
	}
	
	/** Map an error back to a value. Swallows the error. */
	def static <I, O> map(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>O mappingFn) {
		val newPromise = new SubPromise<I, O>(promise, false)
		promise
			// catch the specific error
			.on(errorType, true) [ from, e |
				// apply the mapping and set the result to the new promise
				try {
					if(!newPromise.fulfilled) {
						val value = mappingFn.apply(from, e)
						newPromise.set(from, value)
					}
				} catch(Exception e2) {
					newPromise.error(from, e2)
				}
			]
			// if a specific error was not caught, propagate the throwable
			.on(Throwable) [ from, e | newPromise.error(from, e) ]
			// pass a normal value
			.then [ from, e | newPromise.set(from, e) ]
		newPromise => [ operation = 'map(' + errorType.simpleName + ')' ]
	}

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <I, O> call(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>IPromise<?, O> mappingFn) {
		promise.call(errorType) [ i, e | mappingFn.apply(e) ]
	}
			
	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <I, O> call(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>IPromise<?, O> mappingFn) {
		val newPromise = new SubPromise<I, O>(new Promise<I>, false)
		promise
			// catch the specific error
			.on(errorType, true) [ from, e |
				// apply the mapping and set the result to the new promise
				try {
					mappingFn.apply(from, e)
						.on(Throwable) [ newPromise.error(from, it) ]
						.then [ newPromise.set(from, it) ]
				} catch(Exception e2) {
					newPromise.error(from, e2)
				}
			]
			// if a specific error was not caught, propagate the throwable
			.on(Throwable) [ from, e | newPromise.error(from, e) ]
			// pass a normal value
			.then [ from, e | newPromise.set(from, e) ]
		newPromise => [ operation = 'call(' + errorType.simpleName + ')' ]
	}

	@Deprecated
	def static <I, O> onErrorMap(IPromise<I, O> promise, (Throwable)=>O mappingFn) {
		promise.map(Throwable, mappingFn)
	}

	@Deprecated
	def static <I, I2, O> onErrorCall(IPromise<I, O> promise, (Throwable)=>IPromise<I2, O> mappingFn) {
		promise.call(Throwable, mappingFn)
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/** Create a new promise with a new input, defined by the inputFn */
	def static <I1, I2, O> mapInput(IPromise<I1, O> promise, (I1, O)=>I2 inputFn) {
		val newPromise = new SubPromise<I2, O>(new Promise<I2>)
		promise
			.on(Throwable) [ r, e | newPromise.error(inputFn.apply(r, null), e) ]
			.then [ r, it | newPromise.set(inputFn.apply(r, it), it) ]
		newPromise => [ operation = 'input' ]
	}
	
	/** Create a stream out of a promise of a stream. */
	def static <I, P extends IPromise<I, Stream<T>>, T> toStream(P promise) {
		val newStream = new Stream<T>
		promise
			.on(Throwable) [ newStream.error(it) ]
			.then [ s | s.pipe(newStream) ] 
		newStream
	}

	/** Resolve a promise of a promise to directly a promise. */
	def static <I, O, P extends IPromise<?, O>> resolve(IPromise<I, P> promise) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.on(Throwable) [ r, e | newPromise.error(r, e) ]
			.then [ r, p |
				p
					.then [ newPromise.set(r, it) ]
					.on(Throwable) [ newPromise.error(r, it) ] 
			]
		newPromise => [ operation = 'resolve' ]
	}

	/** Flattens a promise of a promise to directly a promise. Alias of .resolve */
	def static <I1, I2, O, P extends IPromise<I1, O>> flatten(IPromise<I2, P> promise) {
		promise.resolve => [ operation = 'flatten' ]
	}

	/** Performs a flatmap, which is a combination of map and flatten/resolve. Alias of .call */	
	def static <I, O, R, P extends IPromise<I, R>> IPromise<I, R> flatMap(IPromise<I, O> promise, (O)=>P promiseFn) {
		promise.call(promiseFn) => [ operation = 'flatMap' ]
	}	

	// TIMING /////////////////////////////////////////////////////////////////

	/** 
	 * Create a new promise that delays the output (not the error) of the existing promise.
	 * The idea here is that since timing has to be executed on another thread, instead of 
	 * having a threaded implementation, this method requires you to call your own implementation.
	 * To do so, you implement the timerFn, and then pass it.
	 * <p>
	 * Example:
	 * <pre>
		val exec = Executors.newSingleThreadScheduledExecutor
		val timerFn = [ long delayMs, =>void fn | exec.schedule(fn, delayMs, TimeUnit.MILLISECONDS) return ]
		complete.wait(100, timerFn).then [ anyDone = true ]
	 * </pre>
	 * @param timerFn a function with two parameters: a delay in milliseconds, and a closure
	 * 			calling the function should execute the closure after the delay.
	 */	
	def static <I, O> wait(IPromise<I, O> promise, long periodMs, Procedure2<Long, Procedure0> timerFn) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.on(Throwable) [ newPromise.error(it) ]
			.then [ input, value |
				timerFn.apply(periodMs) [
					newPromise.set(input, value)
				]
			]
		newPromise
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	@Deprecated
	def static <I, O> onError(IPromise<I, O> promise, (Throwable)=>void handler) {
		promise.on(Throwable, handler)
	}
	
	@Deprecated
	def static <I, O> onErrorThrow(IPromise<I, O> promise, (I, Throwable)=>Exception exceptionFn) {
		promise.on(Throwable) [ i, t | throw exceptionFn.apply(i, t) ]
	}

	@Deprecated
	def static <I, O> onErrorThrow(IPromise<I, O> promise, String message) {
		promise.on(Throwable) [ i, t | throw new Exception(message + ', for input ' + i, t) ]
	}

	/** Convert or forward a promise to a task */	
	def static <I, O> asTask(IPromise<I, O> promise) {
		val task = new Task
		promise.completes(task)
		task
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, O, O2> pipe(IPromise<I, O> promise, IPromise<O, O2> target) {
		promise
			.on(Throwable) [ target.error(it) ]
			.then [ target.set(it) ]
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, I2, O> completes(IPromise<I, O> promise, Task task) {
		promise
			.on(Throwable) [ r, it | task.error(it) ]
			.then [ r, it | task.set(true) ]
			.on(Throwable) [ r, it | task.error(it) ]
	}

}
