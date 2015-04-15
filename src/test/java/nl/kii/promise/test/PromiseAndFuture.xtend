package nl.kii.promise.test

import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import org.junit.Test

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*

class PromiseAndFuture {
	
	val exec = Executors.newCachedThreadPool
	
	@Test
	def void testFuture() {
		
		val task = new FutureTask [
			'hi'
		]
		exec.submit(task)
		
		val result = task.get
		
		println(result)
		
	}
	
	@Test
	def void testPromise() {
		
		val promise = exec.promise [10]
		
		promise
			.map [ it + 10 ]
			.then [ println(it) ]
			.on(Throwable) [ println(it) ]
		
		Thread.sleep(100)
		
	}
	
}