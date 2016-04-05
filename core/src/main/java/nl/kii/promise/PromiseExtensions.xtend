package nl.kii.promise

import com.google.common.util.concurrent.AbstractFuture
import java.util.List
import java.util.Map
import java.util.concurrent.Future
import nl.kii.async.options.AsyncDefault
import nl.kii.stream.Stream
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import nl.kii.util.AssertionException
import nl.kii.util.Opt
import nl.kii.util.Period

import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.util.OptExtensions.*

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
		new SubPromise<I, O>(AsyncDefault.options) => [ set(from, value) ]
	}
	
	/** Create a promise of a pair */
	def static <K, V> promisePair(Pair<Class<K>, Class<V>> type) {
		new Promise<Pair<K, V>>
	}

	/** Distribute work using an asynchronous method */	
	def static <I, I2, O, P extends IPromise<I2, O>> IPromise<?, List<O>> call(List<I> data, int concurrency, (I)=>P operationFn) {
		stream(data)
			.call(concurrency, operationFn)
			.collect // see it as a list of results
			=> [ options.operation = 'call(concurrency=' + concurrency + ')' ]
	}

	/** Shortcut for quickly creating a completed task */	
	def static Task complete() {
		new Task => [ complete ]
	}

	/** Shortcut for quickly creating a completed task */	
	def static <I> complete(I from) {
		new SubTask(AsyncDefault.options) => [ complete(from) ]
	}

	/** Shortcut for quickly creating a promise with an error */	
	def static Task error(String message) {
		new Task => [ it.error(new Exception(message)) ]
	}

	/** Shortcut for quickly creating a promise with an error */	
	def static <T> Promise<T> error(Class<T> cls, String message) {
		new Promise<T> => [ it.error(new Exception(message)) ]
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
		promises.map[asTask].stream.call[ Task it | it ].collect.asTask
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
				.then [ task.complete ]
				.on(Throwable) [ task.error(it) ]
		}
		task
	}

	// COMPLETING TASKS ///////////////////////////////////////////////////////

	/** Always call onResult, whether the promise has been either fulfilled or had an error. */
	def static <I, O> always(IPromise<I, O> promise, Procedures.Procedure1<Entry<?, O>> resultFn) {
		promise.then [ resultFn.apply(new Value(null, it)) ]
		promise.on(Throwable) [ resultFn.apply(new Error(null, it)) ]
		promise
	}
	
	// OPERATORS //////////////////////////////////////////////////////////////
	
	/** Fulfill a promise */
	def static <T> >> (T value, Promise<T> promise) {
		promise.set(value)
		promise
	}
	
	/** Fulfill a promise */
	def static <T> << (Promise<T> promise, T value) {
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
	def static <I, O, R> SubPromise<I, R> map(IPromise<I, O> promise, (I, O)=>R mappingFn) {
		val newPromise = new SubPromise<I, R>(promise)
		promise
			.then [ r, it | newPromise.set(r, mappingFn.apply(r, it)) ]
			.on(Throwable, true) [ r, it | newPromise.error(r, it) ]
		newPromise => [ options.operation = 'map' ]
	}
	
	// ASYNC MAPPING //////////////////////////////////////////////////////////
	
	/** Asynchronously transform the value of a promise */ 
	def static <I, O, R, P extends IPromise<?, R>> call(IPromise<I, O> promise, (O)=>P promiseFn) {
		promise.map(promiseFn).resolve
			=> [ options.operation = 'call' ]
	}

	/** Asynchronously transform the value of a promise */ 
	def static <I, O, R, P extends IPromise<?, R>> call(IPromise<I, O> promise, (I, O)=>P promiseFn) {
		promise.map(promiseFn).resolve
			=> [ options.operation = 'call' ]
	}

	// CHECKS /////////////////////////////////////////////////////////////////
	
	/**
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <I, O> check(IPromise<I, O> promise, String checkDescription, (O)=>boolean checkFn) {
		promise.check(checkDescription) [ from, it | checkFn.apply(it) ]
	}

	/** 
	 * Check on each value if the assert/check description is valid.
	 * Throws an Exception with the check description if not.
	 */
	def static <I, O> check(IPromise<I, O> promise, String checkDescription, (I, O)=>boolean checkFn) {
		promise.effect [ from, it |
			if(!checkFn.apply(from, it)) throw new AssertionException(checkDescription + '- for value: ' + it + ' \nand promise input: ' + from)
		]
	}

	// SIDEEFFECTS ////////////////////////////////////////////////////////////
	
	/** Perform some side-effect action based on the promise. */
	def static <I, O> effect(IPromise<I, O> promise, (O)=>void listener) {
		promise.then [ r, it | listener.apply(it) ]
	}

	/** Perform some side-effect action based on the promise. */
	def static <I, O> effect(IPromise<I, O> promise, (I, O)=>void listener) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.then [ in, out |
				try {
					listener.apply(in, out)
					newPromise.set(in, out)
				} catch(Throwable t) {
					newPromise.error(in, t)
				} 
			]
			.on(Throwable, true) [ in, out | newPromise.error(in, out) ]
		newPromise => [ options.operation = 'effect' ]
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
			=> [ options.operation = 'perform' ]
	}
	
	// RESPOND TO ERRORS, BUT DO NOT SWALLOW THE ERROR ////////////////////////
	
	/** Listen for an error coming from the promise. Does not swallow the error. */
	def static <T extends Throwable, I, O> on(IPromise<I, O> promise, Class<T> errorType, (T)=>void handler) {
		promise.on(errorType, false) [ from, it | handler.apply(it) ]
	}

	/** Listen for an error coming from the promise. Does not swallow the error. */
	def static <T extends Throwable, I, O>  on(IPromise<I, O> promise, Class<T> errorType, (I, T)=>void handler) {
		promise.on(errorType, false, handler)
	}

	// ASYNCHRONOUSLY TRANSFORM ERRORS INTO A SIDEEFFECT //////////////////////

	/** Asynchronously transform an error into a sideeffect. Swallows the error. */
	def static <I, O> perform(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>IPromise<?, ?> handler) {
		promise.perform(errorType) [ from, it | handler.apply(it) ]
	}
	
	/** Asynchronously transform an error into a sideeffect. Swallows the error. */
	def static <I, O> perform(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>IPromise<?, ?> handler) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			// catch the specific error
			.on(errorType, true) [ from, e |
				// apply the mapping and throw away the result. pass any error to the subpromise.
				try {
					handler.apply(from, e)
						.on(Throwable) [ newPromise.error(from, it) ]
				} catch(Exception e2) {
					newPromise.error(from, e2)
				}
			]
			// if a specific error was not caught, propagate the throwable
			.on(Throwable, true) [ from, e | newPromise.error(from, e) ]
			// pass a normal value
			.then [ from, e | newPromise.set(from, e) ]
		newPromise => [ options.operation = 'call(' + errorType.simpleName + ')' ]
	}
	
	// MAP ERRORS INTO A VALUE ////////////////////////////////////////////////

	/** Map an error back to a value. Swallows the error. */
	def static <I, O> map(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>O mappingFn) {
		promise.map(errorType) [ from, it | mappingFn.apply(it) ]
	}
	
	/** Map an error back to a value. Swallows the error. */
	def static <I, O> map(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>O mappingFn) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			// catch the specific error
			.on(errorType, false) [ from, e |
				// apply the mapping and set the result to the new promise
				try {
					if(!newPromise.fulfilled) {
						val value = mappingFn.apply(from, e)
						newPromise.set(from, value)
					}
				} catch(Throwable e2) {
					newPromise.error(from, e2)
				}
			]
			// if a specific error was not caught, propagate the throwable
			.on(Throwable, true) [ from, e | newPromise.error(from, e) ]
			// pass a normal value
			.then [ from, e | newPromise.set(from, e) ]
		newPromise => [ options.operation = 'map(' + errorType.simpleName + ')' ]
	}
