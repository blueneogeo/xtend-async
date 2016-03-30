package nl.kii.async.test.promise

import nl.kii.async.annotation.Atomic
import nl.kii.promise.Promise
import org.junit.Test

import static org.junit.Assert.*

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
			.on(Throwable, true) [ result = 1 ] // error is caught and result is set
		1 <=> result
	}

	@Test def void canCatchErrorsAfterListening() {
		val promise = new Promise<Integer>
		0 <=> result
		promise.set(1)
		promise
			.then [ result = it / 0 ] // throws exception, but is caught
			.on(Throwable, true) [ result = 1 ] // error is caught and result is set
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
			.on(Exception, true) [ fail('this may no longer match, the error has already been caught') ]
		1 <=> result
	}

}
