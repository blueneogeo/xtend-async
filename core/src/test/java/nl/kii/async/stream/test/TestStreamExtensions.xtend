package nl.kii.async.stream.test

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import nl.kii.async.stream.Sink
import nl.kii.async.stream.Streams
import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.async.promise.Promises.*
import static org.junit.Assert.*

import static extension nl.kii.async.promise.BlockingExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.async.stream.Streams.*
import static extension nl.kii.util.DateExtensions.*
import static extension nl.kii.util.JUnitExtensions.*

class TestStreamExtensions {

	val threads = newCachedThreadPool
	val schedulers = newScheduledThreadPool(5)

	// CREATION ///////////////////////////////////////////////////////////////

	@Test
	def void testRangeStream() {
		(5..7).stream
			.map[it]
			.collect
			.block <=> #[5, 6, 7]
	}

	@Test
	def void testListStream() {
		#[1, 2, 3].each
			.map[it+1]
			.collect
			.block <=> #[2, 3, 4]
	}
	
	@Test
	def void testListStreamForEmptyList() {
		#[].iterator.stream.collect.block <=> #[]
	}
	
	@Test
	def void testClosureStream() {
		val range = (1..5).iterator
		newStream [ if(range.hasNext) range.next ] // null ends stream
			.collect
			.block <=> #[1, 2, 3, 4, 5]
	}

	@Test
	def void testStreamMerging() {
		val s1 = (1..10).stream
		val s2 = (11..20).stream
		val s3 = #[21, 22, 23].iterator.stream
		val merged = merge(s1, s2, s3)
		23 <=> merged.effect [ println(it) ].count.block
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		(1..3).stream.map [ it + 1 ].collect.block <=> #[2, 3, 4]
	}
	
	@Test
	def void testMapInput() {
		(1..3).stream
			.map [ it + 1 ]
			.mapInput [ 'x' + it ]
			.collectInOut
			.block <=> #{ 'x1'->2, 'x2'->3, 'x3'->4 }
	}
	
	@Test
	def void testFilter() {
		(1..5).stream.filter [it%2 == 0].collect.block <=> #[2, 4]
	}
	
	// REDUCTIONS /////////////////////////////////////////////////////////////

	@Test
	def void testCollect() {
		(1..3).stream.collect.block <=> #[1, 2, 3]
	}
	
	@Test
	def void testSum() {
		(1..3).stream.map[it*2].sum.block <=> 2+4+6 as double
	}

	@Test
	def void testAvg() {
		(0..4).stream.map[it+1].average.block <=> (1+2+3+4+5) / 5 as double
	}

	@Test
	def void testMax() {
		#[1, 6, 3, 4, 2, 5].iterator.stream.max.block <=> 6
	}
	
