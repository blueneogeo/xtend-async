package nl.kii.async.test.stream

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncDefault
import nl.kii.stream.Stream
import nl.kii.stream.StreamEventHandler
import nl.kii.stream.StreamObserver
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Value
import org.junit.Ignore
import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.promise.PromiseExtensions.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.async.test.AsyncJUnitExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.util.JUnitExtensions.*

class TestStream {

	val threads = newCachedThreadPool

	@Ignore // FIX: this test should work!
	@Test
	def void testStreamIsControlled() {
		// it works while we stay within the queue size
		(1..AsyncDefault.options.maxQueueSize).stream
			.call(1) [ it | promise(it) ]
			.count
			.entries <=> #[1000.value]

		// it should also work as we go beyond the queue size!
		(1..AsyncDefault.options.maxQueueSize * 2).stream
			.count
			.entries <=> #[2000.value]

		// FIX: a call(1) breaks it!
		(1..AsyncDefault.options.maxQueueSize * 2).stream
			.call(1) [ it | promise(it) ]
			.count
			.entries <=> #[2000.value]
	}
	
	@Test
	def void testSkip() {
		val inputStream = new Stream<Integer>
		val outputStream = new Stream<Integer>
		
		inputStream << 1 << 2 << 3 << 4 << finish << 5 << 2 << 6 << finish
		
		inputStream.onChange [ msg |
			switch msg {
				Value<Integer, Integer>: {
					// skip anything after two
					if(msg.value == 2) inputStream.skip
				}
			}
			// forward the messages into the outputstream
			outputStream.act(msg) [ inputStream.next ]
		]
		inputStream.next
		
		outputStream.entries <=> #[1.value, 2.value, finish, 5.value, 2.value, finish ]
	}

	@Ignore
	@Test
	def void testObservingAStream() {
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
		counter <=> 6
		s << 1
		counter <=> 8
	}
	
	@Test
	def void testBufferedStream() {
		val counter = new AtomicInteger(0)
		val s = new Stream<Integer> << 1 << 2 << 3
		s.effect [ counter.addAndGet(it) ].start
		counter <=> 6
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
		counter <=> 1 // next pushes the first number onto the stream
		s.skip
		counter <=> 1 // skipped to the finish, nothing added
		s.next
		counter <=> 1 // got the finish, nothing added
		s.next
		counter <=> 5 // after finish is 4, added to 1
		s.next
		counter <=> 10 // 5 added
		s.next
		counter <=> 10 // we were at the end of the stream so nothing changed
		s << 1 << 4
		counter <=> 11 // we called next once before, and now that the value arrived, it's added
		s.next
		counter <=> 15 // 4 added to 11
	}
	
	@Atomic int result
	
	@Test
	def void testControlledChainedBufferedStream() {
		val s = int.stream
		val s2 = s.map[it]
		s2.on [ each [ result = $1 ] ] 
		s << 1 << 2 << 3
		s2.next
		result <=> 1
		s2.next
		result <=> 2
		s2.next
		result <=> 3
		s2.next
		result <=> 3
	}

	@Atomic Throwable error

	@Test
	def void testStreamErrors() {
		val s = new Stream<Integer>
		// now try to catch the error
		s
			.map [ 1/it ]
			.on(Throwable) [ error = it ]
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

		overflow <=> 0
		sum <=> 3000
	}
	
	@Atomic int overflowCount
	
	@Test
	def void testStreamBufferOverflow() {
		val stream = int.stream => [ options.maxQueueSize = 3 ]
		stream.when [ overflow [ incOverflowCount ] ]
		stream << 1 << 2 << 3 // so far so good
		stream << 4 // should break here
		stream << 5 // should break here too
		
		overflowCount <=> 2
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
		counter <=> 0
		overflowCount <=> 2
		pauseCount <=> 1
		
		// resume and we should get both values
		stream.resume
		stream.push(1)
		stream.next
		counter <=> 1
		overflowCount <=> 2
		pauseCount <=> 1
		
	}
	
}