//
//	// ASYNCHRONOUSLY MAP ERRORS INTO A VALUE /////////////////////////////////

	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <I, O> call(IPromise<I, O> promise, Class<? extends Throwable> errorType, (Throwable)=>IPromise<?, O> mappingFn) {
		promise.call(errorType) [ i, e | mappingFn.apply(e) ]
	}
			
	/** Asynchronously map an error back to a value. Swallows the error. */
	def static <I, O> call(IPromise<I, O> promise, Class<? extends Throwable> errorType, (I, Throwable)=>IPromise<?, O> mappingFn) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			// catch the specific error
			.on(errorType, false) [ from, e |
				// apply the mapping and set the result to the new promise
				try {
					mappingFn.apply(from, e)
						.then [ newPromise.set(from, it) ]
						.on(Throwable) [ newPromise.error(from, it) ]
				} catch(Exception e2) {
					newPromise.error(from, e2)
				}
			]
			// pass a normal value
			.then [ from, e | newPromise.set(from, e) ]
			// if a specific error was not caught, propagate the throwable
			.on(Throwable, true) [ from, e | newPromise.error(from, e) ]
		newPromise => [ options.operation = 'call(' + errorType.simpleName + ')' ]
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	/**
	 * Creates a new promise from an existing promise, 
	 * giving you an entry and a promise to complete, 
	 * and letting you decide how that entry fulfills the promise.
	 */
	def static <I, O, I2, O2, P extends IPromise<I2, O2>> P transform(IPromise<I, O> promise, (Entry<I, O>, SubPromise<I2, O2>)=>void mapFn) {
		val newPromise = new SubPromise<I2, O2>(promise)
		promise.onChange [ entry |
			try {
				mapFn.apply(entry, newPromise)
			} catch(Throwable t) {
				newPromise.apply(new Error(null, t))
			}
		]
		newPromise as P => [ options.operation = 'transform' ]
	}

	/** Create a stream out of a promise of a stream. */
	def static <I, P extends IPromise<I, Stream<T>>, T> toStream(P promise) {
		val newStream = new Stream<T>
		promise
			.then [ wrappedStream | wrappedStream.pipe(newStream) ] 
			.on(Throwable) [ newStream.error(it) ]
		newStream
	}

	/** Resolve a promise of a promise to directly a promise. */
	def static <I, O, P extends IPromise<?, O>> resolve(IPromise<I, P> promise) {
		val newPromise = new SubPromise<I, O>(promise)
		promise
			.then [ r, p |
				p
					.then [ newPromise.set(r, it) ]
					.on(Throwable) [ newPromise.error(r, it) ] 
			]
			.on(Throwable, true) [ r, e | newPromise.error(r, e) ]
		newPromise => [ options.operation = 'resolve' ]
	}

	/** Flattens a promise of a promise to directly a promise. Alias of .resolve */
	def static <I1, I2, O, P extends IPromise<I1, O>> flatten(IPromise<I2, P> promise) {
		promise.resolve => [ options.operation = 'flatten' ]
	}

	/** Performs a flatmap, which is a combination of map and flatten/resolve. Alias of .call */	
	def static <I, O, R, P extends IPromise<I, R>> IPromise<I, R> flatMap(IPromise<I, O> promise, (O)=>P promiseFn) {
		promise.call(promiseFn) => [ options.operation = 'flatMap' ]
	}	

	/** 
	 * Transform the input of a promise based on the existing input and output.
	 * <p>
	 * The input mapping function is passed both an input and an output parameter. The output
	 * is only available when a normal value comes in, and not for errors or finishes, in which
	 * case it is none.
	 */	
	def static <I, O, T> IPromise<T, O> mapInput(IPromise<I, O> promise, (I, Opt<O>)=>T inputMappingFn)	{
		val newPromise = new SubPromise<T, O>(promise)
		promise
			.then [ in, it | newPromise.set(inputMappingFn.apply(in, some(it)), it) ]
			.on(Throwable, true) [ in, e | newPromise.error(inputMappingFn.apply(in, none), e) ]
		newPromise => [ options.operation = 'mapInput' ]
	}

	/** 
	 * Creates a new promise without the input data of the original promise.
	 */
	def static <T> IPromise<T, T> removeInput(IPromise<?, T> promise) {
		promise.mapInput [ in, out | out.orNull ]
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
	def static <I, O> wait(IPromise<I, O> promise, Period period, (Period)=>Task timerFn) {
		promise.perform [ timerFn.apply(period) ]
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	/** Convert or forward a promise to a task */	
	def static <I, O> asTask(IPromise<I, O> promise) {
		new Task(promise.options) => [ promise.completes(it) ]
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, O, O2> pipe(IPromise<I, O> promise, SubPromise<I, O> target) {
		promise
			.then [ in, it | target.set(in, it) ]
			.on(Throwable, true) [ in, e | target.error(in, e) ]
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, O, O2> pipe(IPromise<I, O> promise, Promise<O> target) {
		promise
			.effect [ in, it | target.set(it) ]
			.on(Throwable, true) [ in, e | target.error(e) ]
	}

	/** Forward the events from this promise to another promise of the same type */
	def static <I, I2, O> completes(IPromise<I, O> promise, Task task) {
		promise
			.effect [ r, it | task.set(true) ]
			.on(Throwable, true) [ r, it | task.error(it) ]
	}

	/** 
	 * Convert a promise into a future which can block the thread until there is a value by calling .get().
	 * Use with care, blocking may be evil!
	 */
	def static <T> Future<T> asFuture(IPromise<?, T> promise) {
		new AbstractFuture<T> {
			def observe(IPromise<?, T> promise) {
				promise
					.then [ set(it) ]
					.on(Throwable) [ this.exception = it ]
			}
		} => [ observe(promise) ]
	}

}