	@Test
	def void testMin() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.min.block <=> 1
	}
	
	@Test
	def void testAll() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.all [ it < 7 ].block <=> true
		#[6, 3, 4, 1, 2, 5].iterator.stream.all [ it >= 7 ].block <=> false
	}

	@Test
	def void testNone() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.none [ it >= 7 ].block <=> true
		#[6, 3, 4, 1, 2, 5].iterator.stream.none [ it < 7 ].block <=> false
	}

	@Atomic int counter

	@Test
	def void testFirstMatch() {
		#[1, 7, 3, 8, 1, 2, 5].iterator.stream
			.effect [ incCounter ]
			.check('stop streaming after match found') [ counter != 5 ]
			.first [ it % 2 == 0 ]
			.block <=> 8 
	}

	@Test
	def void testCount() {
		(1..3).stream.count.block <=> 3
	}
	
	@Test
	def void testReduce() {
		(1..3).stream.reduce(1) [ last, in, out | last + out ].block <=> 7
	}

	@Test
	def void testScan() {
		(1..3).stream.scan(1) [ last, in, out | last + out ].collect.block <=> #[2, 4, 7]
	}

	@Test(timeout=1000)
	def void testFlatten() {
		#[1..3, 4..6, 7..10]
			.map[println(it.start + '..' + it.end) stream(it)] // create a list of 3 streams
			.iterator.stream // create a stream of 3 streams
			.flatten // flatten into a single stream
			.effect [ println(it) ]
			.collect // collect into a single list
			.block <=> #[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
	}

	@Test
	def void testUntil() {
		(1..1_000_000_000).stream.until [ it > 2 ].collect.block <=> #[1, 2]
	}
	
	@Test
	def void testTake() {
		(1..1_000_000_000).stream.take(3).collect.block <=> #[1, 2, 3]
	}
	
	@Test
	def void testAnyMatch() {
		#[false, false, true, false].iterator.stream.any[it].block <=> true
		#[false, false, false, false].iterator.stream.any[it].block <=> false
	}

	@Test
	def void testSeparate() {
		#[ #[1, 2, 3], #[4, 5] ]
			.iterator.stream
			.separate
			.collect
			.block <=> #[1, 2, 3, 4, 5]
	}
	
	@Test
	def void testBuffer() {
		val sink = Streams.newSink
		// create a buffer on the sink
		val buffered = sink.buffer(5)
		// and already push in data before we do anything with it
		(1..7).forEach [ sink.push(it) ]
		sink.complete
		// we went over the buffer limit... the sink is paused
		sink.isOpen <=> false
		// now the data should be buffered... up to 5 messages
		val result = buffered.collect.block
		result <=> #[1, 2, 3, 4, 5]
	}
	
	@Test
	def void testBuffer2() {
		(1..3).iterator.stream
			.buffer(1)
			.effect [ println(it) ]
			.start
	}
	
	@Atomic int pausedCount
	@Atomic int resumedCount
	
	@Test
	def void testBufferAndBackpressure() {
		val result = newLinkedList
		val sink = new Sink<Integer> {

			override pause() {
				super.pause
				incPausedCount
			}

			override resume() {
				super.resume
				incResumedCount
			}
			
			override onNext() {
			}
			
			override onClose() {
			}
			
		}
		val buffered = sink
			.buffer(3)
			.effect [ result.add(it) ]

		sink << 1 << 2 // no problem, these fit the buffer
		sink.isOpen <=> true
		buffered.isOpen <=> true // sink and buffered should return the same values

		sink << 3 // this will buffer and then pause the stream
		sink.isOpen <=> false
		buffered.isOpen <=> false
		1 <=> pausedCount

		sink << 4 << 5 // problem, these overflow
		sink.isOpen <=> false
		buffered.isOpen <=> false
		3 <=> pausedCount		
		
		buffered.next // output 1, the first next will allow buffering again, and resume the stream
		1 <=> resumedCount
		buffered.next // output 2
		buffered.next // output 3
		result <=> #[1, 2, 3]

		buffered.next // no effect, nothing in the buffer!

		sink << 6 // since we already called next, this should go out immediately
		result <=> #[1, 2, 3, 6]
		sink << 7 << 8 << 9 // no problem
		sink << 10 << 11 // overflow again, should pause the stream for each
		6 <=> pausedCount
		buffered.next // should resume the stream, push out the value
		2 <=> resumedCount
		result <=> #[1, 2, 3, 6, 7]
		buffered.next // 7
		buffered.next // 8
		buffered.next // 9
		buffered.next // no effect again
		result <=> #[1, 2, 3, 6, 7, 8, 9]
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	@Test
	def void testFirst() {
		val s = (1..3).iterator.stream
		s.first.block <=> 1
	}

	@Test
	def void testLast() {
		val s = (1..1_000_000).stream // will loop a million times upto the last...
		s.last.block <=> 1_000_000
	}

	@Test
	def void testSkipAndTake() {
		(1..20).stream
			.skip(3)
			.take(5)
			.collect
			.block <=> #[4, 5, 6, 7, 8]
	}
	
//	@Test
//	def void testListPromiseToStream() {
//		(1..2).stream.promise.toStream <=> #[1, 2]
//	}

	@Test
	def void testDelay() {
		val start = now
		val times = 100
		val period = 10.ms;
		val count = (1..times).stream
			.delay(period, newTimer(schedulers))
			.count.block(times * (period + 10.ms)) // 10 msec for overhead 
		assertEquals(times, count)
		val waited = now - start
		assertTrue(waited > times * period)
	}

	@Test
	def void testPeriodicExecutor() {
		val results = newPeriodicStream(schedulers, 1.sec / 100, 100)
			.count
			.block(2.secs)
		assertEquals(100, results)
	}

	@Test
	def void testPeriodicUsingTimerFn() {
		val results = newTimer(schedulers)
			.newPeriodicStream(1.sec / 100, 100)
			.count
			.block(2.secs)
		assertEquals(100, results)
	}

	@Test
	def void testThrottle() {
		val fireAmount = 1000
		val firePerSec = 500
		val allowPerSec = 3
		val count = newPeriodicStream(schedulers, 1.sec / firePerSec, fireAmount)
			.throttle(1.sec / allowPerSec)
			.effect [ println(it) ]
			.count.block(fireAmount / firePerSec * 1.sec)
		assertEquals(fireAmount / firePerSec * allowPerSec, count) // 100 / 50
	}

	@Test
	def void testThrottleWorksWithBuffer() {
		val fireAmount = 100
		val firePerSec = 50
		val allowPerSec = 5
		val count = newPeriodicStream(schedulers, 1.sec / firePerSec, fireAmount)
			.buffer(10)
			.throttle(1.sec / allowPerSec)
			.count.block(fireAmount / firePerSec * 1.sec)
		assertEquals(fireAmount / firePerSec * allowPerSec, count) // 100 / 50
	}
	
	@Test
	def void testRateLimitUnderControlledStream() {
		val count = (1..10).stream
			// .buffer(100) // buffering is unnecessary with a controlled stream
			.ratelimit(100.ms, newTimer(schedulers))
			.effect [ println(it) ]
			.count.block(1.min)
		assertEquals(10, count)
	}

	@Test
	def void testRateLimitUnderPeriodicStream() {
		val count = newPeriodicStream(schedulers, 10.ms, 10)
			.buffer(100) // must buffer when the stream is uncontrolled
			.ratelimit(100.ms, newTimer(schedulers))
			.effect [ println(it) ]
			.count.block(1.min)
		assertEquals(10, count)
	}

	@Test(timeout=5000)
	def void testWindow() {
		val windowCount = new AtomicInteger
		val count = newPeriodicStream(schedulers, 60.ms / 5, 50)
			.window(50.ms)
			.effect [ windowCount.incrementAndGet ]
			.flatten
			.count.block(5.min)
		assertEquals(50, count)
		// timing depends on the runner
		// assertEquals(10, windowCount.get)
	}

	@Test(timeout=5000)
	def void testWindowWorksWithBuffer() {
		val windowCount = new AtomicInteger
		val count = newPeriodicStream(schedulers, 60.ms / 5, 50)
			.buffer(10)
			.window(50.ms)
			.effect [ windowCount.incrementAndGet ]
			.flatten
			.count.block(30.secs)
		assertEquals(50, count)
		// timing depends on the runner
		// assertEquals(10, windowCount.get)
	}

	@Test(timeout=5000)
	def void testSample() {
		val samples = newPeriodicStream(schedulers, 60.ms / 5, 50)
			.sample(50.ms)
			.effect [ println(it) ]
			.collect.block(1.min)
		assertFalse(samples.isEmpty)
		// timing depends on the runner
		// samples <=> #[5L, 10L, 15L, 20L, 25L, 30L, 35L, 40L, 45L, 50L]
	}

	@Test
	def void testCountingStream() {
		val list = Streams.newCountingStream
			.take(5)
			.collect
			.block(1.sec)
		assertArrayEquals(#[1, 2, 3, 4, 5], list)
	}

	// PARALLEL PROCESSING ////////////////////////////////////////////////////
	
	

}
