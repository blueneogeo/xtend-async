package nl.kii.async.stream.test

import nl.kii.async.annotation.Atomic
import nl.kii.async.stream.Sink
import org.junit.Test

import static java.util.concurrent.Executors.*

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*
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
			.await <=> #[5, 6, 7]
	}

	@Test
	def void testListStream() {
		#[1, 2, 3].iterator.stream
			.map[it+1]
			.collect
			.await <=> #[2, 3, 4]
	}
	
	@Test
	def void testListStreamForEmptyList() {
		#[].iterator.stream.collect.await <=> #[]
	}
	
	@Test
	def void testClosureStream() {
		val range = (1..5).iterator
		stream [ if(range.hasNext) range.next ] // null ends stream
			.collect
			.await <=> #[1, 2, 3, 4, 5]
	}

	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		(1..3).stream.map [ it + 1 ].collect.await <=> #[2, 3, 4]
	}
	
	@Test
	def void testMapInput() {
		(1..3).stream
			.map [ it + 1 ]
			.mapInput [ 'x' + it ]
			.collectInOut
			.await <=> #{ 'x1'->2, 'x2'->3, 'x3'->4 }
	}
	
	@Test
	def void testFilter() {
		(1..5).stream.filter [it%2 == 0].collect.await <=> #[2, 4]
	}
	
	// REDUCTIONS /////////////////////////////////////////////////////////////

	@Test
	def void testCollect() {
		(1..3).stream.collect.await <=> #[1, 2, 3]
	}
	
	@Test
	def void testSum() {
		(1..3).stream.map[it*2].sum.await <=> 2+4+6 as double
	}

	@Test
	def void testAvg() {
		(0..4).stream.map[it+1].average.await <=> (1+2+3+4+5) / 5 as double
	}

	@Test
	def void testMax() {
		#[1, 6, 3, 4, 2, 5].iterator.stream.max.await <=> 6
	}
	
	@Test
	def void testMin() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.min.await <=> 1
	}
	
	@Test
	def void testAll() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.all [ it < 7 ].await <=> true
		#[6, 3, 4, 1, 2, 5].iterator.stream.all [ it >= 7 ].await <=> false
	}

	@Test
	def void testNone() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.none [ it >= 7 ].await <=> true
		#[6, 3, 4, 1, 2, 5].iterator.stream.none [ it < 7 ].await <=> false
	}

	@Atomic int counter

	@Test
	def void testFirstMatch() {
		#[1, 7, 3, 8, 1, 2, 5].iterator.stream
			.effect [ incCounter ]
			.check('stop streaming after match found') [ counter != 5 ]
			.first [ it % 2 == 0 ]
			.await <=> 8 
	}

	@Test
	def void testCount() {
		(1..3).stream.count.await <=> 3
	}
	
	@Test
	def void testReduce() {
		(1..3).stream.reduce(1) [ last, in, out | last + out ].await <=> 7
	}

	@Test
	def void testScan() {
		(1..3).stream.scan(1) [ last, in, out | last + out ].collect.await <=> #[2, 4, 7]
	}

	@Test
	def void testFlatten() {
		#[1..3, 4..6, 7..10]
			.map[stream(it)] // create a list of 3 streams
			.iterator.stream // create a stream of 3 streams
			.flatten // flatten into a single stream
			.collect // collect into a single list
			.await <=> #[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
	}

	@Test
	def void testUntil() {
		(1..1_000_000_000).stream.until [ it > 2 ].collect.await <=> #[1, 2]
	}
	
	@Test
	def void testTake() {
		(1..1_000_000_000).stream.take(3).collect.await <=> #[1, 2, 3]
	}
	
	@Test
	def void testAnyMatch() {
		#[false, false, true, false].iterator.stream.any[it].await <=> true
		#[false, false, false, false].iterator.stream.any[it].await <=> false
	}

	@Test
	def void testSeparate() {
		#[ #[1, 2, 3], #[4, 5] ]
			.iterator.stream
			.separate
			.collect
			.await <=> #[1, 2, 3, 4, 5]
	}
	
	@Test
	def void testBuffer() {
		val sink = new Sink<Integer> {
			override onNext() { }
			override onClose() { }
		}
		// create a buffer on the sink
		val buffered = sink.buffer(5)
		// and already push in data before we do anything with it
		(1..7).forEach [ sink.push(it) ]
		sink.complete
		// we went over the buffer limit... the sink is paused
		sink.isOpen <=> false
		// now the data should be buffered... up to 5 messages
		val result = buffered.collect.await
		result <=> #[1, 2, 3, 4, 5]
		// the buffer was cleared, the sink is open for values again
		sink.isOpen <=> true
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
		s.first.await <=> 1
	}

	@Test
	def void testLast() {
		val s = (1..1_000_000).stream // will loop a million times upto the last...
		s.last.await <=> 1_000_000
	}

	@Test
	def void testSkipAndTake() {
		(1..20).stream
			.skip(3)
			.take(5)
			.collect
			.await <=> #[4, 5, 6, 7, 8]
	}
	
//	@Test
//	def void testListPromiseToStream() {
//		(1..2).stream.promise.toStream <=> #[1, 2]
//	}

//	@Test
//	def void testWait() {
//		val start = now
//		val times = 100
//		val period = 5.ms;
//		(1..times).stream
//			.wait(period, schedulers.timer)
//			.count <=> times
//		val waited = now - start
//		assertTrue(waited > times * period)
//	}

//	@Test
//	def void testThrottle() {
//		(1..100).stream
//			.wait(5.ms, schedulers.timer)
//			.throttle(50.ms)
//			.count <=> 12 // 6 * 100 items / 50 ms = 12 items. the first item is always delayed, giving one extra item, so from 5 to 6
//	}
//	
//	@Test
//	def void testRateLimit() {
//		(1..10).stream
//			.ratelimit(100.ms, schedulers.timer)
//			.ratelimit(200.ms, schedulers.timer)
//			.count <=> 10
//	}
//
//	@Test
//	def void testRateLimitWithErrors() {
//		(1..4).stream
//			.map [ 1000 / (2-it) * 1000 ]
//			.ratelimit(200.ms, schedulers.timer)
//			.map [ 1000 / (2-it) * 1000 ]
//			.on(Exception, true) [ incErrorCount ]
//			<=> #[0, 0, 0]
//		errorCount <=> 1
//	}
//	
//	@Test
//	def void testRateLimitAsyncProcessing() {
//		val list = (1..8).toList
//		val start = now
//		list.iterator.stream
//			.ratelimit(150.ms, schedulers.timer)
//			.effect [ println(it) ]
//			<=> list;
//		val timeTaken = now - start
//		val minTimeTaken = 8 * 100.ms;
//		assertTrue(timeTaken > minTimeTaken)
//	}
//	
//	// FIX: needs better test, and is currently broken!
//	@Test
//	def void testWindow() {
//		val newStream = sink [ ]		
//		newStream
//			.window(50.ms, schedulers.timer)
//			.effect [ println(it) ]
//			.start;
//		(1..1000).stream
//			.ratelimit(100.ms, schedulers.timer)
//			.pipe(newStream)
//		Thread.sleep(5000)
//	}
	
}
