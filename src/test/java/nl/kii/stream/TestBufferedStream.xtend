package nl.kii.stream

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.PromisePairExt.*
import static extension nl.kii.stream.StreamExt.*
import static extension org.junit.Assert.*
import java.util.List
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger

class TestBufferedStream {
	
	// TESTING BASIC STREAM CLASS /////////////////////////////////////////////
	
	@Test
	def void testBufferedStreaming() {
		val s = String.stream << 'a' << 'b' << 'c' << finish // entries are put in before reading
		#['a'.value, 'b'.value, 'c'.value, finish].assertStream(s) // they should be buffered and still readable
	}

	@Test
	def void testDeferredStreaming() {
		val s2 = String.stream
		val s = String.stream
		s.each [ it >> s2 ] // anything that enters s from now on will be pushed to s2
		s << 'a'.value << 'b'.value // only start pushing once all is set up
		#['a'.value, 'b'.value].assertStream(s2) // s2 should now have the items from s1
	}
	
	@Test
	def void testAutomaticEach() {
		val s = Integer.stream << 1 << 2 << 3
		val s2 = Integer.stream // receiving stream
		s.each [ it >> s2 ] // automatically starts streaming each value into to s2 
		#[1.value, 2.value, 3.value].assertStream(s2)
	}

	@Test
	def void testCatchErrorsOff() {
		try {
			val s = Integer.stream << 3 << 2 << 1 << 0
			s.map [ 10 / it ].start // will throw division by 0 at last entry
			throw new Exception('error should have been thrown')
		} catch(StreamException t) {
			// success
		}
	}

	@Test
	def void testCatchErrorsOn() {
		val success = Boolean.promise
		val s = Integer.stream => [ catchErrors = true ] 
		s << 3 << 2 << 1 << 0
		s
			.onError [ true >> success ] // success
			.map [ 10 / it ] // will throw division by 0 at last entry
			.start 
		success.assertPromiseFinished
	}

	@Test
	def void testCatchErrorsOnWithChaining() {
		val success = Boolean.promise
		val s = Integer.stream => [ catchErrors = true ] 
		s << 3 << 2 << 1 << 0
		s
			.map [ 10 / it ] // will throw division by 0 at last entry
			.onError [ true >> success ] // error is caught on the new stream
			.start
		success.assertPromiseFinished
	}
	
	// TESTING STREAMEXT //////////////////////////////////////////////////////

	@Test
	def void testMap() {
		val s = Integer.stream << 1 << 2 << 3
		val mapped = s.map [ it + 3 ]
		#[4.value, 5.value, 6.value].assertStream(mapped)
	}
	
	@Test
	def void testFilter() {
		val s = #[1, 2, 3, 4, 5].stream
		val filtered = s.filter[it % 2 == 0]
		#[2.value, 4.value, finish].assertStream(filtered)
	}
	
	@Test
	def void testFlatten() {
		val s = #[ #[1, 2], #[3, 4] ].stream
		val flattened = s.flatten
		#[1.value, 2.value, 3.value, 4.value, finish].assertStream(flattened)
	}
	
	@Test
	def void testSplit() {
		val s = #[1, 2, 3, 4, 5].stream
		val split = s.split[it % 2 == 0]
		#[1.value, 2.value, finish, 3.value, 4.value, finish, 5.value, finish].assertStream(split)
	}
	
	@Test
	def void testCollectAndFirst() {
		// note that a collect always requires a finish!
		// the finish is the signal to collect. 
		val s = #[1, 2, 3, 4, 5].stream // generates a finish at the end
		val collect = s.collect.first
		collect.assertPromiseEquals(#[1, 2, 3, 4, 5])
	}
	
	@Test
	def void testRepeatedCollect() {
		val s = Integer.stream << 1 << 2 << finish << 3 << 4 << finish << finish 
		// note the double finish, should give empty list --------/\--------/\
		val collect = s.collect
		#[
			#[1, 2].value,
			#[3, 4].value,
			#[].value
		].assertStream(collect)
	}
	
	private def Promise<Integer> mockAddAsync(int value) {
		Integer.promise => [ apply(value + 1) ]
	}	

