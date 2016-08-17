package nl.kii.async.fibers.test

import nl.kii.async.annotation.Async
import nl.kii.async.fibers.annotation.SyncVersionOf
import nl.kii.async.promise.Input
import nl.kii.async.promise.Promise
import org.junit.Test

import static nl.kii.async.fibers.FiberExtensions.*
import static org.junit.Assert.*

import static extension nl.kii.async.promise.PromiseExtensions.*

class TestAsyncToSync {
	
	@Test
	def void testInputCall() {
		async [
			assertEquals(12L, await(SomeAsyncExtensions.getAge('jose')))
		]
	}
	
}

class SomeAsyncExtensions {
	
	@Async static def getAge(String user, Input<Long> input) {
		complete
			.map[12L]
			.pipe(input)
	}

	@Async static def Promise<?, Long> foo(String user) {
		promise(12L)
	}
		
}

@SyncVersionOf(SomeAsyncExtensions)
class SomeSyncExtensions {
	
}
