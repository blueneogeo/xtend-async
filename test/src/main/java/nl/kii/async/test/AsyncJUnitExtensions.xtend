package nl.kii.async.test

import java.util.List
import nl.kii.promise.IPromise
import nl.kii.stream.message.Error
import nl.kii.stream.message.Finish
import nl.kii.stream.message.Value

import static org.junit.Assert.*
import static extension nl.kii.util.DateExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import nl.kii.stream.IStream
import nl.kii.util.Period
import java.util.concurrent.TimeUnit
import nl.kii.stream.message.Entry
import nl.kii.promise.Promise
import java.util.concurrent.ExecutionException

class AsyncJUnitExtensions {

	var static DEFAULT_TIMEOUT = 5.secs

	/** 
	 * Extract the entries from the stream, blocking until the stream finishes.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 * WARNING: it will wait until the stream finishes, so a Finish must be put on the stream.
	 */
	def static <T> entries(IStream<?, T> stream) {
		stream.entries(DEFAULT_TIMEOUT)
	}

	/** 
	 * Extract the entries from the stream, blocking until the stream finishes.
	 * Throws a timeout exception if there is no result within the timeout period.
	 * WARNING: it will wait until the stream finishes, so a Finish must be put on the stream.
	 */
	def static <T> entries(IStream<?, T> stream, Period timeout) {
		val List<Entry<Object, T>> results = newArrayList
		val promise = new Promise<List<Entry<Object, T>>>
		stream.on [ r |
			r.each [ i, o | results.add(value(i, o)) r.stream.next ]
			r.finish [ i, l | results.add(finish(i, l)) r.stream.next ]
			r.error[ i, e | results.add(error(e)) r.stream.next ]
			r.closed [ promise.set(results) ]
		]
		stream.next
		stream.close
		promise.asFuture.get(timeout.ms, TimeUnit.MILLISECONDS)
	}

	/** 
	 * Check if a stream has or will have the values in the list. Will block until the results are in.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 * WARNING: it will wait until the stream finishes, so a Finish must be put on the stream.
	 */
	def static <T> assertEquals(List<T> value, IStream<?, T> stream) {
		assertEquals(value, stream, DEFAULT_TIMEOUT)
	}

	/** 
	 * Check if a stream has or will have the values in the list. Will block until the results are in.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 * WARNING: it will wait until the stream finishes, so a Finish must be put on the stream.
	 */
	def static <T> <=> (IStream<?, T> stream, List<T> value) {
		assertEquals(value, stream, DEFAULT_TIMEOUT)
	}

	/** 
	 * Check if a stream has or will have the values in the list. Will block until the results are in.
	 * Will throw a timeout exception after the timeout period has been exceeded without the list completed.
	 * WARNING: it will wait until the stream finishes, so a Finish must be put on the stream.
	 */
	def static <T> assertEquals(List<T> value, IStream<?, T> stream, Period timeout) {
		try {	
			assertEquals(value, stream.collect.first.asFuture.get(timeout.ms, TimeUnit.MILLISECONDS))
		} catch(ExecutionException e) {
			throw e.cause
		}
	}

	/** 
	 * Check if a promise has or will have the specified value. Will block until the results are in.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 */
	def static <T> assertEquals(T value, IPromise<?, T> promise) {
		assertEquals(value, promise, DEFAULT_TIMEOUT)
	}

	/** 
	 * Check if a promise has or will have the specified value. Will block until the results are in.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 */
	def static <T> <=> (IPromise<?, T> promise, T value) {
		assertEquals(value, promise, DEFAULT_TIMEOUT)
	}

	/** 
	 * Check if a promise has or will have the specified value. Will block until the results are in.
	 * Will throw a timeout exception after the timeout period has been exceeded without a value.
	 */
	def static <T> assertEquals(T value, IPromise<?, T> promise, Period timeout) {
		try {
			assertEquals(value, promise.asFuture.get(timeout.ms, TimeUnit.MILLISECONDS))
		} catch(ExecutionException e) {
			throw e.cause
		}
	}

	/** 
	 * Check if a promise has or will have the specified list. Will block until the results are in.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 */
	def static <T> void assertEquals(List<T> value, IPromise<?, List<T>> promise) {
		assertEquals(value, promise, DEFAULT_TIMEOUT)
	}

	/** 
	 * Check if a promise has or will have the specified list. Will block until the results are in.
	 * Throws a timeout exception if there is no result after the period in AsyncAssert.DEFAULT_TIMEOUT
	 */
	def static <T> void <=> (IPromise<?, List<T>> promise, List<T> value) {
		assertEquals(value, promise, DEFAULT_TIMEOUT)
	}

	/** 
	 * Check if a promise has or will have the specified list. Will block until the results are in.
	 * Will throw a timeout exception after the timeout period has been exceeded without a value.
	 */
	def static <T> void assertEquals(List<T> value, IPromise<?, List<T>> promise, Period timeout) {
		try {
			assertArrayEquals(value, promise.asFuture.get(timeout.ms, TimeUnit.MILLISECONDS))
		} catch(ExecutionException e) {
			throw e.cause
		}
	}

	/** Lets you easily pass a value entry using the << or >> operators */
	def static <I, T> value(T value) {
		new Value<I, T>(null, value)
	}

	/** Lets you easily pass a value entry using the << or >> operators */
	def static <I, T> value(I in, T value) {
		new Value<I, T>(in, value)
	}

	/** Lets you easily pass an error entry using the << or >> operators */
	def static <I, T> error(Throwable t) {
		new Error<I, T>(null, t)
	}
	
	/** Lets you easily pass a Finish entry using the << or >> operators */
	def static <I, O> finish() {
		new Finish<I, O>(null, 0)
	}

	/** Lets you easily pass a Finish entry using the << or >> operators */
	def static <I, O> finish(int level ) {
		new Finish<I, O>(null, level)
	}

	/** Lets you easily pass a Finish entry using the << or >> operators */
	def static <I, O> finish(I in, int level ) {
		new Finish<I, O>(in, level)
	}

	
}
