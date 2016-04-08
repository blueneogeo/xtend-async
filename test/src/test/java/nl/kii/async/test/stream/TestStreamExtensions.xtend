package nl.kii.async.test.stream

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.async.test.AsyncJUnitExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
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
			.entries <=> #[5.value, 6.value, 7.value, close]
	}

	@Test
	def void testListStream() {
		#[1, 2, 3].iterator.stream
			.map[it+1]
			.entries <=> #[2.value, 3.value, 4.value, close]
	}
	
	@Test
	def void testListStreamForEmptyList() {
		#[].iterator.stream <=> #[]
	}
	
	@Test
	def void testMapStream() {
		#{1->'a', 2->'b'}.stream
			.map[key+1->value]
			.entries <=> #[value(2->'a'), value(3->'b'), close]
	}
	
	@Test
	def void testRandomStream() {
		(1..3).streamRandom
			.take(1000)
		 	.effect [ assertTrue(it >= 1 && it <= 3) ]
			.count <=> 1000
	}
	
	// OBSERVABLE /////////////////////////////////////////////////////////////
	
	@Test
	def void testObservable() {
		val count1 = new AtomicInteger(0)
		val count2 = new AtomicInteger(0)
		
		val source = int.stream
		source
			.observe [ observer | observer.effect [ count2.addAndGet(it) ].start ]
			.effect [ count1.addAndGet(it) ]
			.start
		
		source << 1 << 2 << 3
		// both counts are listening, both should increase
		count1 <=> 6
		count2 <=> 6
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		(1..3).stream.map [ it + 1 ] <=> #[2, 3, 4]
	}
	
	@Test
	def void testMapInput() {
		(1..3).stream
			.map [ it + 1 ]
			.mapInput [ in, it | 'x' + in ]
			.entries <=> #[value('x1', 2), value('x2', 3), value('x3', 4), close]
	}
	
	@Test
	def void testFilter() {
		(1..5).stream.filter [it%2 == 0] <=> #[2, 4]
	}
	
	// REDUCTIONS /////////////////////////////////////////////////////////////

	@Test
	def void testCollect() {
		(1..3).stream.collect <=> #[1, 2, 3]
	}
	
	@Test
	def void testSum() {
		(1..3).stream.map[it*2].sum <=> 2+4+6 as double
	}

	@Test
	def void testAvg() {
		(0..4).stream.map[it+1].average <=> (1+2+3+4+5) / 5 as double
	}

	@Test
	def void testMax() {
		#[1, 6, 3, 4, 2, 5].iterator.stream.max <=> 6
	}
	
	@Test
	def void testMin() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.min <=> 1
	}
	
	@Test
	def void testAll() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.all [ it < 7 ] <=> true
		#[6, 3, 4, 1, 2, 5].iterator.stream.all [ it >= 7 ] <=> false
	}

	@Test
	def void testNone() {
		#[6, 3, 4, 1, 2, 5].iterator.stream.none [ it >= 7 ] <=> true
		#[6, 3, 4, 1, 2, 5].iterator.stream.none [ it < 7 ] <=> false
	}

	@Atomic int counter

	@Test
	def void testFirstMatch() {
		#[1, 7, 3, 8, 1, 2, 5].iterator.stream
			.effect [ incCounter ]
			.check('stop streaming after match found') [ println(counter) counter != 5 ]
			.first [ it % 2 == 0 ]
			<=> 8 
	}

	@Test
	def void testCount() {
		(1..3).stream.count <=> 3
	}
	
	@Test
	def void testReduce() {
		(1..3).stream.reduce(1) [ last, in, out | last + out ] <=> 7
	}

	@Test
	def void testScan() {
		(1..3).stream.scan(1) [ last, in, out | last + out ] <=> #[2, 4, 7]
	}

	@Test
	def void testFlatten() {
		#[1..3, 4..6, 7..10]
			.map[stream(it)] // create a list of 3 streams
			.iterator.stream // create a stream of 3 streams
			.flatten // flatten into a single stream
			<=> #[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
	}

	@Test
	def void testUntil() {
		(1..1_000_000_000).stream.until [ it > 2 ] <=> #[1, 2]
	}
	
	@Test
	def void testTake() {
		(1..1_000_000_000).stream.take(3) <=> #[1, 2, 3]
	}
	
	@Test
	def void testAnyMatch() {
		#[false, false, true, false].iterator.stream.any[it] <=> true
		#[false, false, false, false].iterator.stream.any[it] <=> false
	}

	@Test
	def void testSeparate2() {
		#[ #[1, 2, 3], #[4, 5] ]
			.iterator.stream
			.separate <=> #[1, 2, 3, 4, 5]
	}
	
	// ERRORS /////////////////////////////////////////////////////////////////
	
	@Test(expected=ArithmeticException)
	def void testErrorsCanStopStream() {
		val errors = String.stream;
		(1..10).stream
			.map [ 1/(it-5)*0 + it ] // 5 gives a /0 exception
			.map [ 1/(it-7)*0 + it ] // 7 also gives the exception
			.on(Exception) [ message >> errors ] // not filtering errors here
			.collect // so this collect fails
			<=> null
	}

	@Test
	def void testErrorsDontStopStream() {
		val errors = String.stream;
		(1..10).stream
			.map [ 1/(it-5)*0 + it ] // 5 gives a /0 exception
			.map [ 1/(it-7)*0 + it ] // 7 also gives the exception
			.effect(Exception) [ message >> errors ] // filter errors here
			.collect // so this collect succeeds
			<=> #[1, 2, 3, 4, 6, 8, 9, 10] // 5 and 7 are missing
		errors.queue.size <=> 2
	}
	
	@Atomic int valueCount
	@Atomic int errorCount
	
	@Test
	def void testErrorsDontStopStream2() {
		val s = int.stream
			s.effect [
				if(it == 3 || it == 5) throw new Exception('should not break the stream') 
				else incValueCount
			]
			.on(Throwable) [ incErrorCount ]
			.start
		// onError can catch the errors
		for(i : 1..10) s << i
		valueCount <=> 10 - 2
		errorCount <=> 2
	}
	
	@Test
	def void testErrorsDontStopStream3() {
		(1..10).stream
			.map [ 
				if(it == 3 || it == 5) throw new Exception('should not break the stream')
				it
			]
			.effect [ incValueCount ]
			.on(Exception) [ incErrorCount ]
			.start
		valueCount <=> 10 - 2
		errorCount <=> 2
	}
	
	@Atomic int overflowCount
	
	@Test
	def void testBufferOverflow() {
		val stream = int.stream
		stream.buffer(3) [ incOverflowCount ]
		stream << 1 << 2 << 3 // no problem
		stream << 4 << 5 // problems!
		overflowCount <=> 2
	}

	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	@Test
	def void testFirst() {
		val s = Integer.stream << 2 << 3 << 4
		s.first <=> 2
	}

	@Test
	def void testLast() {
		val s = (1..1_000_000).stream // will loop a million times upto the last...
		s.last <=> 1_000_000
	}

	@Test
	def void testSkipAndTake() {
		(1..20).stream
			.skip(3)
			.take(5)
			<=> #[4, 5, 6, 7, 8]
	}
	
	@Test
	def void testListPromiseToStream() {
		(1..2).stream.promise.toStream <=> #[1, 2]
	}

	@Test
	def void testWait() {
		val start = now
		val times = 100
		val period = 5.ms;
		(1..times).stream
			.wait(period, schedulers.timer)
			.count <=> times
		val waited = now - start
		assertTrue(waited > times * period)
	}

	@Test
	def void testThrottle() {
		(1..100).stream
			.wait(5.ms, schedulers.timer)
			.throttle(50.ms)
			.count <=> 12 // 6 * 100 items / 50 ms = 12 items. the first item is always delayed, giving one extra item, so from 5 to 6
	}
	
	@Test
	def void testRateLimit() {
		(1..10).stream
			.ratelimit(100.ms, schedulers.timer)
			.ratelimit(200.ms, schedulers.timer)
			.count <=> 10
	}

	@Test
	def void testRateLimitWithErrors() {
		(1..4).stream
			.map [ 1000 / (2-it) * 1000 ]
			.ratelimit(200.ms, schedulers.timer)
			.map [ 1000 / (2-it) * 1000 ]
			.on(Exception, true) [ incErrorCount ]
			<=> #[0, 0, 0]
		errorCount <=> 1
	}
	
	@Test
	def void testRateLimitAsyncProcessing() {
		val list = (1..8).toList
		val start = now
		list.iterator.stream
			.ratelimit(150.ms, schedulers.timer)
			.effect [ println(it) ]
			<=> list;
		val timeTaken = now - start
		val minTimeTaken = 8 * 100.ms;
		assertTrue(timeTaken > minTimeTaken)
	}
	
	// FIX: needs better test, and is currently broken!
	@Test
	def void testWindow() {
		val newStream = int.stream			
		newStream
			.window(50.ms, schedulers.timer)
			.effect [ println(it) ]
			.start;
		(1..1000).stream
			.ratelimit(100.ms, schedulers.timer)
			.pipe(newStream)
		Thread.sleep(5000)
	}
	
}
