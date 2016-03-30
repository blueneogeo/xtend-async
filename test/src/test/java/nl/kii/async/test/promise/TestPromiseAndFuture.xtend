package nl.kii.async.test.promise

import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*

class TestPromiseAndFuture {
	
	val exec = Executors.newCachedThreadPool
	
	@Test
	def void testFuture() {
		
		val task = new FutureTask ['hi']
		exec.submit(task)
		val result = task.get
		
		assertEquals('hi', result)
		
	}
	
	@Test
	def void testPromise() {
		
		val promise = exec.promise ['hi']
		val result = promise.asFuture.get
		
		assertEquals('hi', result)
		
	}
	
}
