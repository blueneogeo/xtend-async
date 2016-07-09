package nl.kii.async.promise.test

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import nl.kii.async.annotation.Async
import nl.kii.async.annotation.Atomic
import nl.kii.async.promise.Input
import nl.kii.async.promise.Task
import org.junit.Assert
import org.junit.Test

import static java.util.concurrent.Executors.*

import static extension nl.kii.async.promise.BlockingExtensions.*
import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.util.DateExtensions.*
import static extension nl.kii.util.JUnitExtensions.*
import static extension org.junit.Assert.*

class TestPromiseExtensions {
	
	// CREATION /////////////////////////////////////////////////////////////
	
	@Test
	def void testFuture() {
		val promise = new Input<Integer>
		val future = promise.asFuture
		promise << 2
		future.done.assertTrue
		future.get.assertEquals(2)
	}

	@Test
	def void testFutureError() {
		val promise = new Input<Integer>
		val future = promise.asFuture
		promise.error(new Exception)
		try {
			future.get
			Assert.fail('get should throw a exception')
		} catch(ExecutionException e) {
			// success when we get here
		}
	}

	// TRANSFORMATIONS //////////////////////////////////////////////////////
	
	@Test
	def void testMap() {
		val p = new Input(4)
		val mapped = p.map [ it + 10 ]
		14 <=> mapped.await
	}
	
	@Test
	def void testFlatten() {
		val p1 = new Input(3)
		val p2 = new Input<Input<Integer>> << p1
		val flattened = p2.flatten
		flattened.await <=> 3
	}
	
	// ENDPOINTS ////////////////////////////////////////////////////////////
	
	// TODO: needs better test that uses multithreading
	@Test
	def void testAsync() {
		val s = new Input(2)
		val asynced = s.map [ power2(it) ].flatten
		asynced.await <=> 4
	}
	
//	@Test
//	def void testListPromiseToStream() {
//		val p = new Input(#[1, 2, 3])
//		val s = p.stream
//		s.sum.then [ assertEquals(6, it, 0) ]
//	}
	
	@Atomic boolean allDone = false
	@Atomic boolean t2Done = false
	
	@Test
	def void testAll() {
		val t1 = new Task
		val t2 = new Task
		val t3 = new Task
		val all = all(t1, t2, t3)
		assertFalse(t1.fulfilled)
		assertFalse(t2.fulfilled)
		assertFalse(t3.fulfilled)
		assertFalse(all.fulfilled)
		t1.complete 
		assertTrue(t1.fulfilled)
		assertFalse(t2.fulfilled)
		assertFalse(t3.fulfilled)
		assertFalse(all.fulfilled)
		t2.complete 
		assertTrue(t1.fulfilled)
		assertTrue(t2.fulfilled)
		assertFalse(t3.fulfilled)
		assertFalse(all.fulfilled)
		t3.complete 
		assertTrue(t1.fulfilled)
		assertTrue(t2.fulfilled)
		assertTrue(t3.fulfilled)
		assertTrue(all.fulfilled)
	}

	@Test(timeout=500)
	def void testAllOperator() {
		allDone = false
		t2Done = false
		val t1 = new Task
		val t2 = new Task
		val t3 = new Task
		val a = t1 && t2 && t3
//		t2.then [ t2Done = true ]
//		a.then [ allDone = true ]
//		assertFalse(allDone)
//		assertFalse(t2Done)
		t1.complete 
//		assertFalse(allDone)
//		assertFalse(t2Done)
		t2.complete 
//		assertFalse(allDone)
//		assertTrue(t2Done)
		t3.complete 
		a.await
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
		complete.delay(100.ms, exec.timerFn).then [ anyDone = true ]
		assertFalse(anyDone) // only done after 100 ms
		Thread.sleep(1000) // wait long enough
		assertTrue(anyDone) // should be done now
	}
	
	@Test
	def void testPromiseChaining() {
		val p = new Input(1)
		val p2 = p.map [ return new Input(2) ].flatten
		p2.await <=> 2
	}

	@Test def void testTaskChain() {
		complete
			.map [ sayHello ]
			.flatten
			.map [ sayHello ]
			.flatten
			.map [ sayHello ]
			.flatten
			.await
	}

	@Test def void testLongChain() {
		promise(1)
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.await <=> 5
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
			.await
	}	
	
	val threads = newCachedThreadPool

	@Async
	def addOne(Integer n, Input<Integer> promise) {
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
	
	@Test(expected=ArithmeticException)
	def void testPromiseErrorChaining() {
		new Input(1)
			.map [it - 1]
			.map [ 1 / it ] // creates /0 exception
			.map [ it + 1]
			.await
	}
	
	@Atomic boolean foundError

	@Test
	def void testPromiseWithLaterError2() {
		foundError = false
		val p = new Input<Integer>
		p
			.map [ it / 0 ]
			.then [ Assert.fail('it/0 should not succeed') ]
			.on(Throwable) [ foundError = true  ]
		p.set(1)
		foundError <=> true
	}
	
	@Test
	def void testRecursivePromise() {
		faculty(5).await <=> 15
	}

	def faculty(int i) {
		faculty(i, 0)
	}
	
	def power2(int i) { 
		new Input(i*i)
	}
	
	@Async def void faculty(int i, int result, Input<Integer> promise) {
		try {
			if(i == 0) promise << result
			else faculty(i - 1, result + i, promise)
		} catch(Throwable t) {
			promise.error(t)
		}
	}
	
}
