package nl.kii.stream.test
import static extension org.junit.Assert.*
import nl.kii.stream.CopySplitter
import org.junit.Test

import static extension nl.kii.stream.StreamExtensions.*
import nl.kii.async.annotation.Atomic

class TestSplitter {
	
	@Atomic val boolean did1
	@Atomic val boolean did2
	
	@Test
	def void testCopySplitter() {
		val source = int.stream
		val splitter = new CopySplitter(source)
		val s1 = splitter.stream
		val s2 = splitter.stream
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
	
}
