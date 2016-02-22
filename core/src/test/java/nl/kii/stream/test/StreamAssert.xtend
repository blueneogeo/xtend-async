package nl.kii.stream.test

import java.util.LinkedList
import java.util.List
import java.util.concurrent.atomic.AtomicReference
import nl.kii.promise.IPromise
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Finish
import nl.kii.stream.message.Value

import static extension nl.kii.stream.StreamExtensions.*
import static extension org.junit.Assert.*

class StreamAssert {
	
	/** pull all queued data from a stream put it in a list, and print any error */
	def static <R, T> List<Entry<R, T>> gather(nl.kii.stream.IStream<R, T> stream) {
		val data = new LinkedList<Entry<R, T>>
		stream.on [
			error [ data.add(new Error($0, $1)) stream.next ]
			finish [ data.add(new Finish($0, $1)) stream.next ]
			each [ data.add(new Value($0, $1)) stream.next ]
		]
		stream.next
		data
	}

	def static <R, T> assertStreamContains(nl.kii.stream.IStream<R, T> stream, Entry<R, T>... entries) {
		val data = stream.gather
		println(data)
		assertArrayEquals(entries, data)
	}

	def static assertFulfilled(IPromise<?, Boolean> promise) {
		promise.then[] // force start
		promise.fulfilled.assertTrue
	}

	def static <T> assertPromiseEquals(IPromise<?, T> promise, T value) {
		val ref = new AtomicReference<T>
		promise.then[ ref.set(it) ]
		promise.fulfilled.assertTrue
		ref.get.assertEquals(value)
	}

	def static <T> void assertPromiseEquals(IPromise<?, List<T>> promise, List<T> value) {
		val ref = new AtomicReference<List<T>>
		promise.then[ ref.set(it) ]
		promise.fulfilled.assertTrue
		ref.get.assertArrayEquals(value)
	}
	
	def static <I, T> value(T value) {
		new Value<I, T>(null, value)
	}

	def static <I, T> value(I in, T value) {
		new Value<I, T>(in, value)
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