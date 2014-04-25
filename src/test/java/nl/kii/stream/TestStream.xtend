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
		s.onValue [ 
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
		s.onValue [ 
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
		s.onValue [
			counter.addAndGet(it)
		]
		s.next
		assertEquals(1, counter.get) // next pushes the first number onto the stream
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
		s.onValue [ 
			println(1 / it)
			s.next
		] // handler will throw /0 exception
		
		// no onerror set, should throw it
		e.set(null)
		try {
			s << 0
			fail('should never reach this')
		} catch(Throwable t) {
			e.set(t)
		}
		assertNotNull(e.get)

		// should work again
		e.set(null)
		try {
			s << 0
			fail('should never reach this either')
		} catch(Throwable t) {
			e.set(t)
		}
		assertNotNull(e.get)
		
		// now try to catch the error
		val e2 = new AtomicReference<Throwable>
		e.set(null)
		s.onError [ e2.set(it) ] // this prevents an error being thrown
		s << 0
		// now e should still be null, and e2 should be set with the thrown error
		assertNull(e.get)
		assertNotNull(e2.get)
	}





}



