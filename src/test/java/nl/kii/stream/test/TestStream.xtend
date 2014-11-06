package nl.kii.stream.test

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import nl.kii.stream.Entry
import nl.kii.stream.Stream
import nl.kii.stream.StreamEventResponder
import nl.kii.stream.StreamObserver
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import nl.kii.stream.StreamEventHandler

class TestStream {

	val threads = newCachedThreadPool

	@Test
	def void testObservingAStream() {
		//val Stream<Integer> s = (1..3).stream
		val s = new Stream<Integer>
		s.handle(new StreamEventHandler {
			override onNext() { println('next!') }
			override onSkip() { println('skip!') }
			override onClose() { println('close!') }
			override onOverflow(Entry<?, ?> entry) { println('overflow! of ' + entry) }
		})
		s.observe(new StreamObserver<Integer, Integer> {
			override onValue(Integer from, Integer value) {
				println('value: ' + value)
				if(value == 2) throw new Exception('boo!')
				s.next
			}
			override onError(Integer from, Throwable t) {
				println('error:' + t)
				s.next
			}
			override onFinish(Integer from, int level) {
				println('finished')
				s.next
			}
			override onClosed() {
				println('closed')
			}
			
		})
			// .then [ println('done!') ]
			// .onError [ println('caught: ' + it)]
		 s << 1 << 2 << 3 << finish

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
			each [ incCounter($1) ]
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
		val s = int.stream.map[it]
		s.on [ each [ result = $1 ] ] 
		s << 1 << 2 << 3
		
		s.next
		assertEquals(1, result)
		s.next
		assertEquals(2, result)
		s.next
		assertEquals(3, result)
		s.next
		assertEquals(3, result)
	}

	@Atomic Throwable error

	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		// now try to catch the error
		s
			.onEach [ println(1/it) ]
			.onError [ println('!!') error = it ]
		s << 1 << 0
		assertNotNull(error)
	}

	@Atomic int sum
	@Atomic int overflow
	
	@Test
	def void testParallelHighThroughputStreaming() {
		val s = int.stream
		// The threads push in the values so quickly into the queues,
		// that the buffer needs to be increased to deal with the overflow
		// It is unlikely that this is necessary in normal use cases, since
		// you are usually not pushing data as fast as you can without any
		// other preparation of data.
		val s2 = s.buffer(3000) [ incOverflow ]
		// PS: I tested that if a Thread.sleep(1) is put in each pushing task,
		// that negates the need for a big buffer. 
		threads.task [ for(i : 0..999) s << 1 ]
		threads.task [ for(i : 1000..1999) s << 2 ]
		threads.task [ for(i : 2000..2999)	s << 3 ]
		s2.onEach [ incSum ]
		Thread.sleep(1000)
		assertEquals(0, overflow)
		assertEquals(3000, sum)
	}
	
	@Atomic int overflowCount
	
	@Test
	def void testStreamBufferOverflow() {
		val stream = int.stream => [ maxBufferSize = 3 ]
		stream.when [ overflow [ incOverflowCount ] ]
		stream << 1 << 2 << 3 // so far so good
		stream << 4 // should break here
		stream << 5 // should break here too
		assertEquals(2, overflowCount)
	}
	
}
