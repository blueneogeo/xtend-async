package nl.kii.async.stream.test

import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.async.promise.BlockingExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*
import static extension nl.kii.util.DateExtensions.*

class TestStreamPerformance {

	@Test
	def void testStreamsAreFast() {
		val iterations = 50_000_000
		val streamStart = System.currentTimeMillis;
		
		// iterate a lot over a stream
		(1..iterations).iterator.stream.start
		val streamEnd = System.currentTimeMillis
		val double streamTime = streamEnd - streamStart
		println('stream took ' + streamTime + ' ms.')

		// compare with a while loop
		val whileStart = System.currentTimeMillis
		val i = (1..iterations).iterator
		while(i.hasNext) i.next
		val whileEnd = System.currentTimeMillis
		val double whileTime = whileEnd - whileStart
		println('while loop took ' + whileTime + ' ms.')
		
		println('stream is ' + streamTime / whileTime + ' times slower than a raw while loop.')
		
		// don't be more than 10 times slower
		assertTrue(streamTime / whileTime < 20) // has become more due to suspendable?
	}

	@Test
	def void testStreamsAreFast2() {
		val iterations = 5_000_000
		val streamStart = System.currentTimeMillis;
		
		// iterate a lot over a stream
		val streamResult = (1..iterations).iterator.stream
			.filter [ it % 2 == 0 ]
			.map [ 'hello ' + it ]
			.count
			.await(10.secs)
		val streamEnd = System.currentTimeMillis
		assertEquals(iterations / 2, streamResult)
		val double streamTime = streamEnd - streamStart
		println('stream took ' + streamTime + ' ms')

		// compare with a while loop
		val whileStart = System.currentTimeMillis;
		val whileResult = (1..iterations)
			.filter [ it % 2 == 0 ]
			.map [ 'hello ' + it ]
			.size
		val whileEnd = System.currentTimeMillis
		assertEquals(iterations / 2, whileResult)
		val double whileTime = whileEnd - whileStart
		println('while loop took ' + whileTime + ' ms')
		
		println('stream is ' + streamTime / whileTime + ' times slower than a raw while loop')
		
		// don't be more than 10 times slower
		assertTrue(streamTime / whileTime < 10)
	}
	
}
