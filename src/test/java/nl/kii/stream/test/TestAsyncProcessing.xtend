package nl.kii.stream.test

import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.StreamExt.*

class TestAsyncProcessing {

	@Test
	def void testSimpleAsyncPromise() {
		power2(2).then [ println('result: ' + it) ]
		println('we are done immediately, and then the result of 4 comes in a second later')
		Thread.sleep(2100)
	}

	@Test
	def void testDoubleAsyncPromise() {
		power2(2)
			.async [ power2 ]
			.then [ println('result: ' + it) ]
		println('we are done immediately, and then the result of 4 comes in a second later')
		Thread.sleep(2100)
	}
	
	@Test
	def void testAsyncMapping() {
		val s = int.stream << 1 << 2 << 3
		s.async [ power2 ].each [ println('result: ' + it) ]
		Thread.sleep(4000) 
	}
	
	def power2(int i) {
		promise [| Thread.sleep(1000) return i * i ]
	}
	
}