	@Test
	def void testAsync() {
		val s = #[1, 2, 3].stream
		val result = s.async [ mockAddAsync ].collect.first
		result.assertPromiseEquals(#[2, 3, 4])
	}

	@Test
	def void testReduce() {
		val s = #[1, 2, 3].stream
		val reduced = s.reduce(1) [ a, b, c | a + b ].first
		reduced.assertPromiseEquals(7) // started at 1, added 1+2+3
	}
	
	@Test
	def void testLimitAndShortcutting() {
		val counter = new AtomicInteger
		val s = #[1, 2, 3, 4, 5, 6, 7, 8].stream
		// count the mappings made, then limit. shortcutting should mean only 3 mappings are done
//		val limited = s.map [ counter.incrementAndGet; it ].limit(3)
		s.limit(3).each[println(it)]
//		#[1.value, 2.value, 3.value].assertStream(limited)
		counter.get.assertEquals(3)
	}

	@Test
	def void testShortcuttingMultipleListeners() {
		val counter = new AtomicInteger
		val s = #[1, 2, 3, 4, 5, 6, 7, 8].stream
		// count the mappings made, then limit. shortcutting should mean only 3 mappings are done
		val limited = s.map [ counter.incrementAndGet; it ].limit(3)
		#[1.value, 2.value, 3.value, finish].assertStream(limited)
	}
	
	// QUICK TESTS BELOW //////////////////////////////////////////////////////
	
	@Test
	def void testRepeatedStreaming() {
		val stream = Integer.stream 
		stream << 5 << 8 << 3 << 1 << 9 << 23 << finish
		stream.filter[it % 3 == 0].map[it * 2].each [ print(it) ]
		stream << 3 << 4 << finish
	}

	@Test
	def void testCollectOLD() {
		val stream = Integer.stream 
		stream << 5 << 8 << 3 << finish << 1 << 9 << 23 << finish
		stream.filter[it % 3 == 0].map[it * 2].collect.each [ println(it) ]
	}
	
	@Test
	def void testReduceOLD() {
		val stream = Integer.stream << 1 << 2 << 3 << finish
		stream.reduce(0) [ a, b | a + b ].then [ println(it) ]
	}
	
	@Test
	def void testErrorCatching() {
		val stream = Integer.stream => [ catchErrors = true ]
		stream << 4 << 6 << new Exception('throw this')
		stream.onError[println('error:' + it)].each[println(it)]
	}
	
	@Test
	def void testManyListeners() {
		val stream = #[1,2,3,4,5,6,7].stream
		stream
			.limit(2)
			.collect
			.then [ println(join(','))]
	}
	
	@Test
	def void testSplitFn() {
		val stream = #[1, 2, 3, 4, 5, 6, 7, 8].stream
		stream
			.split[it % 3 == 0]
			.collect
			.each [ println(it) ]
	}
	
	@Test
	def void testSimpleStreaming() {
		val stream = Integer.stream << 4 << 9 << 10 << finish
		stream
			.map [ it + 4 ]
			.filter [ it % 2 == 0 ]
			.onFinish [ println('done!') ]
			.each [ println('got: ' + it)]
	}
	
	@Test
	def void testBigStream() {
		val stream = Integer.stream << 3 << 5 << 22 << finish
		stream
			.map [ it + 4 ]
			.map [ toString ]
			.async [ messageAsync ]
			.map [ length ]
			.sum
			.first
			.then [ println('sum: ' + intValue) ]
	}
	
	@Test
	def void testError() {
		val stream = Long.stream << 2L << 0L << finish
		stream.catchErrors = true
		stream
			.map [ 10 / it ] // throws nullpointer
			.sum
			.onError [ println('error: ' + it)] // FIX: why not work after and only before?
			.then [ println('sum: ' + it) ]
	}
	
	@Test
	def void testPromise() {
		val promise = Integer.promise
		promise.then [ println('got' + it) ]
		10 >> promise
	}
	
	@Test
	def void testBufferedPromise() {
		val promise = 10.promise
		promise.then [ println('got ' + it)]
	}
	
	@Test
	def void testPromiseChain() {
		val promise = String.promise
		'hello' >> promise
		promise
			.async [ messageAsync ]
			.async [ messageAsync ]
			.then [ println(it) ]
	}

	@Test
	def void testPromiseChainPair() {
		val promise = String.promise
		'hello' >> promise
		promise
			.async2 [ 2 -> 3 -> messageAsync ] // seen as (2->3) -> Promise<String>
			.then [ p, v | println(p + v) ]
	}
	
	def Promise<String> getMessageAsync(String value) {
		String.promise => [ apply('got value ' + value) ]
	}
	
	// STREAM ASSERT TOOLS ////////////////////////////////////////////////////
	
	def private <T> assertStream(List<? extends Entry<T>> entries, Stream<T> stream) {
		val List<Entry<T>> found = newLinkedList
		stream.onChange [ found.add(it)	]
		stream.start
		println(found)
		assertArrayEquals(found, entries)
	}
	
	def private assertPromiseFinished(Promise<Boolean> promise) {
		promise.then[] // force start
		promise.finished.assertTrue
	}

	def private <T> assertPromiseEquals(Promise<T> promise, T value) {
		val ref = new AtomicReference<T>
		promise.then[ ref.set(it) ]
		promise.finished.assertTrue
		ref.get.assertEquals(value)
	}

	def private <T> void assertPromiseEquals(Promise<List<T>> promise, List<T> value) {
		val ref = new AtomicReference<List<T>>
		promise.then[ ref.set(it) ]
		promise.finished.assertTrue
		ref.get.assertArrayEquals(value)
	}
	
	def private <T> value(T value) {
		new Value<T>(value)
	}

	
}
