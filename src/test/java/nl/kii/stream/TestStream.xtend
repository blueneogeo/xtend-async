package nl.kii.stream

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert
import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.stream.PromiseExtensions.*
import static org.junit.Assert.*

import static extension nl.kii.stream.StreamExtensions.*

class TestStream {

	val threads = newCachedThreadPool

	@Test
	def void testUnbufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer>
		s.onEach [
			counter.addAndGet(it)
		]
		s << 1 << 2 << 3
		assertEquals(6, counter.get)
	}

	@Test
	def void testBufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3
		s.onEach [ 
			counter.addAndGet(it)
		]
		assertEquals(6, counter.get)
	}
	
	@Test
	def void testControlledStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3 << finish << 4 << 5
		s.onAsync [
			each [ counter.addAndGet(it) ]
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
		s2.onAsync [
			each [ result.set(it) ]
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
		// now try to catch the error
		s.on [
			each [ println(1/it)] // handler will throw /0 exception
			error [ e.set(it) ]
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
		s2.onAsync [
			each [ result.set(it) ]
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
	
	@Test
	def void testParallelHighThroughputStreaming() {
		val s = Integer.stream
		val s2 = s.map [ it * 2 ]
		async(threads) [|
			for(i : 0..999) {
				s.apply(new Value(1))
			}
		]
		async(threads) [|
			for(i : 1000..1999) {
				s.apply(new Value(2))
			}
		]
		async(threads) [|
			for(i : 2000..2999) {
				s.apply(new Value(3))
			}
		]
		val sum = new AtomicInteger
		s2.listener = [
			switch it {
				Error<?>: println(it)
				Value<Integer>: sum.addAndGet(value) 
			}
			s2.next
		]
		s2.next
		Thread.sleep(100)
		Assert.assertEquals(12000, sum.get)
	}

}
