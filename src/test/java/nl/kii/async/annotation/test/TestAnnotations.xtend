package nl.kii.async.annotation.test

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.promise.Promise
import nl.kii.promise.Task
import nl.kii.stream.annotation.Async
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.promise.PromiseExtensions.*

class TestAnnotations {

	@Test
	def void testAsyncPromise() {
		val result = new AtomicInteger
		increment(5)
			.then [ result.set(it) ]
		assertEquals(6, result.get)
	}

	@Test
	def void testAsyncTask() {
		val result = new AtomicBoolean
		printHello('world')
			.then [ result.set(it) ]
		assertEquals(true, result.get)
	}

	@Test
	def void testAsyncErrorHandling() {
		val isError = new AtomicBoolean
		printHello(null)
			.onError [ isError.set(true) ]
			.then [ isError.set(false) ]
		assertEquals(true, isError.get)
	}

	@Test
	def void testAsyncTaskOnExecutor() {
		val exec = newCachedThreadPool
		exec.printHello('christian').then [ println('done!') ]
	}
	
	@Async private synchronized def increment(int number, Promise<Integer> promise) {
		promise << number + 1 
	}

	@Async def printHello(Task task, String name) {
		if(name == null) throw new Exception('name cannot be empty')
		println('hello ' + name)
		task.complete
	}
	
}
