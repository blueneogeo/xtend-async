package nl.kii.async.annotation.test

import nl.kii.async.annotation.Atomic
import org.eclipse.xtend.lib.annotations.Data
import org.junit.Test

import static org.junit.Assert.*

class TestAtomicAnnotation {
	
	@Atomic public int counter = 2
	@Atomic Long longNumber
	@Atomic float price
	@Atomic Tester tester = new Tester("Lucien")
	
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

	@Atomic int i = 0
	
	@Test
	def void testReference() {
		assertEquals('Lucien', tester.name)
		tester = new Tester('christian')
		val oldTester = (tester = new Tester('Floris'))
		assertEquals('christian', oldTester.name)
		assertEquals('Floris', tester.name)
		
		doSomething [| i = i + 1 ]
		println(i)
	}
	
	def doSomething(=>void closure) {
		closure.apply
	}
	
}

@Data class Tester {
	String name
}
