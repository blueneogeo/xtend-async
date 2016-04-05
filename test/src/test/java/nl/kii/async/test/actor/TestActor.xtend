package nl.kii.async.test.actor

import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncDefault
import nl.kii.async.options.ThreadSafeAsyncOptions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.async.options.AsyncDefault.*
import static org.junit.Assert.*

import static extension nl.kii.act.ActorExtensions.*
import static extension nl.kii.async.ExecutorExtensions.*

class TestActor {
	
	@Before
	def void setup() {
		AsyncDefault.options = new ThreadSafeAsyncOptions(0, true, 1000, 'input', 50)
	}
	
	@Ignore
	@Test
	def void testHelloWorld() {
		val greeter = actor [ it, done |
			println('hello ' + it)
			done.apply
		]
		'world' >> greeter
		'Christian!' >> greeter
		'time to go!' >> greeter
	}

	@Atomic int access
	@Atomic int value
	@Atomic int multipleThreadAccessViolation
	
	@Test
	def void testActorsAreSingleThreaded() {
		// create an actor that does some simple counting
		val actor = actor [ it, done |
			// check that only a single thread has access
			val a = incAccess
			if(a > 1) incMultipleThreadAccessViolation
			value = value + 1
			decAccess
			done.apply
		]
		// give the actor a lot of parallel work to do
		val threads = newCachedThreadPool
		threads.task [ for(i : 1..1000) actor.apply(i) ]
		threads.task [ for(i : 1..1000) actor.apply(i) ]
		threads.task [ for(i : 1..1000) actor.apply(i) ]
		threads.task [ for(i : 1..1000) actor.apply(i) ]
		// wait a bit for the work to complete
		Thread.sleep(2000)
		// test all went well
		assertEquals(0, multipleThreadAccessViolation)
		assertEquals(4000, value)
	}
		
	@Atomic int actorCounter = 0
	@Atomic int functCounter = 0
	@Atomic int unsyncedCounter = 0
	@Atomic int syncedCounter = 0
	
}
