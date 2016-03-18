package nl.kii.promise.test

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import nl.kii.async.annotation.Async
import nl.kii.async.annotation.Atomic
import nl.kii.promise.Promise
import nl.kii.promise.Task
import org.junit.Test

import static java.util.concurrent.Executors.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.test.StreamAssert.*
import static extension nl.kii.util.DateExtensions.*
import static extension org.junit.Assert.*

class TestPromiseExtensions {
	
	// CREATION /////////////////////////////////////////////////////////////
	
	@Test
	def void testFuture() {
		val promise = Integer.promise
		val future = promise.asFuture
		promise << 2
		future.done.assertTrue
		future.get.assertEquals(2)
	}

	@Test
	def void testFutureError() {
		val promise = Integer.promise
		val future = promise.asFuture
		promise.error(new Exception)
		try {
			future.get
			fail('get should throw a exception')
		} catch(ExecutionException e) {
			// success when we get here
		}
	}

	// TRANSFORMATIONS //////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val p = new Promise(4)
		val mapped = p.map [ it + 10 ]
		mapped.assertPromiseEquals(14)
	}
	
	@Test
	def void testFlatten() {
		val p1 = new Promise(3)
		val p2 = new Promise<Promise<Integer>> << p1
		val flattened = p2.flatten
		flattened.assertPromiseEquals(3)
	}
	
	// ENDPOINTS ////////////////////////////////////////////////////////////
	
	// TODO: needs better test that uses multithreading
	@Test
	def void testAsync() {
		val s = new Promise(2)
		val asynced = s.map [ power2(it) ].flatten
		asynced.assertPromiseEquals(4)
	}
	
	@Test
	def void testListPromiseToStream() {
		val p = new Promise(#[1, 2, 3])
		val s = p.stream
		s.sum.then [ assertEquals(6, it, 0) ]
	}
	
	@Atomic boolean allDone = false
	@Atomic boolean t2Done = false
	
	@Test
	def void testAll() {
		allDone = false
		t2Done = false
		val t1 = new Task
		val t2 = new Task
		val t3 = new Task
		val a = all(t1, t2, t3)
		t2.then [ t2Done = true ]
		a.then [ allDone = true ]
		assertFalse(allDone)
		assertFalse(t2Done)
		t1.complete 
		assertFalse(allDone)
		assertFalse(t2Done)
		t2.complete 
		assertFalse(allDone)
		assertTrue(t2Done)
		t3.complete 
		assertTrue(allDone)
	}

	@Test
	def void testAllOperator() {
		allDone = false
		t2Done = false
		val t1 = new Task
		val t2 = new Task
		val t3 = new Task
		val a = t1 && t2 && t3
		t2.then [ t2Done = true ]
		a.then [ allDone = true ]
		assertFalse(allDone)
		assertFalse(t2Done)
		t1.complete 
		assertFalse(allDone)
		assertFalse(t2Done)
		t2.complete 
		assertFalse(allDone)
		assertTrue(t2Done)
		t3.complete 
		assertTrue(allDone)
	}
	
	@Atomic boolean anyDone = false

	@Test
	def void testAny() {
		val t1 = new Task
		val t2 = new Task
		val t3 = new Task
		val a = any(t1, t2, t3)
		a.then [ anyDone = true ]
		assertFalse(anyDone)
		t1.complete 
		assertTrue(anyDone)
		t2.complete 
		assertTrue(anyDone)
		t3.complete 
		assertTrue(anyDone)
	}
	
	@Test
	def void testAnyOperator() {
		val t1 = new Task
		val t2 = new Task
		val t3 = new Task
		val a = t1 || t2 || t3
		a.then [ anyDone = true ]
		assertFalse(anyDone)
		t1.complete 
		assertTrue(anyDone)
		t2.complete 
		assertTrue(anyDone)
		t3.complete 
		assertTrue(anyDone)
	}
	
	@Test
	def void testWait() {
		val exec = Executors.newSingleThreadScheduledExecutor
		complete.wait(100.ms, exec.timer).then [ anyDone = true ]
		assertFalse(anyDone) // only done after 100 ms
		Thread.sleep(1000) // wait long enough
		assertTrue(anyDone) // should be done now
	}
	
	@Test
	def void testPromiseChaining() {
		val p = new Promise(1)
		val p2 = p.map [ return new Promise(2) ].resolve
		p2.assertPromiseEquals(2)
	}

	@Test def void testTaskChain() {
		complete
			.map [ sayHello ]
			.resolve
			.map [ sayHello ]
			.resolve
			.map [ sayHello ]
			.resolve
			.asFuture.get
	}

	@Test def void testLongChain() {
		val result = 1.addOne
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.asFuture.get
		assertEquals(10, result)
	}

	@Atomic boolean alwaysDone	
	@Atomic Throwable caughtError
	
	@Test(expected=Exception) def void testLongChainWithError() {
		1.addOne
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [
				if(it != null) {
					println(it)
					throw new Exception('help!')
				} 
				addOne
			]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.asFuture.get
	}	
	
	val threads = newCachedThreadPool

	@Async
	def addOne(int n, Promise<Integer> promise) {
		threads.promise [
			promise << n + 1
		]
	}
	
	@Async
	def sayHello(Task task) {
		threads.promise [ 
			println('hello')
			task.complete
			task
		]
	}
	
	@Test(expected=ExecutionException)
	def void testPromiseErrorChaining() {
		val p = new Promise(1)
		p
			.map [it - 1]
			.map [ 1 / it ] // creates /0 exception
			.map [ it + 1]
			.asFuture.get
	}
	
	@Atomic boolean foundError

	@Test
	def void testPromiseWithLaterError2() {
		foundError = false
		val p = int.promise
		p
			.map [ it / 0 ]
			.then [ fail('it/0 should not succeed') ]
			.on(Throwable) [ foundError = true  ]
		p.set(1)
		assertTrue(foundError)
	}
	
	@Test
	def void testRecursivePromise() {
		assertEquals(15, faculty(5).asFuture.get)
	}

	def faculty(int i) {
		faculty(i, 0)
	}
	
	def power2(int i) { 
		new Promise(i*i)
	}
	
	@Async def void faculty(int i, int result, Promise<Integer> promise) {
		try {
			if(i == 0) promise << result
			else faculty(i - 1, result + i, promise)
		} catch(Throwable t) {
			promise.error(t)
		}
	}
	
}
