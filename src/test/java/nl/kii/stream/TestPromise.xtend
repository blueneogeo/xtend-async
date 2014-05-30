package nl.kii.stream

import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.stream.PromiseExtensions.*
import static extension nl.kii.stream.StreamAssert.*

class TestPromise {
	
	@Test
	def void testPromisedAfter() {
		val p = Integer.promise
		val p2 = Integer.promise
		p.then [ it >> p2 ]
		p.set(10)
		p2.assertPromiseEquals(10)
	}
	
	@Test
	def void testPromisedBefore() {
		val p = Integer.promise
		val p2 = Integer.promise
		p.set(10)
		p.then [ it >> p2 ]
		p2.assertPromiseEquals(10)
	}
	
	@Test
	def void testPromiseErrorHandling() {
		val p = 0.promise
		val p2 = boolean.promise
		p.onError [ true >> p2 ]
		p.then [ println(1/it) ] // should create /0 exception
		p2.assertPromiseEquals(true)
	}

	@Test
	def void testPromiseNoHandling() {
		val p = 0.promise
		try {
			// no onError handler specified
			p.then [ println(1/it) ] // should create /0 exception
			fail('we should have gotten an error')
		} catch(Throwable t) {
			// success
		}
	}
	
	@Test
	def void testPromiseErrorChaining() {
		val p = 1.promise
		val p2 = boolean.promise
		p
			.map [it - 1]
			.map [ 1 / it ] // creates /0 exception
			.map [ it + 1]
			.onError [ true >> p2 ]
			.then [ println(it) ]
		p2.assertPromiseEquals(true)
	}

}