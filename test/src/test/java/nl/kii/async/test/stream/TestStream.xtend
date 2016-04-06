package nl.kii.async.test.stream

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncDefault
import nl.kii.stream.Stream
import nl.kii.stream.StreamEntryHandler
import nl.kii.stream.StreamEventHandler
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.async.test.AsyncJUnitExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.util.JUnitExtensions.*
import org.junit.Ignore

class TestStream {

	val threads = newCachedThreadPool

	// @Ignore // FIX: this test should work!
	@Test
	def void testStreamIsControlled() {
		val qsize = AsyncDefault.options.maxQueueSize;
		// it works while we stay within the queue size
		(1..qsize).stream
			.count
			<=> qsize;

		// it should also work as we go beyond the queue size!
		(1..qsize * 2).stream
			.count
			<=> qsize * 2;

		// FIX: a call(1) breaks it!
//		(1..qsize * 2).stream
//			.call(1) [ it | promise(it) ]
//			.count
//			<=> qsize * 2;
	}
	
	@Ignore
	@Test
	def void testObservingAStream() {
		// create a stream of numbers
		val s = new Stream<Integer>
		
		// listen for input
		s.entryHandler = new StreamEntryHandler<Integer, Integer> {
			override onValue(Integer from, Integer value) {
				println('value: ' + value)
				if(value == 2) throw new Exception('boo!')
				s.next
			}
			override onError(Integer from, Throwable t) {
				println('error:' + t)
				s.next
			}
			override onClosed() {
				println('closed')
			}
			
		}

		// listen for control events
		s.eventHandler = new StreamEventHandler<Integer, Integer> {
			override onNext() { println('next!') }
			override onClose() { println('close!') }
			override onPause() { }
			override onResume() { }
			override onOverflow() { println('overflow!') }
		}
		
		s << 1 << 2 << 3 << close
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
		val s = (1..2).stream
		s.on [
			each [ incCounter($1) ]
			closed [ ]
		]
		counter <=> 0 // nothing happens without asking next
		s.next
		counter <=> 1 // next pushes the first number onto the stream
		s.next
		counter <=> 3 // two is added to 1
		s.next
		counter <=> 3 // calling next will no longer have effect, the stream finished
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
	@Atomic int finishCount
	@Atomic int closedCount
	
	@Test
	def void testStreamPauseAndResume() {
		// set up a stream and an effect of a push
		val stream = int.stream
		stream.when [
			overflow [ incOverflowCount ]
			pause [ incPauseCount ]
			resume [ incResumeCount ]
			close [ incClosedCount ]
		]
		stream.on [
			each [ incCounter ]
			closed [ incFinishCount ]
		]
		
		// first push something and see that it arrives
		counter = 0
		overflowCount = 0
		stream.push(1)
		stream.next // process the next value
		stream.push(1)
		stream.next
		counter <=> 2
		overflowCount <=> 0
		
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
	
	@Test
	def void closedStreamsNoLongerAllowInput() {
		val s = int.stream
		s.push(0)
		s.effect [ incCounter ]
		s.next
		counter <=> 1
		s.close
		s.push(0)
		s.next
		counter <=> 1
	}
	
	@Test
	def void closedStreamsStillFinishTheirQueue() {
		// set up a stream that counts
		val s = int.stream
		s.effect [ incCounter ]
		
		// push a value and see that it increases the counter
		s.push(0)
		s.next
		counter <=> 1
		
		// now push a value, close the stream, and still ask for the next value from the queue
		s.push(0)
		s.close
		s.next
		counter <=> 2
	}

}










