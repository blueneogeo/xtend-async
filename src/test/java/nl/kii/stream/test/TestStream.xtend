package nl.kii.stream.test

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import nl.kii.stream.Error
import nl.kii.stream.Stream
import nl.kii.stream.StreamObserver
import nl.kii.stream.Value
import org.junit.Assert
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import nl.kii.stream.StreamMonitor

class TestStream {

	val threads = newCachedThreadPool

	@Test
	def void testObservingAStream() {
		val s = (1..3).stream
		s.monitor(new StreamMonitor {
			override onNext() { println('next!') }
			override onSkip() { println('skip!') }
			override onClose() { println('close!') }
		})
		s.observe(new StreamObserver<Integer> {
			override onValue(Integer value) {
				println('value: ' + value)
				if(value == 2) throw new Exception('boo!')
				s.next
			}
			override onError(Throwable t) {
				println('error:' + t)
				s.next
				true
			}
			override onFinish(int level) {
				println('finished')
				s.next
			}
			override onClosed() {
				println('closed')
			}
		})
			// .then [ println('done!') ]
			// .onError [ println('caught: ' + it)]
		// s << 1 << 2 << 3

		s.next
	}


	@Test
	def void testUnbufferedStream() {
		val counter = new AtomicInteger(0)
		val s = int.stream
		s
			.filter [ it != 2 ]
			.map [ it + 1 ]
			.onEach [ counter.addAndGet(it) ]
		s << 1 << 2 << 3
		assertEquals(6, counter.get)
		s << 1
		assertEquals(8, counter.get)
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
	
	@Atomic int counter
	
	@Test
	def void testControlledStream() {
		val s = new Stream<Integer> << 1 << 2 << 3 << finish << 4 << 5
		s.on [
			each [ incCounter(it) ]
			finish [ ]
		]
		s.next
		assertEquals(1, counter) // next pushes the first number onto the stream
		s.skip
		assertEquals(1, counter) // skipped to the finish, nothing added
		s.next
		assertEquals(1, counter) // got the finish, nothing added
		s.next
		assertEquals(5, counter) // after finish is 4, added to 1
		s.next
		assertEquals(10, counter) // 5 added
		s.next
		assertEquals(10, counter) // we were at the end of the stream so nothing changed
		s << 1 << 4
		assertEquals(11, counter) // we called next once before, and now that the value arrived, it's added
		s.next
		assertEquals(15, counter) // 4 added to 11
	}
	
	@Atomic int result
	
	@Test
	def void testControlledChainedBufferedStream() {
		val s1 = int.stream << 1 << 2 << 3
		val s2 = s1.map[it] << 4 << 5 << 6
		s2.on [
			each [ result = it ]
		]
		s2.next
		assertEquals(4, result)
		s2.next
		assertEquals(5, result)
		s2.next
		assertEquals(6, result)
		s2.next
		assertEquals(1, result)
		s2.next
		assertEquals(2, result)
		s2.next
		assertEquals(3, result)
	}

	@Atomic Throwable error

	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		// now try to catch the error
		s
			.onEach [ println(1/it) ]
			.onError [ error = it ]
		s << 0
		assertNotNull(error)
	}

	@Test
	def void testChainedBufferedSkippingStream() {
		val result = new AtomicInteger(0)
		// parent stream, has already something buffered
		val s1 = int.stream << 1 << 2 << finish << 3
		// substream, also has some stuff buffered, which needs to come out first
		val s2 = s1.map[it] << 4 << 5 << finish << 6 << 7
		s2.on [
			each [ result.set(it) ]
			finish [ ]
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
		threads.task [|
			for(i : 0..999) {
				s.apply(new Value(1))
			}
		]
		threads.task [|
			for(i : 1000..1999) {
				s.apply(new Value(2))
			}
		]
		threads.task [|
			for(i : 2000..2999) {
				s.apply(new Value(3))
			}
		]
		val sum = new AtomicInteger
		s2.onChange [
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
