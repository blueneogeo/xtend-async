package nl.kii.async.test.stream

import java.util.concurrent.Executors
import org.junit.Test

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.async.test.AsyncJUnitExtensions.*

class TestMultiThreadedProcessing {

	val threads = Executors.newCachedThreadPool

	@Test
	def void testSimpleAsyncPromise() {
		power2(2) <=> 4
	}

	@Test
	def void testTripleAsyncPromise() {
		power2(2)
			.map [ power2 ].flatten
			.map [ power2 ].flatten
			<=> 256
	}
	
	@Test
	def void testAsyncMapping() {
		(1..3).stream
			.map [ power2 ].resolve(1)
			.map [ it + 1 ]
			.map[ power2 ].resolve(1)
			<=> #[4, 25, 100]
	}
	
	@Test
	def void testAsyncErrorCatching() {
		(1..3).stream
			.map [ throwsError ] // this error should propagate down the chain to the .map(Throwable) handler
			.resolve(1)
			.map(Throwable) [ 10 ] // convert the error back into a value
			.sum
			<=> 30D
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
