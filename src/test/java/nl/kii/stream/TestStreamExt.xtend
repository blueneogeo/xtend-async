package nl.kii.stream

import java.util.List
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test

import static extension nl.kii.stream.StreamExt.*
import static extension org.junit.Assert.*
import java.util.concurrent.atomic.AtomicInteger

class TestStream {

	@Test
	def void testUnbufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer>
		s.forEach [ counter.addAndGet(it) ]
		s << 1 << 2 << 3
		assertEquals(6, counter.get)
	}

	@Test
	def void testBufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3
		s.forEach [ counter.addAndGet(it) ]
		assertEquals(6, counter.get)
	}

	@Test
	def void testControlledStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3 << finish << 4 << 5
		s.forEach [ it, stream | counter.addAndGet(it) ]
		assertEquals(0, counter.get) // controlled stream, nothing happened yet
		s.next
		assertEquals(1, counter.get) // pushed 1 on the stream
		s.skip
		assertEquals(1, counter.get) // skipped to the finish
		s.next
		assertEquals(5, counter.get) // after finish is 4, added to 1
		s.next
		assertEquals(10, counter.get) // 5 added
		s.next
		assertEquals(10, counter.get) // we were at the end of the stream so nothing changed
		s << 1 << 4
		assertEquals(11, counter.get) // we called next once before, and now that the value arrived, it's added
		s.next
		assertEquals(15, counter.get) // 4 added to 11
	}
	
	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		val e = new AtomicReference<Throwable>
		// s.onError [ e.set(it) ]
		
		// no onerror set, should throw it
		try {
			s.forEach [ val x = 1 / it ]
			s << 0
		} catch(Throwable t) {
			e.set(t)
		}
		assertNotNull(e.get)
		
		// now try to catch the error
		val e2 = new AtomicReference<Throwable>
		e.set(null)
		s.onError [ e2.set(it) ]
		s << 0
		// now e should still be null, and e2 should be set
		assertNull(e.get)
		assertNotNull(e2.get)
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
