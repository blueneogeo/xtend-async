package nl.kii.async.promise.test

import nl.kii.async.annotation.Atomic
import nl.kii.async.promise.Input
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.async.promise.PromiseExtensions.*

class TestPromise {
	
	@Atomic int result = 0

	@Test def void canBeFulfilledBeforeListening() {
		val promise = new Input<Integer>
		promise.set(1)
		promise.then [ result = it ]
		assertEquals(1, result)
	}
	
	@Test def void canBeFulfilledAfterListening() {
		val promise = new Input<Integer>
		promise.then [ result = it ]
		assertEquals(0, result)
		promise.set(1)
		assertEquals(1, result)
	}
	
	@Test def void silentlyFailsWithoutHandler() {
		val promise = new Input<Integer>
		promise.set(1)
		promise.then [ result = it / 0 ] // throws exception, but is caught
		assertEquals(0, result)
	}
	
	@Test def void canCatchErrorsBeforeListening() {
		val promise = new Input<Integer>
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(Throwable) [ result = 1 ] // error is caught and result is set
		assertEquals(1, result)
	}

	@Test def void canCatchErrorsAfterListening() {
		val promise = new Input<Integer>
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(Throwable) [ result = 1 ] // error is caught and result is set
		assertEquals(1, result)
	}
	
	@Test def void canCatchSpecificErrors() {
		val promise = new Input<Integer>
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(NullPointerException) [ fail('the error is not a nullpointer exception') ]
			.on(ArithmeticException) [ result = 1 ]
		assertEquals(1, result)
	}

}
