package nl.kii.stream.test

import nl.kii.stream.CopySplitter
import org.junit.Test

import static extension nl.kii.stream.StreamExtensions.*

class TestSplitter {
	
	@Test
	def void testCopySplitter() {
		val source = int.stream
		val splitter = new CopySplitter(source)
		val s1 = splitter.stream
		val s2 = splitter.stream
		
		s1.on [
			each [ println('s1: ' + it) s1.next ]
		]

		s2.on [
			each [ println('s2: ' + it) s2.next ]
		]

		
		s1.next
		s2.next
//
		source << 1 << 2
//		
//		s1.next
//		s2.next
//		
//		source << 3
//
//		s1.next
//		s2.next

//		s1.onEach [ println('s1: ' + it) ]
//		s2.onEach [ println('s2: ' + it) ]
	}
	
}
