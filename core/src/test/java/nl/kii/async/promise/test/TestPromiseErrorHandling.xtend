package nl.kii.async.promise.test

import nl.kii.async.annotation.Atomic
import nl.kii.async.promise.Input
import org.junit.Test

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.util.JUnitExtensions.*

class TestPromiseErrorHandling {
	
	@Atomic int value
	@Atomic boolean match1
	@Atomic boolean match2
	@Atomic boolean match3
	@Atomic boolean match4
	
	@Test
	def void canMonitorErrors() {
		val p = new Input<Integer>
		p
			.map [ it / 0 ]
			.then [ fail('an error should occur') ]
			.on(Throwable) [ match1 = true ]
		p << 10
		true <=> match1
	}
	
	@Test
	def void canMatchErrorTypes() {
		val p = new Input<Integer>
		p
			.map [ it / 0 ]
			.then [ fail('an error should occur') ]
			.on(ArithmeticException) [ match1 = true ]
			.on(IllegalArgumentException) [ match2 = true ]
			.on(Exception) [ match3 = true ]
			.on(Throwable) [ match4 = true ]
		p << 10
		true <=> match1
		false <=> match2
		true <=> match3
		true <=> match4
	}
	
	@Test
	def void canMapErrors() {
		val p = new Input<Integer>
		p
			.map [ it / 0 ]
			.map(ArithmeticException) [ 10 ] // swallows the error and changes it to 10
			.on(Exception) [ println(cause); match1 = true ] // should no longer match
			.then [  match2 = true ] // should run
		p << 10
		false <=> match1
		true <=> match2
	}

	@Test
	def void canFilterMapErrors() {
		val p = new Input<Integer>
		p
			.map [ it / 0 ]
			.map(IllegalArgumentException) [ 10 ] // wrong type
			.map(ArithmeticException) [ 20 ] // matches
			.map(Throwable) [ 30 ] // error was matched and filtered above
			.then [ value = it ]
		p << 10
		20 <=> value
	}
	
	@Test
	def void canFilterAsyncMapErrors() {
		val p = new Input<Integer>
		p
			.map [ it / 0 ]
			.call(IllegalArgumentException) [ 20.promise ]
			.call(ArithmeticException) [ 30.promise ]
			.call(Throwable) [ 40.promise ]
			.then [ value = it ]
		p << 10
		30 <=> value
	}
	
}
