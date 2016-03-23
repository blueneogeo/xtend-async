package nl.kii.promise.test

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Async
import nl.kii.async.annotation.Atomic
import nl.kii.promise.Promise
import nl.kii.promise.Task
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.test.StreamAssert.*
import static extension nl.kii.util.JUnitExtensions.*

class TestPromise {
	
	@Atomic int result = 0

	@Test def void canBeFulfilledBeforeListening() {
		val promise = new Promise<Integer>
		0 <=> result
		promise.set(1)
		promise.then [ result = it ]
		1 <=> result
	}
	
	@Test def void canBeFulfilledAfterListening() {
		val promise = new Promise<Integer>
		promise.then [ result = it ]
		0 <=> result
		promise.set(1)
		1 <=> result
	}
	
	@Test def void silentlyFailsWithoutHandler() {
		val promise = new Promise<Integer>
		0 <=> result
		promise.set(1)
		promise.then [ result = it / 0 ] // throws exception, but is caught
		0 <=> result
	}
	
	@Test def void canCatchErrorsBeforeListening() {
		val promise = new Promise<Integer>
		0 <=> result
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(Throwable) [ result = 1 ] // error is caught and result is set
		1 <=> result
	}

	@Test def void canCatchErrorsAfterListening() {
		val promise = new Promise<Integer>
		0 <=> result
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(Throwable) [ result = 1 ] // error is caught and result is set
		1 <=> result
	}
	
	@Test def void canCatchSpecificErrors() {
		val promise = new Promise<Integer>
		0 <=> result
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(NullPointerException, true) [ fail('the error is not a nullpointer exception') ]
			.on(ArithmeticException, true) [ result = 1 ]
			.on(Throwable) [ fail('this may no longer match, the error has already been caught') ]
		1 <=> result
	}
	
	@Test
	def void testPromiseChaining() {
		val p = new Promise(1)
		val p2 = p.map [ return new Promise(2) ].resolve
		p2.assertPromiseEquals(2)
	}

	@Test def void testTaskChain() {
		sayHello
			.map [ return sayHello ]
			.resolve
			.map [ return sayHello ]
			.resolve
			.then [
				sayHello
			]
	}

	@Test def void testLongChain() {
		1.addOne
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.on( Throwable ) [ fail( 'should not get here' ) ]
			.assertPromiseEquals(10)
	}

	@Atomic boolean alwaysDone	
	@Atomic Throwable caughtError
	
	@Test def void testLongChainWithError() {
		1.addOne
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ throw new Exception('Forced!') ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.on( Throwable ) [ assertEquals( 'Forced!', message ) ]
			.then [ fail( 'should not get here' ) ]
	}	
	
	val threads = newCachedThreadPool

	@Async
	def addOne(int n, Promise<Integer> promise) {
		threads.promise [|
			promise << n + 1
		]
	}
	
	@Async
	def sayHello(Task task) {
		threads.promise [| 
			println('hello')
			task.complete
			task
		]
	}
	
	@Test
	def void testPromiseErrorChaining() {
		val p = new Promise(1)
		val p2 = boolean.promise
		p
			.map [it - 1]
			.map [ 1 / it ] // creates /0 exception
			.map [ it + 1]
			.on(Throwable) [ true >> p2 ]
			.then [ println(it) ]
		p2.assertPromiseEquals(true)
	}
	
	@Test
	def void testPromiseChain() {
		val p = int.promise
		p
			.map [ it + 1 ]
			.then [ println(it) ]
			.then [ println(it) ]
			.then [ println(it) ]
		p.set(1)
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
		faculty(5).then [ 
			println(it)
			assertEquals(15, it)
		]
	}

	def faculty(int i) {
		faculty(i, 0)
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
