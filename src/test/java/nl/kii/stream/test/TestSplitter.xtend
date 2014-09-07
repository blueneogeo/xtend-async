package nl.kii.stream.test

import nl.kii.async.annotation.Atomic
import org.junit.Test

import static org.junit.Assert.*

import static extension nl.kii.stream.StreamExtensions.*

class TestSplitter {
	
	@Atomic val boolean did1
	@Atomic val boolean did2
	
	@Test
	def void testCopySplitter() {
		val source = int.stream
		val s1 = int.stream
		val s2 = int.stream
		source.split
			.pipe(s1)
			.pipe(s2)
		s1.on [ each [ did1 = true ] ]
		s2.on [ each [ did2 = true ] ]

		// data does not stream automatically
		source << 1
		assertFalse(did1)
		assertFalse(did2)

		// all streams must be ready, so one stream of the two will not stream anything 
		s1.next
		assertFalse(did1)
		assertFalse(did2)

		// now both streams are ready, so now both should get a single value
		s2.next
		assertTrue(did1)
		assertTrue(did2)
		
		did1 = false
		did2 = false
		
		// stream something after the listeners are set
		source << 2
		assertFalse(did1)
		assertFalse(did2)

		// all streams must be ready, so one stream of the two will not stream anything 
		s1.next
		assertFalse(did1)
		assertFalse(did2)

		// now both streams are ready, so now both should get a single value
		s2.next
		assertTrue(did1)
		assertTrue(did2)
	}
	
	@Test
	def void testBalancer() {
		
		val source = int.stream
		val s1 = int.stream
		val s2 = int.stream
		source.balance
			.pipe(s1)
			.pipe(s2)
		s1.on [ each [ did1 = true ] ]
		s2.on [ each [ did2 = true ] ]

		did1 = false
		did2 = false
		
		source << 1 << 2 << 3 << finish
		assertFalse(did1)
		assertFalse(did2)
		
		s1.next		
		assertTrue(did1)
		assertFalse(did2)
		
		s2.next		
		assertTrue(did1)
		assertTrue(did2)

		did1 = false
		did2 = false
		
		s2.next		
		assertFalse(did1)
		assertTrue(did2)

		s2.next

	}
	
}




