package nl.kii.async

import nl.kii.async.annotation.Async
import nl.kii.promise.Promise
import org.eclipse.xtend.lib.annotations.Data
import org.junit.Assert
import org.junit.Test

import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.util.DateExtensions.*

class AsyncCachedTest {

	int fetches
	val cache = new AsyncCached<User>(1.min) [ fetchUser ]

	@Test
	def void testCached() {
		
		complete
			.call [ cache.get ]
			.call [ cache.get ]
			.call [ cache.get ]
			
		Assert.assertEquals(1, fetches)
	}
	
	@Async def fetchUser(Promise<User> promise) {
		fetches++
		// if(cache != null) throw new Exception('error!')
		promise.set(new User('emre', 24))
	}
	
}

@Data
package class User {
	String name
	int age
} 
