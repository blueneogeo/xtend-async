package nl.kii.stream.test

import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test

import static extension org.junit.Assert.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.PromiseExtensions.*
import static extension nl.kii.stream.PromiseExtensions.*

class TestAsyncProcessing {

	@Test
	def void testSimpleAsyncPromise() {
		val result = new AtomicInteger
		power2(2).then [ result.set(it)	]
		0.assertEquals(result.get)
		Thread.sleep(210)
		4.assertEquals(result.get)
	}

	@Test
	def void testTripleAsyncPromise() {
		val result = new AtomicInteger
		power2(2)
			.mapAsync [ power2 ]
			.mapAsync [ power2 ]
			.then [	result.set(it) ]
		0.assertEquals(result.get)
		Thread.sleep(500)
		256.assertEquals(result.get)
	}
	
	@Test
	def void testAsyncMapping() {
		val result = new AtomicReference(new LinkedList<Integer>)
		val s = int.stream << 1 << 2 << 3
		s
			.mapAsync [ power2 ]
			.map [ it + 1 ]
			.mapAsync [ power2 ]
			.onEach [	result.get.add(it) ]
		0.assertEquals(result.get.size)
		Thread.sleep(700) 
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
			.mapAsync [ throwsError ] // this error should propagate down the chain to the .error handler
			.map [ it + 1 ]
			.mapAsync [ power2 ]
			.onError [ result.incrementAndGet ]
			.onEach [	fail('we should not end up here, since an error should be caught instead') ]
		Thread.sleep(700) 
		3.assertEquals(result.get)
	}
	
	def power2(int i) {
		promise [|
			Thread.sleep(100)
			return i * i
		]
	}

	def throwsError(int i) {
		promise [|
			Thread.sleep(100)
			if(true) throw new Exception('something went wrong')
			return i * i
		]
	}
	
}
