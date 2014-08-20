package nl.kii.stream.test

import org.junit.Test

import static java.util.concurrent.Executors.*
import static extension nl.kii.promise.PromiseExtensions.*

import static extension nl.kii.stream.StreamAssert.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension org.junit.Assert.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.io.File

class TestStreamExtensions {

	val threads = newCachedThreadPool

	@Test
	def void testRangeStream() {
		val s = (5..7).stream
		val s2 = s.map[it]
		s2.assertStreamEquals(#[5.value, 6.value, 7.value, finish])
	}

	@Test
	def void testListStream() {
		val s = #[1, 2, 3].stream
		println(s.queue)
		val s2 = s.map[it+1]
		s2.assertStreamEquals(#[2.value, 3.value, 4.value, finish])
	}
	
	@Test
	def void testMapStream() {
		val map = #{1->'a', 2->'b'}
		val s = map.stream
		val s2 = s.map[key+1->value]
		s2.assertStreamEquals(#[value(2->'a'), value(3->'b'), finish])
	}
	
	// SUBSCRIPTION BUILDING //////////////////////////////////////////////////
	
	@Test
	def void testSubscriptionBuilding() {
		val finished = new AtomicBoolean
		val errored = new AtomicBoolean
		val count = new AtomicInteger
		
		val s = (1..10).stream
		s
			.map [ it - 10 ]
			.map [ 100 / it ] // division by 10 for s = 10
			.onError [ errored.set(true) ]
			.onFinish [ finished.set(true) ]
			.onEach [ count.incrementAndGet ]
			
		assertEquals(true, finished.get) // end of iteration
		assertEquals(true, errored.get) // because of the 10
		assertEquals(9, count.get) // 10 gave the error
	}
	
	// OBSERVABLE /////////////////////////////////////////////////////////////
	
	@Test
	def void testObservable() {
		val count1 = new AtomicInteger(0)
		val count2 = new AtomicInteger(0)
		
		val s = int.stream
		val publisher = s.publish
		
		val s1 = publisher.stream
		val s2 = publisher.stream
		
		s1.onEach [ count1.addAndGet(it) ]
		s2.onEach [ count2.addAndGet(it) ]
		
		s << 1 << 2 << 3
		// both counts are listening, both should increase
		assertEquals(6, count1.get)
		assertEquals(6, count2.get)
		
		// we cancel the first listener by closing the stream, now the first count should no longer change
		s1.close
		
		s << 4 << 5
		assertEquals(6, count1.get)
		assertEquals(15, count2.get)
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		val mapped = s.map [ it + 1 ]
		mapped.assertStreamEquals(#[2.value, 3.value, 4.value, finish, 5.value, 6.value])
	}
	
	@Test
	def void testFilter() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		val filtered = s.filter [ it % 2 == 0]
		filtered.assertStreamEquals(#[2.value, finish, 4.value])
	}
	
	@Test
	def void testSplit() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val split = s.split [ it % 2 == 0]
		split.assertStreamEquals(#[1.value, 2.value, finish(0), 3.value, finish(0), finish(1), 4.value, finish(0), 5.value, finish(0), finish(1)])
	}
	
	@Test
	def void testMerge() {
		val s = Integer.stream << 1 << 2 << finish(0) << 3 << finish(1) << 4 << finish(0) << 5 
		val merged = s.merge
		merged.assertStreamEquals(#[1.value, 2.value, 3.value, finish, 4.value, 5.value])
	}
	
	// REDUCTIONS /////////////////////////////////////////////////////////////

	@Test
	def void testCollect() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish << 6
		val collected = s.collect
		// 5 is missing below because there is no finish to collect 5
		collected.assertStreamEquals(#[#[1, 2, 3].value, #[4, 5].value])
	}
	
	@Test
	def void testDoubleCollect() {
		val s = (1..11).stream
		val split = s.split [ it % 4 == 0 ] // finish after 4, 8
		val split2 = split.split [ it % 2 == 0 ] // finish after 2, 4, 6, 8, 10
		val collect = split2.collect // [1, 2], [3, 4], f0, [5, 6], [7, 8], f0, [9, 10], f1
		val collect2 = collect.collect // [[1, 2], [3, 4]], [[5, 6], [7, 8]
		val collect3 = collect2.collect
		collect3.first.then [
			assertEquals(
				#[#[#[1, 2], #[3, 4]], #[#[5, 6], #[7, 8]], #[#[9, 10], #[11]]]
			)
		]
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
		collect4.first.then [
			assertEquals(#[
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
			])
		]
	}
	
	
	@Test
	def void testSum() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val summed = s.sum
		summed.assertStreamEquals(#[6D.value, 9D.value])
	}

	@Test
	def void testAvg() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val avg = s.average
		avg.assertStreamEquals(#[2D.value, 4.5D.value])
	}
	
	@Test
	def void testCount() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val counted = s.count
		counted.assertStreamEquals(#[3L.value, 2L.value])
	}
	
	@Test
	def void testReduce() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val summed = s.reduce(1) [ a, b | a + b ] // starting at 1!
		summed.assertStreamEquals(#[7.value, 10.value])
	}

	@Test
	def void testReduceWithCounter() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val summed = s.reduce(0L) [ a, b, c | a + c ]
		// #[0 + 1 + 2 , 0 + 1]
		summed.assertStreamEquals(#[3L.value, 1L.value])
	}
	
	@Test
	def void testLimit() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val limited = s.limit(1)
		limited.assertStreamEquals(#[1L.value, finish, 4L.value, finish])		
	}
	
	@Test
	def void testLimitBeforeCollect() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val limited = s.limit(1).collect
		limited.assertStreamEquals(#[#[1L].value, #[4L].value])		
	}
	
	@Test
	def void testUntil() {
		val s = Long.stream << 1L << 2L << 3L << 4L << finish << 4L << 2L << 5L << 6L << finish
		val untilled = s.until [ it == 2L ]
		untilled.assertStreamEquals(#[1L.value, finish, 4L.value, finish])
	}

	@Test
	def void testUntil2() {
		val s = Long.stream << 1L << 2L << 3L << 4L << finish << 4L << 2L << 5L << 6L << finish
		val untilled = s.until [ it == 2L ].collect
		untilled.assertStreamEquals(#[#[1L].value, #[4L].value])
	}
	
	@Test
	def void testAnyMatchNoFinish() {
		val s = Boolean.stream << false << false << true << false
		val matches = s.anyMatch[it].first
		matches.assertPromiseEquals(true)
	}

	@Test
	def void testAnyMatchWithFinish() {
		val s = Boolean.stream << false << false << false << finish
		val matches = s.anyMatch[it].first
		matches.assertPromiseEquals(false)
	}
	
	@Test
	def void testFragment() {
		(1..10).stream
			.split [ it % 3 == 0 ]
			.collect
			.separate
			.collect
			.first
			.assertPromiseEquals((1..10).toList)
	}

	// PARALLEL ///////////////////////////////////////////////////////////////
	
	// TODO: use assertions here instead of printing
	@Test
	def void testResolving() {
		val doSomethingAsync = [ String x |
			async(threads) [|
				for(i : 1..5) {
					Thread.sleep(10)
					println(x + i)
				}
				x
			]
		]
		val s = String.stream
		s << 'a' << 'b' << 'c' << finish << 'd' << 'e' << finish << 'f' << finish
		println(s.queue)
		s
			.map [
				// println('pushing ' + it)
				it
			]
			.map(doSomethingAsync)
			.resolve(3)
			.collect
			.onEach [
				println('got: ' + it)
			]
			//.onEach [ println('got: ' + it)	]
		s << 'f' << 'g' << finish << 'h' << finish
		s << 'd' << 'e' << finish
		s << 'a' << 'b' << 'c' << finish
		Thread.sleep(1000)
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	@Test
	def void testFirst() {
		val s = Integer.stream << 2 << 3 << 4
		s.first.assertPromiseEquals(2)
	}

	@Test
	def void testLast() {
		val s = (1..1_000_000).stream // will loop a million times upto the last...
		s.last.assertPromiseEquals(1_000_000)
	}

	@Test
	def void testSkipAndTake() {
		val s = (1..1_000_000_000).stream // will loop only 3 times!
		s.skip(3).take(3).collect.first.assertPromiseEquals(#[4, 5, 6])
	}
	
	@Test
	def void testFirstAfterCollect() {
		val s = Integer.stream << 1 << 2 << finish << 3 << 4 << finish
		s.collect.first.assertPromiseEquals(#[1, 2])
	}
	
	@Test
	def void testStreamForwardTo() {
		// since we use flow control, we can stream forward a lot without using much memory
		val s1 = (1..1000000).stream
		val s2 = int.stream
		s1.forwardTo(s2)
		s2.count.then [ assertEquals(1000000, it, 0) ]
	}
	
	// TEST FILE STREAMING ////////////////////////////////////////////////////
	
	@Test
	def void testFileStreaming() {
		val file = new File('gradle.properties')
		file.stream
			.toText
			.map [ '- ' + it ]
			.onFinish [ println('finish') ]
			.onEach [ println(it) ]
	}
	
	@Test
	def void testWriteStreamToFile() {
		val data = #[
			'Hello,',
			'This is some text',
			'Please make this into a nice file!'
		]
		data.stream.toBytes.writeTo(new File('test.txt'))
	}
	
	@Test
	def void testFileCopy() {
		val source = new File('test.txt')
		val destination = new File('text2.txt')
		source.stream.writeTo(destination).then [
			source.delete
			destination.delete
		]
	}
	
}
