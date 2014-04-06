package nl.kii.stream

import java.util.List
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test

import static extension nl.kii.stream.StreamExt.*
import static extension org.junit.Assert.*

class TestStream {

	@Test
	def void testBasicPushMapAndEach() {
		val s = new Stream<Integer>
			.map [ it + 4 ]
			.each [ println(it) ]
			.open
		s << 1 << 2 << 3
	}

	@Test
	def void testFilter() {
		val s = new Stream<Integer>
		s.filter [ it % 2 == 0 ].each [ println(it) ]
		s << 1 << 2 << 3 << 4 << 5 << finish
	}
	
	@Test
	def void testBasicCollect() {
		val s = new Stream<Integer>
		s.collect.then [ println(it) ]
		s << 1 << 2 << 3 << finish
	}
	
	@Test
	def void testSubstream() {
		val s = new Stream<Integer>
		s
			.split[ it % 3 == 0 ]
			.substream
//			.each [ substream | 
//				substream.collect.then [ println(it) ]
//			]
		s << 1 << 2 << 3 << 4 << 5 << 6 << 7 << 8 << 9 << 10
	}
	
	@Test
	def void testSplit() {
		val s = new Stream<Integer>
		s.split [ it % 2 == 0 ].onChange[ print(it + ' ') ]
		s << 1 << 2 << 3 << 4 << 5 << finish
	}
	
	// STREAM ASSERT TOOLS ////////////////////////////////////////////////////
	
	def private <T> assertStream(List<? extends Entry<T>> entries, Stream<T> stream) {
		val List<Entry<T>> found = newLinkedList
		stream.onChange [ found.add(it)	]
		stream.open
		println(found)
		assertArrayEquals(found, entries)
	}
	
	def private assertPromiseFinished(Promise<Boolean> promise) {
		promise.then[] // force start
		promise.finished.assertTrue
	}

	def private <T> assertPromiseEquals(Promise<T> promise, T value) {
		val ref = new AtomicReference<T>
		promise.then[ ref.set(it) ]
		promise.finished.assertTrue
		ref.get.assertEquals(value)
	}

	def private <T> void assertPromiseEquals(Promise<List<T>> promise, List<T> value) {
		val ref = new AtomicReference<List<T>>
		promise.then[ ref.set(it) ]
		promise.finished.assertTrue
		ref.get.assertArrayEquals(value)
	}
	
	def private <T> value(T value) {
		new Value<T>(value)
	}

	
}
