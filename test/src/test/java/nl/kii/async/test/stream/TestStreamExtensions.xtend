package nl.kii.async.test.stream

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Atomic
import nl.kii.promise.Promise
import nl.kii.stream.Stream
import nl.kii.stream.message.Value
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
	val schedulers = newScheduledThreadPool(1)

	// CREATION ///////////////////////////////////////////////////////////////

	@Test
	def void testRangeStream() {
		(5..7).stream
			.map[it]
			.entries <=> #[5.value, 6.value, 7.value, finish]
	}

	@Test
	def void testListStream() {
		#[1, 2, 3].streamList
			.map[it+1]
			.entries <=> #[2.value, 3.value, 4.value, finish]
	}
	
	@Test
	def void testListStreamForEmptyList() {
		#[].streamList <=> #[]
	}
	
	@Test
	def void testMapStream() {
		#{1->'a', 2->'b'}.stream
			.map[key+1->value]
			.entries <=> #[value(2->'a'), value(3->'b'), finish]
	}
	
	@Test
	def void testRandomStream() {
		(1..3).streamRandom
			.takeAndFinish(1000)
		 	.effect [ assertTrue(it >= 1 && it <= 3) ]
			.count.first <=> 1000
	}
	
	// OBSERVABLE /////////////////////////////////////////////////////////////
	
	@Test
	def void testObservable() {
		val count1 = new AtomicInteger(0)
		val count2 = new AtomicInteger(0)
		
		val s = int.stream
		val publisher = s.publisher
		
		val s1 = publisher.observe
		val s2 = publisher.observe
		
		s1.effect [ count1.addAndGet(it) ].start
		s2.effect [ count2.addAndGet(it) ].start
		
		s << 1 << 2 << 3
		// both counts are listening, both should increase
		count1 <=> 6
		count2 <=> 6
		
		// we cancel the first listener by closing the stream, now the first count should no longer change
		s1.close
		
		s << 4 << 5
		count1 <=> 6
		count2 <=> 15
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		s
			.map [ it + 1 ]
			.entries <=> #[2.value, 3.value, 4.value, finish, 5.value, 6.value]
	}
	
	@Test
	def void testMapInput() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		s
			.map [ it + 1 ]
			.mapInput [ in, it | 'x' + in ]
			.entries <=> #[value('x1', 2), value('x2', 3), value('x3', 4), finish('xnull', 0), value('x4', 5), value('x5', 6)]
	}
	
	@Test
	def void testFilter() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		s
			.filter [ it % 2 == 0]
			.entries <=> #[2.value, finish, 4.value]
	}
	
	@Test
	def void testSplit() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		s
			.split [ it % 2 == 0]
			.entries <=> #[1.value, 2.value, finish(0), 3.value, finish(0), finish(1), 4.value, finish(0), 5.value, finish(0), finish(1)]
	}

	@Test
	def void testSplitWithSkip() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val split = s.split [ it % 4 == 0]
		val collect = int.stream
		split.on [
			each [ 
				if($1 == 1) stream.skip
				collect << $1
			]
			finish [ 
				collect << finish($1)
			]
		]
		split.next
		split.next
		split.next
		split.next
		split.next
		split.next
		split.next
		split.next
		collect.entries <=> #[1.value, finish(0), finish(1), 4.value, finish(0), 5.value, finish(0), finish(1)]
	}

	
	@Test
	def void testMerge() {
		val s = Integer.stream << 1 << 2 << finish(0) << 3 << finish(1) << 4 << finish(0) << 5 
		s
			.merge
			.entries <=> #[1.value, 2.value, 3.value, finish, 4.value, 5.value]
	}
	
	// REDUCTIONS /////////////////////////////////////////////////////////////

	@Test
	def void testCollect() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish << 6
		// 6 is missing below because there is no finish to collect 6
		s
			.collect
			.entries <=> #[#[1, 2, 3].value, #[4, 5].value]
	}
	
	@Test
	def void testDoubleCollect() {
		val s = (1..11).stream
		val split = s.split [ it % 4 == 0 ] // finish after 4, 8
		val split2 = split.split [ it % 2 == 0 ] // finish after 2, 4, 6, 8, 10
		val collect = split2.collect // [1, 2], [3, 4], f0, [5, 6], [7, 8], f0, [9, 10], f1
		val collect2 = collect.collect // [[1, 2], [3, 4]], [[5, 6], [7, 8]
		val collect3 = collect2.collect
		collect3.first <=> #[#[#[1, 2], #[3, 4]], #[#[5, 6], #[7, 8]], #[#[9, 10], #[11]]]
	}
	
	@Test
	def void testGuardDoubleSplits() {
		val s = (1..11).stream
		val split = s.split [ it % 4 == 0 ]
		val split2 = split.split [ it % 3 == 0 ] // should also split at %4
		val split3 = split2.split [ it % 2 == 0 ] // should also split at %4 and %3
		val collect = split3.collect
		val collect2 = collect.collect
		val collect3 = collect2.collect
		val collect4 = collect3.collect 
		collect4.first <=> #[
			#[
				#[
					#[1, 2], // %2 
					#[3] // %2
				], // %3
				#[
					#[4] // %2
				] // %3
			], // %4 
			#[
				#[
					#[5, 6] // %2
				], // %3
				#[
					#[7, 8] // %2
				] // %3
			], // %4
			#[
				#[
					#[9] // %2
				], // %3 
				#[
					#[10],  // %2
					#[11] // %2
				] // %3
			] // end of stream finish
		]
	}
	
	
	@Test
	def void testSum() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		s
			.sum
			.entries <=> #[6D.value, 9D.value]
	}

	@Test
	def void testAvg() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		s
			.average
			.entries <=> #[2D.value, 4.5D.value]
	}
	
	@Test
	def void testMax() {
		val s = Integer.stream << 1 << 8 << 3 << 2 << 3 << finish << 7 << 4 << 5 << finish
		s
			.max
			.entries <=> #[8.value, 7.value]
	}
	
	@Test
	def void testMin() {
		val s = Integer.stream << 1 << 8 << 3 << 2 << 3 << finish << 7 << 4 << 5 << finish
		s
			.min
			.entries <=> #[1.value, 4.value]
	}
	
	@Test
	def void testAll() {
		val s = Integer.stream << 1 << 8 << 3 << 2 << 3 << finish << 7 << 4 << 5 << finish
		s
			.all [ it > 3 ]
			.entries <=> #[false.value, true.value]
	}

	@Test
	def void testNone() {
		val s = Integer.stream << 1 << 8 << 3 << 2 << 3 << finish << 7 << 4 << 5 << finish
		s
			.none [ it < 3 ]
			.entries <=> #[false.value, true.value]
	}

	@Test
	def void testFirstMatch() {
		val s = Integer.stream << 1 << 8 << 3 << 2 << 3 << finish << 7 << 4 << 5 << finish << 1 << 10 // note no finish here
		s
			.first [ it % 2 == 0 ]
			.entries <=> #[8.value, 4.value, 10.value] // 10 is still streamed
	}

	@Test
	def void testCount() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		s
			.count
			.entries <=> #[3.value, 2.value]
	}
	
	@Test
	def void testReduce() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		s
			.reduce(1) [ a, b | a + b ]
			.entries <=> #[7.value, 10.value]
	}

	@Test
	def void testScan() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		s
			.scan(1) [ a, b | a + b ]
			.entries <=> #[2.value, 4.value, 7.value, finish, 5.value, 10.value, finish]
	}

	@Test
	def void testFlatten() {
		#[1..3, 4..6, 7..10]
			.map[stream(it)] // create a list of 3 streams
			.datastream // create a stream of 3 streams
			.flatten // flatten into a single stream
			.collect
			<=> #[#[1, 2, 3], #[4, 5, 6], #[7, 8, 9, 10]]
	}

	@Test
	def void testFlattenWithMerge() {
		#[1..3, 4..6, 7..10]
			.map[stream(it)] // create a list of 3 streams
			.datastream // create a stream of 3 streams
			.flatten // flatten into a single stream
			.merge // remove the finishes from the stream
			<=> #[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
	}

	@Test
	def void testTake() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		s
			.take(1)
			.entries 
			 <=> #[1L.value, finish, 4L.value, finish]
	}
	
	@Test
	def void testTakeBeforeCollect() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		s
			.take(1)
			.collect
			.entries <=> #[#[1L].value, #[4L].value]
	}

	@Test
	def void testUntil() {
		val s = Long.stream << 1L << 2L << 3L << finish
		println(s
			.until [ it > 2 ]
			.entries) <=> #[1L.value, 2L.value, finish]
	}
	
	@Test
	def void testUntilWithMultipleParts() {
		val s = Long.stream << 1L << 2L << 3L << 4L << finish << 1L << 2L << 5L << 6L << finish
		s
			.until [ it > 3 ]
			.collect
			.entries <=> #[ #[1L, 2L, 3L].value, #[1L, 2L].value ]
	}

	@Test
	def void testAnyMatchNoFinish() {
		val s = Boolean.stream << false << false << true << false
		s
			.any[it]
			.first <=> true
	}

	@Test
	def void testAnyMatchWithFinish() {
		val s = Boolean.stream << false << false << false << finish
		val matches = s.any[it].first
		matches <=> false
	}
	
	@Test
	def void testSeparate() {
		(1..10).stream
			.split [ it % 3 == 0 ]
			.collect
			.separate
			.collect
			.first <=> (1..10).toList
	}
	
	@Test
	def void testSeparate2() {
		#[ #[1, 2, 3], #[4, 5] ]
			.streamList
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
			.first <=> null
	}

	@Test
	def void testErrorsDontStopStream() {
		val errors = String.stream;
		(1..10).stream
			.map [ 1/(it-5)*0 + it ] // 5 gives a /0 exception
			.map [ 1/(it-7)*0 + it ] // 7 also gives the exception
			.effect(Exception) [ message >> errors ] // filter errors here
			.collect // so this collect succeeds
			.first <=> #[1, 2, 3, 4, 6, 8, 9, 10] // 5 and 7 are missing
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

	// PARALLEL ///////////////////////////////////////////////////////////////
	
	@Test
	def void testResolve() {
		val t1 = int.promise
		val t2 = int.promise
		val s = #[t1, t2].streamList.resolve
		s.onChange [
			switch it {
				Value<?, Integer>: {
					println(value)
					s.next
				}
			}
		]
		println('start') 
		s.next
		println('A')
		t1.set(1)
		println('B') 
		// s.next
		println('C')
		t2.set(2)
		println('D')
		println('E')
	}
	
	
	
	// TODO: use assertions here instead of printing
	// FIX: still gives undetermined and changing results when using resolve(30! 
	// seems a problem with capturing the finishes.
	// using resolve(1) will capture all finishes, however
	// using resolve() will eliminate all finishes and return a single list..
	@Test
	def void testResolving() {
		val doSomethingAsync = [ String x |
			threads.promise [
				Thread.sleep(50)
				x
			]
		]
		val s = String.stream
		s << 'a' << 'b' << 'c' << finish << 'd' << 'e' << finish << 'f' << finish
		s
			.map(doSomethingAsync)
			.resolve(1)
			.collect
			.effect [ println('got: ' + it) ]
			.start
		s << '1' << '2' << finish 
		s << 'x' << 'y' << finish
		s << 'A' << 'B' << 'C' << finish
		Thread.sleep(1000)
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
			.takeAndFinish(5)
			<=> #[4, 5, 6, 7, 8]
	}
	
	@Test
	def void testFirstAfterCollect() {
		val s = Integer.stream << 1 << 2 << finish << 3 << 4 << finish
		s.collect.first <=> #[1, 2]
	}
	
	@Test
	def void testCollectCollect() {
		val s = Integer.stream << 1 << 2 << finish << 3 << 4 << finish << finish(1)
		s.collect.collect.first <=> #[#[1, 2], #[3, 4]]
	}
	
	@Test
	def void testPipe() {
		val s = (1..3).stream
//		s.split.stream.on [
//			each [ println('x' + $1) ]
//			finish [ println('done') ]
//		]
		val s2 = s.split.stream
		s2.on [
			each [ println('x' + $1) ]
			//error [ s2.next true ]
			//finish [ s2.next ]
			//closed [ s2.close ]
		]
		s2.next
	}
	
	@Test
	def void testStreamForwardTo() {
		// since we use flow control, we can stream forward a lot without using much memory
		val s1 = int.stream << 1 << 2 << 3
		// val s2 = int.stream
		// s1.pipe(s2)
		val s2 = s1.split.stream
		s2.effect [ println(it) ].then [ println('done') ]
		//s2.count.then [ assertEquals(1_000, it, 0) ]
	}
	
	@Test
	def void testStreamPromise() {
		val s = int.stream
		val p = s.promise
		s << 1 << 2 << finish
		val s2 = p.toStream
		s2
			.effect [ println(it) ]
			.start
			<=> true
	}

	// @Test FIX!
	def void testStreamPromiseLater() {
		val p = new Promise<Stream<Integer>>
		val s = int.stream
		p.set(s)
		s << 1 << 2 << finish
		val s2 = p.toStream
		s2
			.effect [ println(it) ]
			.start
			<=> true
	}
	
	@Test
	def void testThrottle() {
		(1..1000).stream.throttle(10.ms).effect [ println(it) ].start
	}
	
	@Test
	def void testRateLimit() {
		val stream = (1..10).stream
		val limited = stream
			.ratelimit(100.ms, schedulers.timer)
			.ratelimit(200.ms, schedulers.timer)
		limited
			.effect [ println(it) ]
			.collect.first.asFuture.get
	}

	@Test
	def void testRateLimitWithErrors() {
		val stream = (1..4).stream
		val limited = stream
			.map [ 1000 / (2-it) * 1000 ]
			.ratelimit(200.ms, schedulers.timer)
			.map [ 1000 / (2-it) * 1000 ]
		limited
			.on(Exception, true) [ println(it) ]
			.effect [ println(it) ]
			.collect.first.asFuture.get
	}
	
	@Test
	def void testRateLimitAsyncProcessing() {
		val list = (1..10).toList
		list.streamList
			.ratelimit(1.secs, schedulers.timer)
			.wait(200.ms, schedulers.timer)
			.effect [ println(it) ]
			<=> list
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
	
	// @Test FIX: latest does not work yet
	def void testLatest() {
		// val scheduler = newSingleThreadScheduledExecutor;
		// (5..10).streamRandom.every(1000, scheduler).latest.onEach [ println(it) ]
		// Thread.sleep(5000)
	}
	
}
