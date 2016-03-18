package nl.kii.stream.test

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Test

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension org.junit.Assert.*

class TestMultiThreadedProcessing {

	val threads = Executors.newCachedThreadPool

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
			.map [ power2 ].flatten
			.map [ power2 ].flatten
			.then [	result.set(it) ]
		0.assertEquals(result.get)
		Thread.sleep(500)
		256.assertEquals(result.get)
	}
	
	@Test
	def void testAsyncMapping() {
		val s = (1..3).stream
		val result = s
			.map [ power2 ].resolve(1)
			.map [ it + 1 ]
			.map[ power2 ].resolve(1)
			.collect.first
			.asFuture.get
		assertEquals(#[4, 25, 100], result)
	}
	
	@Test
	def void testAsyncErrorCatching() {
		val s = (1..3).stream
		val sum = s
			.map [ throwsError ] // this error should propagate down the chain to the .map(Throwable) handler
			.resolve(1)
			.map(Throwable) [ 10 ] // convert the error back into a value
			.sum.first
			.asFuture.get
		assertEquals(30, sum, 0) // 30 = 3 times the error value of 10
	}
	
	def power2(int i) {
		threads.promise [|
			Thread.sleep(100)
			return i * i
		]
	}

	def throwsError(int i) {
		threads.promise [
			Thread.sleep(100)
			if(threads != null) throw new Exception('something went wrong')
			return i * i
		]
	}
	
}
