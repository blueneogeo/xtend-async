package nl.kii.promise.test

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import nl.kii.async.annotation.Atomic
import nl.kii.promise.Promise
import nl.kii.promise.Task
import org.junit.Test

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.test.StreamAssert.*
import static extension org.junit.Assert.*

class TestPromiseExtensions {
	
	// CREATION /////////////////////////////////////////////////////////////
	
	@Test
	def void testFuture() {
		val promise = Integer.promise
		val future = promise.future
		promise << 2
		future.done.assertTrue
		future.get.assertEquals(2)
	}

	@Test
	def void testFutureError() {
		val promise = Integer.promise
		val future = promise.future
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
		val timerFn = [ long delayMs, =>void fn | exec.schedule(fn, delayMs, TimeUnit.MILLISECONDS) return ]
		complete.wait(100, timerFn).then [ anyDone = true ]
		assertFalse(anyDone) // only done after 100 ms
		Thread.sleep(1000) // wait long enough
		assertTrue(anyDone) // should be done now
	}
	
	private def power2(int i) { new Promise(i*i) }
	
}