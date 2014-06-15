package nl.kii.stream

import java.util.LinkedList
import java.util.List
import java.util.concurrent.atomic.AtomicReference
import nl.kii.promise.Promise

import static extension nl.kii.stream.StreamExtensions.*
import static extension org.junit.Assert.*

class StreamAssert {
	
	/** pull all queued data from a stream put it in a list, and print any error */
	def static <T> List<Entry<T>> gather(Stream<T> stream) {
		val data = new LinkedList<Entry<T>>
		stream.on [
			each [ data.add(value) ]
			error [ printStackTrace ]
			finish [ data.add(new Finish<T>(level)) ]
		]
		data
	}

	def static <T> assertStreamEquals(Stream<T> stream, List<? extends Entry<T>> entries) {
		val data = stream.gather
		println(data)
		assertArrayEquals(entries, data)
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