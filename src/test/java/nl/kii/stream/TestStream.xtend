package nl.kii.stream

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.stream.StreamExt.*

class TestStream {

	@Test
	def void testUnbufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer>
		s.onNextValue [ 
			counter.addAndGet(it)
			s.next
		]
		s.next
		s << 1 << 2 << 3
		assertEquals(6, counter.get)
	}

	@Test
	def void testBufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3
		s.onNextValue [ 
			counter.addAndGet(it)
			s.next
		]
		s.next
		assertEquals(6, counter.get)
	}
	
	@Test
	def void testControlledStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3 << finish << 4 << 5
		s.onNextValue [
			counter.addAndGet(it)
		]
		s.next
		assertEquals(1, counter.get) // next pushes the first number onto the stream
		s.skip
		assertEquals(1, counter.get) // skipped to the finish, nothing added
		s.next
		assertEquals(1, counter.get) // got the finish, nothing added
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
	def void testControlledChainedBufferedStream() {
		val result = new AtomicInteger(0)
		val s1 = int.stream << 1 << 2 << 3
		val s2 = s1.map[it] << 4 << 5 << 6
		s2.onNextValue [
			result.set(it)
		]
		s2.next
		assertEquals(4, result.get)
		s2.next
		assertEquals(5, result.get)
		s2.next
		assertEquals(6, result.get)
		s2.next
		assertEquals(1, result.get)
		s2.next
		assertEquals(2, result.get)
		s2.next
		assertEquals(3, result.get)
	}

	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		val e = new AtomicReference<Throwable>
		s.onNextValue [
			println(1 / it)
		] // handler will throw /0 exception
		
		// no onerror set, should throw it
		e.set(null)
		try {
			s.next
			s << 0
			fail('should never reach this')
		} catch(ArithmeticException t) {
			e.set(t)
		}
		assertNotNull(e.get)

		// should work again
		e.set(null)
		try {
			s.next
			s << 0
			fail('should never reach this either')
		} catch(ArithmeticException t) {
			e.set(t)
		}
		assertNotNull(e.get)
		
		// now try to catch the error
		val e2 = new AtomicReference<Throwable>
		e.set(null)
		s.onNextError [
			e2.set(it)
		] // this prevents an error being thrown
		s.next
		s << 0
		// now e should still be null, and e2 should be set with the thrown error
		assertNull(e.get)
		assertNotNull(e2.get)
	}

	@Test
	def void testChainedBufferedSkippingStream() {
		val result = new AtomicInteger(0)
		// parent stream, has already something buffered
		val s1 = int.stream << 1 << 2 << finish << 3
		// substream, also has some stuff buffered, which needs to come out first
		val s2 = s1.map[it] << 4 << 5 << finish << 6 << 7
		s2.onNextValue [
			result.set(it)
		]
		s2.next // ask the next from the substream
		assertEquals(4, result.get) // which should be the first buffered value
		s2.skip // skip to the finish
		s2.next // results in the finish
		s2.next // process the value after the finish
		assertEquals(6, result.get) // which is 6
		s2.skip // skip, which should first discard 7, then go to the parent stream and discard 1 and 2
		s2.next // results in the finish
		s2.next // results in the 3 after the finish
		assertEquals(3, result.get)
	}


}
