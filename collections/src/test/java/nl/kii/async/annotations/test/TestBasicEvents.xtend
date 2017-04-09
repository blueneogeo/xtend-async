package nl.kii.async.annotations.test

import nl.kii.async.event.Event
import org.junit.Assert
import org.junit.Test

class TestBasicEvents {
	
	String greeting1
	String greeting2
	
	@Test
	def void testEvent() {
	
		val greeter = new Greeter

		// two listeners to a new greeting
		
		greeter.onNewGreeting [ greeting | 
			println('listener 1 got greeting ' + greeting)
			greeting1 = greeting
		]

		greeter.onNewGreeting [ greeting | 
			println('listener 2 got greeting ' + greeting)
			greeting2 = greeting
		]
		
		// let the greeter do the greeting
		
		greeter.doSalutations
		
		// both listeners should have recieved the greeting
		
		Assert.assertEquals('hello my friends', greeting1)
		Assert.assertEquals('hello my friends', greeting2)
	}
	
}

class Greeter {
	
	@Event String newGreeting
	
	def doSalutations() {
		newGreeting('hello my friends')
	}
	
}

