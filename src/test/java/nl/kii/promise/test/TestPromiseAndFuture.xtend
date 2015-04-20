package nl.kii.promise.test

import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import org.junit.Test

import static extension nl.kii.async.ExecutorExtensions.*
import static extension org.junit.Assert.*

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
		val result = promise.future.get
		
		assertEquals('hi', result)
		
	}
	
}