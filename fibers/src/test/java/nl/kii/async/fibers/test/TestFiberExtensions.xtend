package nl.kii.async.fibers.test

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.SuspendableCallable
import java.util.concurrent.Executors
import nl.kii.async.promise.BlockingExtensions
import nl.kii.async.promise.Promises
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.async.fibers.FiberExtensions.*
import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*

class TestFiberExtensions {

	/**
	 * Tests that we can do some asynchronous operation and wait for it, and the value being returned
	 * all the way out into the outer result.
	 */
	@Test(timeout=5000)
	def void testAsyncAwait() {
		var result = runOnFiber [
			// here is our real test, we do something asynchronously, and then await that result
			val result = async [ doSomethingFiberish return 'hello2' ].await
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
			doSomethingFiberish
			throw new ExpectedException
		]
	}
	
	static class ExpectedException extends Exception { }

	@Test(timeout=6000)
	def void testLoop() {
		val list = runOnFiber [
			val list = newLinkedList()
			for(i : 1..10) {
				doSomethingFiberish
				list.add(i)
				println(i)
			}
			list
		]
		assertEquals(#[1, 2, 3, 4, 5, 6, 7, 8, 9, 10], list)
	}

	@Test
	def void testPromisesCanSuspend() {
		val input = Promises.newInput
			input
				.effect [ doSomethingFiberish ]  // fiber suspension in the effect
				.then [ println(it) ]
		runOnFiber [ input.set(10) 0 ]
		assertTrue(input.fulfilled)
	}


	@Test
	def void testFibersCanSuspendInStreamsAndPromises() {
		val list = runOnFiber [
			(1..10).each // convert to stream
				.effect [ Fiber.sleep(50) ] // fiber suspension in the stream
				.collect // convert to promise
				// .effect [ Fiber.sleep(50) ] // fiber suspension in the promise
				.await // suspension until result
		]
		assertEquals(#[1, 2, 3, 4, 5, 6, 7, 8, 9, 10], list.sort)
	}

	@Test
	def void testStreamsAreFiberInjected() {
		val count = runOnFiber [
			(1..10).stream
				.map [ a, b |
					println(Fiber.currentFiber().id) 
					// this will throw an exception if there is no fiber here
					doSomethingFiberish
					b
				]
				.call [
					async [ doSomethingFiberish ]
				]
				.count
				.await
		]
		println('completed all ' + count)
		assertEquals(10, count)
	}
	
	@Suspendable
	def void doSomethingFiberish() {
		Fiber.sleep(100)
	}
	
	@Test
	def void testAsyncWrapping() {
		val executors = Executors.newScheduledThreadPool(3)
		runOnFiber [
			val task = async [
				val task2 = async [
					val task3 = Promises.newTask(executors) [
						println('A')
					]
					task3.await
					println('B')
				]
				task2.await
				println('C')
			]
			task.await
			println('D')
		]
		println('done')
	}

	/**
	 * FiberExtensions.await can only be called from within a fiber. So to create a fiber,
	 * it is wrapped in an async. To not have the test finish before the async operation
	 * is finished, we block the test with BlockingExtensions.await until the promise
	 * from the outer async has completed. 
	 */
	@Suspendable
	package def <T> T runOnFiber(SuspendableCallable<T> function) {
		BlockingExtensions.block(async(function))
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
//	@Test(timeout=6000)
//	def void testAwaitControlledStream() {
//		val stream = newSink
//		async [ 
//			for(i : 1..10) {
//				Fiber.sleep(1000)
//				stream.push(i)
//			}
//			stream.complete
//		]
//		runOnFiber [
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
				println('S ' + i + ' ' + Fiber.currentFiber().id)
				async [
					println('A ' + i + ' ' + Fiber.currentFiber().id)
					println('S2 ' + i + ' ' + Fiber.currentFiber().id)
					async [
						println('A2 ' + i + ' ' + Fiber.currentFiber().id)
					]
					println('E2 ' + i + ' ' + Fiber.currentFiber().id)
				]
				println('E ' + i + ' ' + Fiber.currentFiber().id)
			]
		}
	}
		
}
