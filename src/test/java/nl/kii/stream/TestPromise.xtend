package nl.kii.stream

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*

class TestPromise {
	
	@Test
	def void testPromisedAfter() {
		val p = Integer.promise
		p.then [ println(it) ]
		p.apply(10)
	}
	
	@Test
	def void testPromisedBefore() {
		val p = Integer.promise
		p.apply(10)
		p.then [ println(it) ]
	}


}