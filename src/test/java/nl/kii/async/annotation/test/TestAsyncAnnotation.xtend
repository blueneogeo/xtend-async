package nl.kii.async.annotation.test

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.async.annotation.Async
import nl.kii.promise.Promise
import nl.kii.promise.Task
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.promise.PromiseExtensions.*

class TestAsyncAnnotation {

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
		assertTrue(result.get)
	}

	@Test
	def void testAsyncErrorHandling() {
		val isError = new AtomicBoolean
		printHello(null)
			.on(Throwable) [ isError.set(true) ]
			.then [ isError.set(false) ]
		assertTrue(isError.get)
	}

	@Test
	def void testAsyncTaskOnExecutor() {
		val success = new AtomicBoolean
		val exec = newCachedThreadPool
		exec.printHello('christian').then [ success.set(true) ]
		Thread.sleep(10)
		assertTrue(success.get)
	}
	
	@Async def increment(int number, Promise<Integer> promise) {
		promise << number + 1 
	}

	@Async(true) def printHello(Task task, String name) {
		if(name == null) throw new Exception('name cannot be empty')
		println('hello ' + name)
		task.complete
	}

	/* 
	 * Currently not supported because Xtend AA's do not fully support typeparameters
	 * 	
	@Test
	def void testStaticGenerticPromise() {
		val p = justPromise('test')
		assertEquals('test', p.get)
	}
	
	@Async static def <T> justPromise(T value, Promise<T> promise) {
		promise.set(value)
	}
	*/
	
}
