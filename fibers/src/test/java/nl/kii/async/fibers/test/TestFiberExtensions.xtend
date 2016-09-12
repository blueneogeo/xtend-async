package nl.kii.async.fibers.test

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.SuspendableCallable
import nl.kii.async.promise.BlockingExtensions
import org.junit.Ignore
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
	@Ignore
	@Test(timeout=5000)
	def void testAsyncAwait() {
		var result = blocking [
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
	@Ignore
	@Test(expected=ExpectedException, timeout=5000)
	def void testAwaitingErrors() {
		blocking [
			wait(1.sec)
			throw new ExpectedException
		]
	}
	
	static class ExpectedException extends Exception { }

	@Ignore
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

	@Test(timeout=6000)
	def void testStreamAwaitNext() {
		val stream = (1..10).iterator.stream
		blocking [
			for(value : stream.awaitEach) {
				println(value)
			}
			println('done!')
			null
		]
	}


//	@Ignore
//	@Test(timeout=6000)
//	def void testAwaitUncontrolledStream() {
//		val stream = newSink
//		async [ 
//			for(i : 1..10) {
//				wait(50.ms)
//				stream.push(i)
//			}
//		]
//		blocking [
//			for(i : stream.await) {
//				println(i)
//			}
//			10
//		]
//	}
	
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

	/* FIX: iterate has issues with instrumentation!
	 * So far it seems a Quasar problem, for some reason Iterator<T> will not be instrumented correctly,
	 * and the test will give an exception in Eclipse with the agent instrumentor, but NOT when testing
	 * from Gradle! However the Gradle test also breaks when trying to use a .forEach [ println(it) ],
	 * even through the forEach is not running any blocking code.
	 * 
	 * Solution for now seems to be to awaitNext instead on a stream.
	 * 
	@Test(timeout=6000)
	def void testAwaitControlledStream() {
		val stream = newSink
		async [ 
			for(i : 1..10) {
				wait(150.ms)
				stream.push(i)
			}
			stream.complete
		]
		blocking [
			for(i : stream.iterate) {
				println(i)
			}
			null
		]
	}
	*/
	
	/*
	def static <T> StreamIterable<T> iterate(Stream<?, T> stream) {
		new StreamIterable(stream)
	}
	*/
	
}

/* Classes used for testing iterate issues as described above.
 * casting to the actual type does work, but using it as Iterable
 * breaks the fiber code in Eclipse...
class StreamIterable<T> implements Iterable<T> {
	
	val Stream<?, T> stream
	
	new(Stream<?, T> stream) {
		this.stream = stream
	}
	
	@Suspendable
	override StreamLooper<T> iterator() {
		new StreamLooper<T>(stream)
	}
	
	def StreamLooper<T> foo() {
		new StreamLooper<T>(stream)
	}
	
}

class StreamLooper<T> implements Iterator<T> {
	
	val Stream<?, T> stream
	var T nextValue
	
	new(Stream<?, T> stream) {
		this.stream = stream
	}
	
	@Suspendable
	override boolean hasNext() {
		nextValue = stream.awaitNext
		nextValue != null
	}
	
	@Suspendable
	override T next() {
		if(nextValue == null) stream.awaitNext
		else nextValue
	}
	
	override void remove() {
		throw new UnsupportedOperationException("remove is not supported on a stream")
	}
	
}
*/