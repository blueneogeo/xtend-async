package nl.kii.stream.test

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.StreamAssert.*
import static extension nl.kii.stream.StreamExt.*

class TestStreamExt {

	@Test
	def void testRangeStream() {
		val s = (5..7).stream
		val s2 = s.map[it]
		#[5.value, 6.value, 7.value, finish].assertStreamEquals(s2)
	}

	@Test
	def void testListStream() {
		val s = #[1, 2, 3].stream
		val s2 = s.map[it+1]
		#[2.value, 3.value, 4.value, finish].assertStreamEquals(s2)		
	}
	
	@Test
	def void testMapStream() {
		val map = #{1->'a', 2->'b'}
		val s = map.stream
		val s2 = s.map[key+1->value]
		#[value(2->'a'), value(3->'b'), finish].assertStreamEquals(s2)
	}
	
	// TRANSFORMATIONS ////////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		val mapped = s.map [ it + 1 ]
		#[2.value, 3.value, 4.value, finish, 5.value, 6.value].assertStreamEquals(mapped)
	}
	
	@Test
	def void testFilter() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		val filtered = s.filter [ it % 2 == 0]
		#[2.value, finish, 4.value].assertStreamEquals(filtered)
	}
	
	@Test
	def void testSplit() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5
		// #[1.value, 2.value, 3.value, finish, 4.value, 5.value].assertStreamEquals(s)
		val split = s.split [ it % 2 == 0]
		#[1.value, 2.value, finish, 3.value, finish, 4.value, finish, 5.value].assertStreamEquals(split)
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
		#[#[1, 2, 3].value, #[4, 5].value].assertStreamEquals(collected)
	}
	
	@Test
	def void testSum() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val summed = s.sum
		#[6D.value, 9D.value].assertStreamEquals(summed)
	}

	@Test
	def void testAvg() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val avg = s.avg
		#[2D.value, 4.5D.value].assertStreamEquals(avg)
	}
	
	@Test
	def void testCount() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val counted = s.count
		#[3L.value, 2L.value].assertStreamEquals(counted)
	}
	
	@Test
	def void testReduce() {
		val s = Integer.stream << 1 << 2 << 3 << finish << 4 << 5 << finish
		val summed = s.reduce(1) [ a, b | a + b ] // starting at 1!
		#[7.value, 10.value].assertStreamEquals(summed)
	}

	@Test
	def void testReduceWithCounter() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val summed = s.reduce(0L) [ a, b, c | a + c ]
		// #[0 + 1 + 2 , 0 + 1]
		#[3L.value, 1L.value].assertStreamEquals(summed)
	}
	
	@Test
	def void testLimit() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val limited = s.limit(1)
		#[1L.value, finish, 4L.value, finish].assertStreamEquals(limited)		
	}
	
	@Test
	def void testLimitBeforeCollect() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val limited = s.limit(1).collect
		#[#[1L].value, #[4L].value].assertStreamEquals(limited)		
	}
	
	@Test
	def void testUntil() {
		val s = Long.stream << 1L << 2L << 3L << finish << 4L << 5L << finish
		val untilled = s.until [ it == 2L ]
		#[1L.value, finish, 4L.value, 5L.value, finish].assertStreamEquals(untilled)
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

	// ASYNC //////////////////////////////////////////////////////////////////

	// TODO: needs better test that uses multithreading
	@Test
	def void testAsync() {
		val s = Integer.stream << 2 << finish << 3 << 4 << finish
//		val asynced = 
		s.async [ power2(it) ].collect.then [ println(it) ]
		// #[4.value, 9.value].assertStreamEquals(asynced)
	}

	// TODO: needs better test that uses multithreading
	@Test
	def void testAsync3() {
		val s = Integer.stream << 2 << 3 << 4 << 5 << 6 << 7
		val asynced = s.async(3) [ power2(it) ]
		#[4.value, 9.value, 16.value, 25.value, 36.value, 49.value].assertStreamEquals(asynced)
	}
	
	private def power2(int i) { (i*i).promise }
	
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
