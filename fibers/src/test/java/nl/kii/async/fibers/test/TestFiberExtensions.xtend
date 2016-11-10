package nl.kii.async.fibers.test

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.SuspendableCallable
import nl.kii.async.promise.BlockingExtensions
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.async.fibers.FiberExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.util.DateExtensions.*

class TestFiberExtensions {

	/**
	 * Tests that we can do some asynchronous operation and wait for it, and the value being returned
	 * all the way out into the outer result.
	 */
	@Test(timeout=5000)
	def void testAsyncAwait() {
		var result = runOnFiber [
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
		runOnFiber [
			wait(1.sec)
			throw new ExpectedException
		]
	}
	
	static class ExpectedException extends Exception { }

	@Test(timeout=6000)
	def void testLoop() {
		val list = runOnFiber [
			val list = newLinkedList()
			for(i : 1..10) {
				wait(50.ms)
				list.add(i)
				println(i)
			}
			list
		]
		assertEquals(#[1, 2, 3, 4, 5, 6, 7, 8, 9, 10], list)
	}

	@Test
	def void testStreamsAreFiberInjected() {
		val count = runOnFiber [
			(1..10).stream
				.map [ a, b |
					println(Fiber.currentFiber()?.id) 
					// this will throw an exception if there is no fiber here
					doSomethingFiberish
					b
				]
				.call [
					async [ doSomethingFiberish ]
				]
				// .perform [ async [ testSomeQuery4 ] ]
//				.effect [ println('completed ' + it) ]
				.count
				.await
		]
		println('completed all ' + count)
	}
	
	@Suspendable
	def void doSomethingFiberish() {
		Fiber.sleep(100)
	}

	/**
	 * FiberExtensions.await can only be called from within a fiber. So to create a fiber,
	 * it is wrapped in an async. To not have the test finish before the async operation
	 * is finished, we block the test with BlockingExtensions.await until the promise
	 * from the outer async has completed. 
	 */
	@Suspendable
	package def <T> T runOnFiber(SuspendableCallable<T> function) {
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
	
	@Test
	def void getScheduler() {
		for(i : 1..1) {
			async [
				println('S ' + i + ' ' + Fiber.currentFiber()?.id)
				async [
					println('A ' + i + ' ' + Fiber.currentFiber()?.id)
					println('S2 ' + i + ' ' + Fiber.currentFiber()?.id)
					async [
						println('A2 ' + i + ' ' + Fiber.currentFiber()?.id)
					]
					println('E2 ' + i + ' ' + Fiber.currentFiber()?.id)
				]
				println('E ' + i + ' ' + Fiber.currentFiber()?.id)
			]
		}
	}
		
}
