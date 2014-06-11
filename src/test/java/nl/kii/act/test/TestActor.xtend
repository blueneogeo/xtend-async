package nl.kii.act.test

import java.util.concurrent.atomic.AtomicReference
import org.junit.Test

import static nl.kii.stream.PromiseExtensions.*

import static extension nl.kii.act.ActorExtensions.*
import nl.kii.act.Actor
import static extension java.util.concurrent.Executors.*
import java.util.concurrent.atomic.AtomicInteger
import static extension org.junit.Assert.*

class TestActor {
	
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
	
	// bit of a constructed test to simulate two actors using async processes
	// calling eachother, hoping to see if deadlocks are now avoided.
	@Test
	def void testAsyncCrosscallingActors() {
		val doneCounter = new AtomicInteger(0)
		val decrease = new AtomicReference<Actor<Integer>>
		val threads = newCachedThreadPool
		val checkDone = actor [ int y |
			async(threads) [|
				Thread.sleep(5)
				if(y <= 0) doneCounter.incrementAndGet
				else {
					y >> decrease.get // recursion
				}
			]
		]
		decrease.set(actor [ int value |
			async(threads) [|
				(value - 1) >> checkDone
			]
		])
		checkDone << 100
		checkDone << 300
		checkDone << 200
		Thread.sleep(2000)
		assertEquals(3, doneCounter.get)
	}
	
	@Test
	def void testActorLoad() {
		val threads = newCachedThreadPool
		val counter = new AtomicInteger(0)
		val ref = new AtomicReference<Actor<Integer>> 
		val a = actor [ int i, done |
			async(threads) [|
				Thread.sleep(1)
				counter.incrementAndGet
				done.apply
			]
		]
		ref.set(a)
		for(i : 1..100000) {
			a << i
		}
		Thread.sleep(1000)
		assertEquals(100000, counter.get)
	}
	
}