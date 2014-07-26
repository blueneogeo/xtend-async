package nl.kii.promise.test

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Async
import nl.kii.promise.Promise
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamAssert.*
import nl.kii.promise.Task

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
		val p = 0.promise
		val p2 = boolean.promise
		p.onError [ true >> p2 ]
		p.then [ println(1/it) ] // should create /0 exception
		p2.assertPromiseEquals(true)
	}

	@Test
	def void testPromiseNoHandling() {
		val p = 0.promise
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
		val p = 1.promise
		val p2 = p.then [ return 2.promise ]
		p2.assertPromiseEquals(2)
	}

	@Test def void testTaskChain() {
		sayHello
			.then [ return sayHello ]
			.then [ return sayHello ]
			.then [
				sayHello
			]
	}

	@Test def void testLongChain() {
		val alwaysDone = new AtomicBoolean
		val caughtError = new AtomicReference<Throwable>
		1.addOne
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
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
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [
				if(true) throw new Exception('help!') 
				return addOne
			]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.then [ return addOne ]
			.onError [ caughtError.set(it) ]
			.always [ alwaysDone.set(true) ]
			.then [ fail('should not get here' + it)]
		assertEquals(true, alwaysDone.get)
		assertNotNull(caughtError.get)
	}	
	
	val threads = newCachedThreadPool

	@Async
	def addOne(int n, Promise<Integer> promise) {
		threads.run [|
			promise << n + 1
		]
	}
	
	@Async
	def sayHello(Task task) {
		threads.run [| 
			println('hello')
			task.complete
		]
	}
	
	@Test
	def void testPromiseErrorChaining() {
		val p = 1.promise
		val p2 = boolean.promise
		p
			.map [it - 1]
			.map [ 1 / it ] // creates /0 exception
			.map [ it + 1]
			.onError [ true >> p2 ]
			.then [ println(it) ]
		p2.assertPromiseEquals(true)
	}

}