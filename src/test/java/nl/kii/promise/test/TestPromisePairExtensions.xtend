package nl.kii.promise.test

import org.junit.Test

import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamAssert.*
import nl.kii.promise.Promise

class TestPromisePairExtensions {

	@Test
	def void testThenWithPairParams() {
		val p = (int->int).promisePair << (1->2)
		val p2 = Integer.promise
		p.then [ k, v | p2 << k + v ]
		p2.assertPromiseEquals(3)
	}
	
	@Test
	def void testAsyncWithPairParams() {
		val p = (int->int).promisePair << (1->2)
		val asynced = p.call [ a, b | power2(a + b) ]
		asynced.assertPromiseEquals(9)
	}
	
	@Test
	def void testMapWithPairs() {
		val p = new Promise(2)
		val asynced = p
			.map [ it -> it * it ] // returns promise(2->4)
			.map [ key, value | key -> (key + value) * (key + value) ] // returns promise(2->36)
		asynced.assertPromiseEquals(2 -> 36)
	}
	
	@Test
	def void testAsyncPair() {
		val p = new Promise(2)
		val asynced = p
			.call2 [ it -> new Promise(it) ]
			.call2 [ key, value | key -> power2(value) ]
		asynced.assertPromiseEquals(2 -> 4)
	}

	@Test
	def void testAsyncPairUsingFlatmap() {
		val p = new Promise(2)
		val asynced = p
			.call2 [ it -> new Promise(it) ]
			.call2 [ key, value | key -> power2(value) ]
		asynced.assertPromiseEquals(2 -> 4)
	}

	private def power2(int i) { new Promise(i*i) }
	
}