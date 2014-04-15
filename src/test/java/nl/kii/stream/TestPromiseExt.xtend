package nl.kii.stream

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.StreamAssert.*

class TestPromiseExt {
	
	// TRANSFORMATIONS //////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val p = Integer.promise << 4
		val mapped = p.map [ it + 10 ]
		mapped.assertPromiseEquals(14)
	}
	
	@Test
	def void testFlatten() {
		val p1 = Integer.promise << 3
		val p2 = new Promise<Promise<Integer>> << p1
		val flattened = p2.flatten
		flattened.assertPromiseEquals(3)
	}
	
	// ENDPOINTS ////////////////////////////////////////////////////////////
	
	// TODO: needs better test that uses multithreading
	@Test
	def void testAsync() {
		val s = Integer.promise << 2
		val asynced = s.async [ power2(it) ]
		asynced.assertPromiseEquals(4)
	}
	
	private def power2(int i) { (i*i).promise }
	
}