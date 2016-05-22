package nl.kii.async.stream.test

import java.util.concurrent.ExecutionException
import org.junit.Test

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.util.JUnitExtensions.*
import java.util.concurrent.Executors

class TestStreamErrorHandling {

	val executor = Executors.newSingleThreadExecutor
	
	@Test(expected=ExecutionException)
	def void testUncaughtErrorsStopTheStream() {
		val errors = String.sink;
		(1..10).iterator.stream
			.map [ 1/(it-5)*0 + it ] // 5 gives a /0 exception
			.map [ 1/(it-7)*0 + it ] // 7 also gives the exception
			.on(Exception) [ errors << message ] // not filtering errors here
			.collect // so this collect fails
			.await <=> null
	}

	@Test
	def void testCatchingErrors() {
		val errors = String.sink;
		(1..10).stream
			.map [ 1/(it-5)*0 + it ] // 5 gives a /0 exception
			.map [ 1/(it-7)*0 + it ] // 7 also gives the exception
			.effect(Exception) [ message >> errors ] // filter errors here
			.collect // so this collect succeeds
			.await <=> #[1, 2, 3, 4, 6, 8, 9, 10] // 5 and 7 are missing
	}
	
	@Test
	def void testMappingErrors() {
		(1..10).stream
			.effect [ if(it == 3 || it == 5) throw new Exception ]
			.map(Throwable) [ 0 ]
			.collect
			.await <=> #[1, 2, 0, 4, 0, 6, 7, 8, 9, 10 ]
	}
	
	@Test
	def void testAsyncMappingErrors() {
		(1..10).stream
			.effect [ if(it == 3 || it == 5) throw new Exception ]
			.call(Throwable) [ executor.promise [ Thread.sleep(100) return 0 ] ]
			.collect
			.await <=> #[1, 2, 0, 4, 0, 6, 7, 8, 9, 10 ]
	}
	
}
