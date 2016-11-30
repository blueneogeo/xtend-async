package nl.kii.async.stream.test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import nl.kii.async.stream.Sink
import org.junit.Test

import static nl.kii.async.promise.Promises.*
import static org.junit.Assert.*

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*

class TestStreamThreadSafety {
	
	val executor = Executors.newCachedThreadPool
	var int counter = 0
	
	@Test
	def void testSynchronizedStream() {
		val threads = 100
		val iterations = 50_000

		// create a stream we will just dump data into		
		val input = new Sink<Integer> {
			override onNext() { }
			override onClose() { }
		}
		// listen on the output and count the results
		input
			.synchronize
			.effect [ counter = counter + 1 ]
			.count
			// .then [ println('got count ' + it) ]
		// create a bunch of threads that push values in
		val latch = new CountDownLatch(threads)
		for(thread : 1..threads) {
			// println('starting thread ' + thread)
			executor.execute [
				val start = (thread-1) * iterations
				val end = thread * iterations - 1
				for(i : start..end) {
					input.push(i)
				}
				// println('finished thread ' + thread)
				latch.countDown
			]
		}
		latch.await
		input.complete
		// when all threads are done, check our results
		// println('all threads done')
		// println('results: ' + counter + ', expected: ' + (threads * iterations))
		assertEquals(threads * iterations, counter)
	}

	@Test
	def void testStreamWithAsyncCall() {
		val threads = 10
		val iterations = 5_000

		// create a stream we will just dump data into		
		val input = new Sink<Integer> {
			override onNext() { }
			override onClose() { }
		}
		// listen on the output and count the results
		input
			// .perform [ newPromise(executor) [ ] ] // for the executors pushing in FIX: NOT WORKING
			.synchronize 
			.effect [ counter = counter + 1 ]
			.count
			.then [ println('got count ' + it) ]
		// create a bunch of threads that push values in
		val latch = new CountDownLatch(threads)
		for(thread : 1..threads) {
			executor.execute [
				val start = (thread-1) * iterations
				val end = thread * iterations - 1
				for(i : start..end) {
					input.push(i)
				}
				latch.countDown
			]
		}
		latch.await		
		Thread.sleep(200) // let processes finish
		// when all threads are done, check our results
		// println('all threads done')
		// println('results: ' + counter + ', expected: ' + (threads * iterations))
		assertEquals(threads * iterations, counter)
	}
	
}