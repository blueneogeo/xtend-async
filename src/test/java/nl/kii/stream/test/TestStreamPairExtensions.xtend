package nl.kii.stream.test

import org.junit.Test

import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamAssert.*
import static extension nl.kii.stream.StreamExtensions.*

class TestStreamPairExt {

	@Test
	def void testEachWithPairParams() {
		val p = stream(1->2)
		val p2 = int.stream
		p.onEach [ k, v | p2 << k + v ]
		p2.assertStreamContains(3.value)
	}
	
	@Test
	def void testAsyncWithPairParams() {
		val p = stream(1->2)
		val asynced = p.call [ a, b | power2(a + b) ]
		asynced.assertStreamContains(9.value, finish)
	}
	
	@Test
	def void testMapWithPairs() {
		val p = stream(2)
		val asynced = p
			.map [ it -> it * it ] // returns stream(2->4)
			.map [ key, value | key -> (key + value) * (key + value) ] // returns stream(2->36)
		asynced.assertStreamContains(value(2->36), finish)
	}
	
	@Test
	def void testAsyncPair() {
		val p = stream(2)
		val asynced = p
			.map [ it -> promise(it) ]
			.resolveValue
			.map [ key, value | key -> power2(value) ]
			.resolveValue
		asynced.assertStreamContains(value(2->4), finish)
	}

	private def power2(int i) { (i*i).promise }
	
}
