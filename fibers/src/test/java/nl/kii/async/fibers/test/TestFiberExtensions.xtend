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
	@Test(timeout=5000)
	def void testAsyncAwait() {
		var result = blocking [
			// here is our real test, we do something asynchronously, and then await that result
			val result = async [ wait(500.ms) return 'hello2' ].await
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
	@Test(expected=ExpectedException, timeout=5000)
	def void testAwaitingErrors() {
		blocking [
			wait(1.sec)
			throw new ExpectedException
		]
	}
	
	static class ExpectedException extends Exception { }

	@Test(timeout=6000)
	def void testLoop() {
		val list = blocking [
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
	@Suspendable
	package def <T> T blocking(SuspendableCallable<T> function) {
		BlockingExtensions.await(async(function))
	}

//	@Ignore
//	@Test(timeout=6000)
//	def void testStreamAwaitNext() {
//		val stream = (1..10).iterator.stream
//		blocking [
//			for(value : stream.awaitEach) {
//				println(value)
//			}
//			println('done!')
//			null
//		]
//	}
//
//	/*
//	 * So far it seems a Quasar problem, for some reason Iterator<T> will not be instrumented correctly,
//	 * and the test will give an exception in Eclipse with the agent instrumentor, but NOT when testing
//	 * from Gradle! However the Gradle test also breaks when trying to use a .forEach [ println(it) ],
//	 * even through the forEach is not running any blocking code.
//	 * 
//	 * Solution for now seems to be to awaitNext instead on a stream.
//	 */ 
//	@Ignore
//	@Test(timeout=6000)
//	def void testAwaitControlledStream() {
//		val stream = newSink
//		async [ 
//			for(i : 1..10) {
//				wait(150.ms)
//				stream.push(i)
//			}
//			stream.complete
//		]
//		blocking [
//			for(i : stream.awaitEach) {
//				println(i)
//			}
//			null
//		]
//	}
	
}
