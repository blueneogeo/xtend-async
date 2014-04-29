package nl.kii.stream.test

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.StreamAssert.*
import static extension nl.kii.stream.StreamExt.*
import static extension nl.kii.stream.StreamPairExt.*

class TestPromisePairExt {

	@Test
	def void testEachWithPairParams() {
		val p = stream(1->2)
		val p2 = int.stream
		p.each [ k, v | p2 << k + v ]
		#[3.value].assertStreamEquals(p2)
	}
	
	@Test
	def void testAsyncWithPairParams() {
		val p = stream(1->2)
		val asynced = p.async [ a, b | power2(a + b) ]
		#[9.value, finish].assertStreamEquals(asynced)
	}
	
	@Test
	def void testMapWithPairs() {
		val p = stream(2)
		val asynced = p
			.map [ it -> it * it ] // returns stream(2->4)
			.mapToPair [ key, value | key -> (key + value) * (key + value) ] // returns stream(2->36)
		#[value(2->36)].assertStreamEquals(asynced)
	}
	
	@Test
	def void testAsyncPair() {
		val p = stream(2)
		val asynced = p
			.asyncToPair [ it -> promise(it) ]
			.asyncToPair [ key, value | key -> power2(value) ] // returns stream(2->4)
		#[value(2->4), finish].assertStreamEquals(asynced)
	}

	private def power2(int i) { (i*i).promise }
	
}