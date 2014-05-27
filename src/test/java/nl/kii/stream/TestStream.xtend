package nl.kii.stream

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class TestStream {

	@Test
	def void testUnbufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer>
		s.forEach [
			counter.addAndGet(it)
		]
		s << 1 << 2 << 3
		assertEquals(6, counter.get)
	}

	@Test
	def void testBufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3
		s.forEach [ 
			counter.addAndGet(it)
		]
		assertEquals(6, counter.get)
	}
	
	@Test
	def void testControlledStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3 << finish << 4 << 5
		val subscription = s.listenAsync [
			forEach [ counter.addAndGet(it) ]
		]
		subscription.next
		assertEquals(1, counter.get) // next pushes the first number onto the stream
		subscription.skip
		assertEquals(1, counter.get) // skipped to the finish, nothing added
		subscription.next
		assertEquals(1, counter.get) // got the finish, nothing added
		subscription.next
		assertEquals(5, counter.get) // after finish is 4, added to 1
		subscription.next
		assertEquals(10, counter.get) // 5 added
		subscription.next
		assertEquals(10, counter.get) // we were at the end of the stream so nothing changed
		s << 1 << 4
		assertEquals(11, counter.get) // we called next once before, and now that the value arrived, it's added
		subscription.next
		assertEquals(15, counter.get) // 4 added to 11
	}
	
	@Test
	def void testControlledChainedBufferedStream() {
		val result = new AtomicInteger(0)
		val s1 = int.stream << 1 << 2 << 3
		val s2 = s1.map[it] << 4 << 5 << 6
		val subscription = s2.listenAsync [
			forEach [ result.set(it) ]
		]
		subscription.next
		assertEquals(4, result.get)
		subscription.next
		assertEquals(5, result.get)
		subscription.next
		assertEquals(6, result.get)
		subscription.next
		assertEquals(1, result.get)
		subscription.next
		assertEquals(2, result.get)
		subscription.next
		assertEquals(3, result.get)
	}

	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		val e = new AtomicReference<Throwable>
		
		// no onerror set, should throw it
		e.set(null)
		try {
			s.forEach [ println(1/it) ] // handler will throw /0 exception
			s << 0
			fail('should never reach this')
		} catch(ArithmeticException t) {
			e.set(t)
		}
		assertNotNull(e.get)

		// should work again
		e.set(null)
		try {
			s << 0
			println('no error?')
			fail('should never reach this either')
		} catch(ArithmeticException t) {
			e.set(t)
		}
		assertNotNull(e.get)
	}
	
	@Test
	def void testStreamErrors2() {
		val s = new Stream<Integer>
		val e = new AtomicReference<Throwable>
		// now try to catch the error
		s.listen [
			forEach [ println(1/it)] // handler will throw /0 exception
			onError [ e.set(it) ]
		]
		s << 0
		assertNotNull(e.get)
	}

	@Test
	def void testChainedBufferedSkippingStream() {
		val result = new AtomicInteger(0)
		// parent stream, has already something buffered
		val s1 = int.stream << 1 << 2 << finish << 3
		// substream, also has some stuff buffered, which needs to come out first
		val s2 = s1.map[it] << 4 << 5 << finish << 6 << 7
		val sub = s2.listenAsync [
			forEach [ result.set(it) ]
		]
		sub.next // ask the next from the substream
		assertEquals(4, result.get) // which should be the first buffered value
		sub.skip // skip to the finish
		sub.next // results in the finish
		sub.next // process the value after the finish
		assertEquals(6, result.get) // which is 6
		sub.skip // skip, which should first discard 7, then go to the parent stream and discard 1 and 2
		sub.next // results in the finish
		sub.next // results in the 3 after the finish
		assertEquals(3, result.get)
	}

}
