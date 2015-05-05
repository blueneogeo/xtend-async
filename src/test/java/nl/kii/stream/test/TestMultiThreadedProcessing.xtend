package nl.kii.stream.test

import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
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
		val result = new AtomicReference(new LinkedList<Integer>)
		val s = int.stream << 1 << 2 << 3
		s
			.map [ power2 ].resolve(1)
			.map [ it + 1 ]
			.map[ power2 ].resolve(1)
			.effect [ result.get.add(it) ]
			.start
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
			.map [ throwsError ] // this error should propagate down the chain to the .error handler
			.resolve(1)
//			.onError [ println('caught error! ' + message) ]
			.map [ it + 1 ]
			.map [ power2 ]
			.resolve
			.on(Exception) [ result.incrementAndGet ]
			.effect [ println('result ' + it) ]
			.start
		Thread.sleep(500)
		3.assertEquals(result.get)
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
