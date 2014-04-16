package nl.kii.stream

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.StreamAssert.*
import static extension org.junit.Assert.*
import java.util.concurrent.ExecutionException

class TestPromiseExt {
	
	// CREATION /////////////////////////////////////////////////////////////
	
	@Test
	def void testFuture() {
		val promise = Integer.promise
		val future = promise.future
		promise << 2
		future.done.assertTrue
		future.get.assertEquals(2)
	}

	@Test
	def void testFutureError() {
		val promise = Integer.promise
		val future = promise.future
		promise.error(new Exception)
		try {
			future.get
			fail('get should throw a exception')
		} catch(ExecutionException e) {
			// success when we get here
		}
	}

	// TRANSFORMATIONS //////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val p = 4.promise
		val mapped = p.map [ it + 10 ]
		mapped.assertPromiseEquals(14)
	}
	
	@Test
	def void testFlatten() {
		val p1 = 3.promise
		val p2 = new Promise<Promise<Integer>> << p1
		val flattened = p2.flatten
		flattened.assertPromiseEquals(3)
	}
	
	// ENDPOINTS ////////////////////////////////////////////////////////////
	
	// TODO: needs better test that uses multithreading
	@Test
	def void testAsync() {
		val s = 2.promise
		val asynced = s.async [ power2(it) ]
		asynced.assertPromiseEquals(4)
	}
	
	private def power2(int i) { (i*i).promise }
	
}