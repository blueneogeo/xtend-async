package nl.kii.stream.test

import static extension org.junit.Assert.*
import org.junit.Test

import static extension nl.kii.stream.PromiseExt.*
import static extension nl.kii.stream.StreamExt.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.LinkedList

class TestAsyncProcessing {

	@Test
	def void testSimpleAsyncPromise() {
		val result = new AtomicInteger
		power2(2).then [ result.set(it)	]
		0.assertEquals(result.get)
		Thread.sleep(2100)
		4.assertEquals(result.get)
	}

	@Test
	def void testTripleAsyncPromise() {
		val result = new AtomicInteger
		power2(2)
			.async [ power2 ]
			.async [ power2 ]
			.then [	result.set(it) ]
		0.assertEquals(result.get)
		Thread.sleep(5000)
		256.assertEquals(result.get)
	}
	
	@Test
	def void testAsyncMapping() {
		val result = new AtomicReference(new LinkedList<Integer>)
		val s = int.stream << 1 << 2 << 3
		s
			.async [ power2 ]
			.map [ it + 1 ]
			.async [ power2 ]
			.each [	result.get.add(it) ]
		0.assertEquals(result.get.size)
		Thread.sleep(7000) 
		3.assertEquals(result.get.size)
		4.assertEquals(result.get.get(0))
		25.assertEquals(result.get.get(1))
		100.assertEquals(result.get.get(2))
	}
	
	@Test
	def void testAsyncErrorCatching() {
		val result = new AtomicInteger
		val s = int.stream << 1 << 2 << 3
		s
			.async [ throwsError ] // this error should propagate down the chain to the .error handler
			.map [ it + 1 ]
			.async [ power2 ]
			.error [ result.incrementAndGet ]
			.each [	fail('we should not end up here, since an error should be caught instead') ]
		Thread.sleep(7000) 
		3.assertEquals(result.get)
	}
	
	def power2(int i) {
		promise [|
			Thread.sleep(1000)
			return i * i
		]
	}

	def throwsError(int i) {
		promise [|
			Thread.sleep(1000)
			if(true) throw new Exception('something went wrong')
			return i * i
		]
	}
	
}
