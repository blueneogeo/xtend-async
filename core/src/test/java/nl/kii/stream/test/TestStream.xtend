package nl.kii.stream.test

import java.util.List
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Async
import nl.kii.async.annotation.Atomic
import nl.kii.promise.Promise
import nl.kii.stream.Stream
import nl.kii.stream.StreamEventHandler
import nl.kii.stream.StreamObserver
import nl.kii.stream.message.Entry
import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.stream.test.StreamAssert.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class TestStream {

	val threads = newCachedThreadPool

	@Test
	def void test() {
		(1..100_000).stream
			.map [ it + 10 ]
			.effect [ println(it) ]
			.call(3) [ doSomethingHeavy ]
			.on(Throwable) [ println(it) ]
			.start
	}
	
	@Async def doSomethingHeavy(int i, Promise<List<Integer>> promise) {
		promise << #[i, i+1, i+2]
	}

	@Test
	def void testObservingAStream() {
		//val Stream<Integer> s = (1..3).stream
		val s = new Stream<Integer>
		s.handle(new StreamEventHandler<Integer, Integer> {
			override onNext() { println('next!') }
			override onSkip() { println('skip!') }
			override onClose() { println('close!') }
			override onPause() { }
			override onResume() { }
			override onOverflow(Entry<Integer, Integer> entry) { println('overflow! of ' + entry) }
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
			.effect [ counter.addAndGet(it) ]
			.start
		s << 1 << 2 << 3
		assertEquals(6, counter.get)
		s << 1
		assertEquals(8, counter.get)
	}
	
	@Test
	def void testBufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3
		s.effect [ counter.addAndGet(it) ].start
		assertEquals(6, counter.get)
	}
	
	@Atomic int counter
	
	@Test
	def void testControlledStream() {
		val s = new Stream<Integer> << 1 << 2 << 3 << finish(0) << 4 << 5
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
		val s = int.stream
		val s2 = s.map[it]
		s2.on [ each [ result = $1 ] ] 
		s << 1 << 2 << 3
		s2.next
		assertEquals(1, result)
		s2.next
		assertEquals(2, result)
		s2.next
		assertEquals(3, result)
		s2.next
		assertEquals(3, result)
	}

	@Atomic Throwable error

	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		// now try to catch the error
		s
			.effect [ println(1/it) ]
			.on(Throwable) [ println('!!') error = it ]
			.start
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
		s2.effect[ incSum ].start
		Thread.sleep(1000)
		assertEquals(0, overflow)
		assertEquals(3000, sum)
	}
	
	@Atomic int overflowCount
	
	@Test
	def void testStreamBufferOverflow() {
		val stream = int.stream => [ options.maxQueueSize = 3 ]
		stream.when [ overflow [ incOverflowCount ] ]
		stream << 1 << 2 << 3 // so far so good
		stream << 4 // should break here
		stream << 5 // should break here too
		assertEquals(2, overflowCount)
	}

	@Atomic int pauseCount
	@Atomic int resumeCount
	
	@Test
	def void testStreamPauseAndResume() {
		// set up a stream and an effect of a push
		val stream = int.stream
		stream.when [ 
			overflow [ incOverflowCount ]
			pause [ incPauseCount ]
			resume [ incResumeCount ]
		]
		stream.effect [ incCounter ]
		
		// first push something and see that it arrives
		counter = 0
		overflowCount = 0
		stream.push(1)
		stream.next // process the next value
		stream.push(1)
		stream.next
		assertEquals(2, counter)
		assertEquals(0, overflowCount)
		
		// now pause the stream and see that now it does not work, and the value overflowed
		counter = 0
		overflowCount = 0
		stream.pause
		stream.push(1)
		stream.next
		stream.push(1)
		stream.next
		assertEquals(0, counter)
		assertEquals(2, overflowCount)
		assertEquals(1, pauseCount)
		
		// resume and we should get both values
		stream.resume
		stream.push(1)
		stream.next
		assertEquals(1, counter)
		assertEquals(2, overflowCount)
		assertEquals(1, resumeCount)
		
	}
	
}











