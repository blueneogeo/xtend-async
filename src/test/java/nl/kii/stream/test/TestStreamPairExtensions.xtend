package nl.kii.stream.test

import nl.kii.promise.Promise
import org.junit.Test

import static extension nl.kii.stream.StreamAssert.*
import static extension nl.kii.stream.StreamExtensions.*

class TestStreamPairExt {

	@Test
	def void testMapWithRoot() {
		val p = datastream(2)
		val asynced = p
			.map [ it * it ] // returns stream(2->4)
			.map [ key, value | (key + value) * (key + value) ] // returns stream(2->36)
			.onEach [ r, it | println(r->it)]
		//asynced.assertStreamContains(value(2->36), finish)
	}
	
	@Test
	def void testAsyncPair() {
		val p = datastream(2)
		val asynced = p
			.map [ new Promise(it) ]
			.resolve
			.map [ power2 ]
			.resolve
			.onEach [ r, it | println(r->it)]
		// asynced.assertStreamContains(value(2->4), finish)
	}

	private def power2(int i) { new Promise(i*i) }
	
}
