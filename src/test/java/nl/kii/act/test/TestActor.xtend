package nl.kii.act.test
import static extension nl.kii.promise.PromiseExtensions.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import nl.kii.act.Actor
import org.junit.Test

import static java.util.concurrent.Executors.*
import static org.junit.Assert.*

import static extension nl.kii.act.ActorExtensions.*
import static extension nl.kii.async.ExecutorExtensions.*

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
			threads.task [|
				Thread.sleep(5)
				if(y <= 0) doneCounter.incrementAndGet
				else {
					y >> decrease.get // recursion
				}
			]
		]
		decrease.set(actor [ int value |
			threads.task [|
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
		// a simple actor that just adds all incoming numbers
		val actor = new Actor<Integer> {
			public var counter = 0
			override protected act(Integer message, ()=>void done) {
				counter += message
				done.apply
			}
		}
		// start 10 tasks in parallel, each pushing in 100000 messages
		val threads = newCachedThreadPool;
		(1..10)
			.map [ threads.task [| for(i : 1..100_000) { actor << 1 } ] ]
			.all
			.onError [ fail(message) ]
			.then [ println('done') ]
		Thread.sleep(500)
		assertEquals(1_000_000, actor.counter)
	}
	
}