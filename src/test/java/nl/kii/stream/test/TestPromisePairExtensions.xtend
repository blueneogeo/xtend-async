package nl.kii.stream.test

import org.junit.Test

import static extension nl.kii.stream.PromiseExtensions.*
import static extension nl.kii.stream.PromisePairExtensions.*
import static extension nl.kii.stream.StreamAssert.*

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
		val asynced = p.mapAsync [ a, b | power2(a + b) ]
		asynced.assertPromiseEquals(9)
	}
	
	@Test
	def void testMapWithPairs() {
		val p = promise(2)
		val asynced = p
			.map [ it -> it * it ] // returns promise(2->4)
			.mapToPair [ key, value | key -> (key + value) * (key + value) ] // returns promise(2->36)
		asynced.assertPromiseEquals(2 -> 36)
	}
	
	@Test
	def void testAsyncPair() {
		val p = promise(2)
		val asynced = p
			.asyncToPair [ it -> promise(it) ]
			.asyncToPair [ key, value | key -> power2(value) ] // returns promise(2->4)
		asynced.assertPromiseEquals(2 -> 4)
	}

	private def power2(int i) { (i*i).promise }
	
}