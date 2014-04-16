package nl.kii.stream

import java.util.List
import java.util.concurrent.atomic.AtomicReference

import static extension org.junit.Assert.*

class StreamAssert {
	
	def static <T> assertStreamEquals(List<? extends Entry<T>> entries, Stream<T> stream) {
		assertArrayEquals(stream.queue, entries)
	}
	
	def static assertFulfilled(Promise<Boolean> promise) {
		promise.then[] // force start
		promise.fulfilled.assertTrue
	}

	def static <T> assertPromiseEquals(Promise<T> promise, T value) {
		val ref = new AtomicReference<T>
		promise.then[ ref.set(it) ]
		promise.fulfilled.assertTrue
		ref.get.assertEquals(value)
	}

	def static <T> void assertPromiseEquals(Promise<List<T>> promise, List<T> value) {
		val ref = new AtomicReference<List<T>>
		promise.then[ ref.set(it) ]
		promise.fulfilled.assertTrue
		ref.get.assertArrayEquals(value)
	}
	
	def static <T> value(T value) {
		new Value<T>(value)
	}
	
}