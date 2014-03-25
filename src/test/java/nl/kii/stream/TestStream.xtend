package nl.kii.stream

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.PromisePairExt.*
import static extension nl.kii.stream.StreamExt.*
import static extension org.junit.Assert.*

class TestStream {
	
	@Test
	def void testStreamCreation() {
		val s1 = String.stream << 'a' << 'b' << 'c'
		val ts = String.stream
		s1.each [
			println(it) 
			it >> ts
		]
		ts.finish.collect.then [
			println('done') 
			length.assertEquals(3)
		]
	}
	
	// QUICK TESTS BELOW
	
	@Test
	def void testRepeatedStreaming() {
		val stream = Integer.stream 
		stream << 5 << 8 << 3 << 1 << 9 << 23 << finish
		stream.filter[it % 3 == 0].map[it * 2].each [ println(it) ]
		stream << 3 << 4 << finish
	}

	@Test
	def void testCollect() {
		val stream = Integer.stream 
		stream << 5 << 8 << 3 << 1 << 9 << 23 << finish
		stream.filter[it % 3 == 0].map[it * 2].collect.then [ println(it) ]
		stream << 3 << 4 << finish
	}
	
	@Test
	def void testReduce() {
		val stream = Integer.stream << 1 << 2 << 3 << 4 << 5 << 6 << 7 << finish
		stream.reduce(0) [ a, b | a + b ].then [ println(it) ]
	}
	
	@Test
	def void testErrorCatching() {
		val stream = Integer.stream => [ catchErrors = true; autostart = false ]
		stream << 4 << 6 << new Exception('throw this')
		stream.each[println(it)].onError[println('error:' + it)].start
	}
	
	@Test
	def void testManyListeners() {
		val stream = #[1,2,3,4,5,6,7].stream
		stream.autostart(false)
			.limit(2)
			.each[print(it)]
			.each[print(it)]
			.each[print(it)]
			.each[print(it)]
			.start
	}
	
	@Test
	def void testMultiThreading() {
		
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
		stream
			.map [ 10 / it ] // throws nullpointer
			.onError [ println('error: ' + it)] // FIX: why not work after and only before?
			.sum
			.then [ println('sum: ' + it) ]
	}
	
	@Test
	def void testPromise() {
		val promise = Integer.promise
		promise.then [ println('got' + it) ]
		10 >> promise
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
	
}
