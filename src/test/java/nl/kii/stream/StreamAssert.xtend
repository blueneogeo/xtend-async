package nl.kii.stream

import java.util.List
import java.util.concurrent.atomic.AtomicReference

import static extension org.junit.Assert.*
import java.util.LinkedList

class StreamAssert {
	
	def static <T> List<Entry<T>> gather(Stream<T> stream) {
		// pull in all data and put in a list, and show any errors
		val data = new LinkedList<Entry<T>>
		stream
			.onFinish [| 
				data.add(new Finish<T>)
				stream.next
			]
			.onError [ 
				printStackTrace
				stream.next
			]
			.onValue [ 
				data.add(value)
				stream.next
			]
		stream.next
		data
	}
	
	def static <T> assertStreamEquals(List<? extends Entry<T>> entries, Stream<T> stream) {
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