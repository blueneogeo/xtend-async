package nl.kii.async.annotations.test

import nl.kii.async.annotation.Async
import nl.kii.async.collections.AsyncCached
import nl.kii.async.promise.Input
import org.eclipse.xtend.lib.annotations.Data
import org.junit.Assert
import org.junit.Test

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.util.DateExtensions.*

class AsyncCachedTest {

	int fetches
	val cache = new AsyncCached<User>(1.min, [ fetchUser ])

	@Test
	def void testCached() {
		
		complete
			.call [ cache.get ]
			.call [ cache.get ]
			
		Assert.assertEquals(1, fetches)

		cache.clear

		complete
			.call [ cache.get ]
			.call [ cache.get ]
			
		Assert.assertEquals(2, fetches)
	}
	
	@Async def fetchUser(Input<User> promise) {
		fetches++
		promise.set(new User('emre', 24))
	}
	
}

@Data
package class User {
	String name
	int age
} 
