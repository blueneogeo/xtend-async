package nl.kii.async.fibers.test

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.SuspendableCallable
import nl.kii.async.promise.BlockingExtensions
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.async.fibers.FiberExtensions.*
import static extension nl.kii.util.DateExtensions.*

class TestFiberExtensions {

	/**
	 * Tests that we can do some asynchronous operation and wait for it, and the value being returned
	 * all the way out into the outer result.
	 */
	@Test(timeout=5000) @Suspendable
	def void testAsyncAwait() {
		var result = callBlocking [
			// here is our real test, we do something asynchronously, and then await that result
			val result = async [ wait(2.secs) return 'hello2' ].await
			println('got ' + result)
			return result
		]
		assertEquals('hello2', result)
	}

	/**
	 * Tests that exceptions thrown inside async operations escalate into normal throws all the way out,
	 * just like values do. The ExpectedException in the inner async is always thrown and escalates all
	 * the way out to the calling test.
	 */
	@Test(expected=ExpectedException, timeout=5000) @Suspendable
	def void testAwaitingErrors() {
		callBlocking [
			wait(1.sec)
			if(Class.name != null) throw new ExpectedException
		]
	}
	
	static class ExpectedException extends Exception { }

	@Test(timeout=5000) @Suspendable
	def void testLoop() {
		val list = callBlocking [
			val list = newLinkedList()
			for(i : 1..10) {
				wait(50.ms)
				list.add(i)
				println(i)
			}
			list
		]
		assertEquals((1..10).toList, list)
	}

	/**
	 * FiberExtensions.await can only be called from within a fiber. So to create a fiber,
	 * it is wrapped in an async. To not have the test finish before the async operation
	 * is finished, we block the test with BlockingExtensions.await until the promise
	 * from the outer async has completed. 
	 */
	package def <T> T callBlocking(SuspendableCallable<T> function) {
		BlockingExtensions.await(
			async(function)
		)
	}
	
}
