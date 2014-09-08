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
import static extension nl.kii.stream.StreamAssert.*

class TestPromise {
	
	@Test
	def void testPromisedAfter() {
		val p = Integer.promise
		val p2 = Integer.promise
		p.then [ it >> p2 ]
		p.set(10)
		p2.assertPromiseEquals(10)
	}
	
	@Test
	def void testPromisedBefore() {
		val p = Integer.promise
		val p2 = Integer.promise
		p.set(10)
		p.then [ it >> p2 ]
		p2.assertPromiseEquals(10)
	}
	
	@Test
	def void testPromiseErrorHandling() {
		val p = new Promise(0)
		val p2 = boolean.promise
		p.onError [ true >> p2 ]
		p.then [ println(1/it) ] // should create /0 exception
		p2.assertPromiseEquals(true)
	}

	@Test
	def void testPromiseNoHandling() {
		val p = new Promise(0)
		try {
			// no onError handler specified
			p.then [ println(1/it) ] // should create /0 exception
			fail('we should have gotten an error')
		} catch(Throwable t) {
			// success
		}
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
		val alwaysDone = new AtomicBoolean
		val caughtError = new AtomicReference<Throwable>
		1.addOne
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.onError [ caughtError.set(it) ]
			.always [ alwaysDone.set(true) ]
			.assertPromiseEquals(10)
		assertEquals(true, alwaysDone.get)
		assertNull(caughtError.get)
	}
	
	@Test def void testLongChainWithError() {
		val alwaysDone = new AtomicBoolean
		val caughtError = new AtomicReference<Throwable>
		1.addOne
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [
				if(it != null) throw new Exception('help!') 
				addOne
			]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.call [ addOne ]
			.onError [ caughtError.set(it) ]
			.always [ alwaysDone.set(true) ]
			.then [ fail('should not get here' + it)]
		assertEquals(true, alwaysDone.get)
		assertNotNull(caughtError.get)
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
			.onError [ true >> p2 ]
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
			.onError [ foundError = true  ]
		p.set(1)
		assertTrue(foundError)
	}

}
