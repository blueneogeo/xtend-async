package nl.kii.stream

import java.util.LinkedList
import java.util.List
import java.util.concurrent.atomic.AtomicReference
import nl.kii.promise.IPromise

import static extension nl.kii.stream.StreamExtensions.*
import static extension org.junit.Assert.*

class StreamAssert {
	
	/** pull all queued data from a stream put it in a list, and print any error */
	def static <T> List<Entry<T>> gather(Stream<T> stream) {
		val data = new LinkedList<Entry<T>>
		stream
			.onFinish [ data.add(new Finish(level)) ]
			.onEach [ data.add(value) ]
		data
	}

	def static <T> assertStreamContains(Stream<T> stream, Entry<T>... entries) {
		val data = stream.gather
		println(data)
		assertArrayEquals(entries, data)
	}

	def static assertFulfilled(IPromise<Boolean> promise) {
		promise.then[] // force start
		promise.fulfilled.assertTrue
	}

	def static <T> assertPromiseEquals(IPromise<T> promise, T value) {
		val ref = new AtomicReference<T>
		promise.then[ ref.set(it) ]
		promise.fulfilled.assertTrue
		ref.get.assertEquals(value)
	}

	def static <T> void assertPromiseEquals(IPromise<List<T>> promise, List<T> value) {
		val ref = new AtomicReference<List<T>>
		promise.then[ ref.set(it) ]
		promise.fulfilled.assertTrue
		ref.get.assertArrayEquals(value)
	}
	
	def static <T> value(T value) {
		new Value<T>(value)
	}
	
}