package nl.kii.observe

import com.google.common.collect.Queues
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.stream.StreamExtensions.*

class TestPublisher {
	
	@Test
	def void testPublishAndObserve() {
		val collector = String.stream
		val publisher = new Publisher<String>(Queues.newSynchronousQueue, true, 10)
		
		// register two listeners
		publisher.onChange [ '1:' + it >> collector ]
		val stop2 = publisher.onChange [ '2:' + it >> collector ]
		
		// A show be collected from both listeners
		publisher.apply('A')
		collector.finish
		
		// now stop listener 2, and then only listener 1 should collect B
		stop2.apply
		publisher.apply('B')
		collector.finish

		collector.finish(1) // higher level finish, so we can double-collect
		collector.collect.collect.then [
			assertEquals(#[
				#['1:A', '2:A'], // first result from both listeners
				#['1:B'] // second result from only listener 1, since 2 stopped
			], it)
		]
	}
	
}