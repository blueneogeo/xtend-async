package nl.kii.stream.test

import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.stream.PromiseExtensions.*

import static extension nl.kii.stream.StreamAssert.*
import static extension nl.kii.stream.StreamExtensions.*

class TestStreamExtensions {

	val threads = newCachedThreadPool

	@Test
	def void testPrint() {
		val s = int.stream
		s << 1 << 2 << finish
		s.on [
			each [ println(it) ]
			finish [ println('finished!') ]
		]
	}

	@Test
	def void testRangeStream() {
		val s = (5..7).stream
		val s2 = s.map[it]
		s2.assertStreamEquals(#[5.value, 6.value, 7.value, finish])
	}

	@Test
	def void testListStream() {
		val s = #[1, 2, 3].stream
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
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		// #[1.value, 2.value, 3.value, finish, 4.value, 5.value].assertStreamEquals(s)
		val split = s.split [ it % 2 == 0]
		split.assertStreamEquals(#[1.value, 2.value, finish, 3.value, finish, 4.value, finish, 5.value])
	}
	
//	@Test
//	def void testSubstream() {
//		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish << 6
//		val subbed = s.substream
//		subbed.queue.length.assertEquals(2) // no finish at end means 6 is not converted
//		val s1 = subbed.queue.get(0) as Value<Stream<Integer>>
//		val s2 = subbed.queue.get(1) as Value<Stream<Integer>>
//		#[1.value, 2.value, 3.value].assertStreamEquals(s1.value)
//		#[4.value, 5.value].assertStreamEquals(s2.value)
//	}

	// REDUCTIONS /////////////////////////////////////////////////////////////

	@Test
	def void testCollect() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish << 6
		val collected = s.collect
		// 5 is missing below because there is no finish to collect 5
		collected.assertStreamEquals(#[#[1, 2, 3].value, #[4, 5].value])
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
		val avg = s.avg
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
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val untilled = s.until [ it == 2L ]
		untilled.assertStreamEquals(#[1L.value, finish, 4L.value, 5L.value, finish])
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

	// PARALLEL ///////////////////////////////////////////////////////////////
	
	// TODO: fix this one!
	@Test
	def void testResolving() {
		val doSomethingAsync = [ String x |
			asyncFn(threads) [|
				for(i : 1..5) {
					Thread.sleep(10)
					// println(x + i)
				}
				x
			]
		]
		val s = #['a', 'b'].stream
		println(s.queue)
		s << 'c' << finish << 'd' << 'e' << finish << 'f'
//		s
//			.map [
//				// println('pushing ' + it)
//				it
//			]
//			.map(doSomethingAsync)
//			.resolve(1)
//			.collect
//			.onEach [
//				println('got: ' + it)
//			]
			//.onEach [ println('got: ' + it)	]
		 // s << 'f' << 'g' << finish << 'h' << finish
		//s << 'd' << 'e'
//		s << 'a' << 'b' << 'c'
		Thread.sleep(1000)
	}
	
	// ENDPOINTS //////////////////////////////////////////////////////////////
	
	@Test
	def void testFirst() {
		val s = Integer.stream << 2 << 3 << 4
		s.first.assertPromiseEquals(2)
	}
	
	@Test
	def void testFirstAfterCollect() {
		val s = Integer.stream << 1 << 2 << finish << 3 << 4 << finish
		s.collect.first.assertPromiseEquals(#[1, 2])
	}
	
}
