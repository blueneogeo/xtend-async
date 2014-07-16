package nl.kii.async.annotation.test

import nl.kii.async.annotation.Atomic
import org.eclipse.xtend.lib.annotations.Data
import org.junit.Test

import static org.junit.Assert.*

class TestAtomicAnnotation {
	
	@Atomic int counter = 2
	@Atomic long longNumber
	@Atomic float price
	@Atomic Tester tester
	
	@Test
	def void testInteger() {
		assertEquals(2, counter = 3)
		assertEquals(4, incCounter)
		assertEquals(3, decCounter)
		assertEquals(5, incCounter(2))
	}
	
	@Test
	def void testLong() {
		longNumber = 45346465433435435L
		assertEquals(45346465433435436L, incLongNumber)
	}
	
	@Test
	def void testFloat() {
		price = 4.5
		assertEquals(4.5, price, 0)
	}
	
}

@Data class Tester {
	String name
}
