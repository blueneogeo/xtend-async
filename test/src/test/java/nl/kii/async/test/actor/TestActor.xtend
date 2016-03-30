package nl.kii.async.test.actor

import nl.kii.async.annotation.Async
import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncDefault
import nl.kii.async.options.ThreadSafeAsyncOptions
import org.junit.Before
import org.junit.Test

import static java.util.concurrent.Executors.*
import static nl.kii.async.options.AsyncDefault.*
import static org.junit.Assert.*

import static extension nl.kii.act.ActorExtensions.*
import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class TestActor {
	
	@Before
	def void setup() {
		AsyncDefault.options = new ThreadSafeAsyncOptions(0, true, 1000, 'input', 50)
	}
	
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

	def unsynced() { incSyncedCounter }
	synchronized def synced() { incSyncedCounter }

	/**
	 * Test of actor versus function calls, method calls and synchronized method calls,
	 * under a single threaded load.
	 * <p>
	 * Synchronized calls seem to be about twice as slow as functions and unsynced methods.
	 * Actors are about 3x as slow as synchronized methods.
	 */
	@Test
	def void testActorRelativeSingleThreadedPerformance() {
		val iterations = 1..10_000_000
		val funct = [ incFunctCounter ]
		val actor = actor [ incActorCounter ]

		// warm up
		for(i : 1..20_000_000) actor.apply(i)
		
		val functTimeMs = measure [ for(i : iterations) funct.apply(i) ]
		println('function took: ' + functTimeMs)

		val unsyncedTimeMs = measure [ for(i : iterations) unsynced	]
		println('unsynced method took: ' + unsyncedTimeMs)

		val syncedTimeMs = measure [ for(i : iterations) synced	]
		println('synced method took: ' + syncedTimeMs)

		val actorTimeMs = measure [	for(i : iterations) actor.apply(i) ]
		println('actor took: ' + actorTimeMs)
	}

	/**
	 * Test of actor versus function calls, method calls and synchronized method calls,
	 * under a multithreaded load.
	 * <p>
	 * Synchronized calls seem to be about twice as slow as functions and unsynced methods.
	 * Actors are about 3x as slow as synchronized methods.
	 * <p>
	 * Interestingly this is about the same as under singlethreaded load.
	 */
	@Test
	def void testActorRelativeMultiThreadedPerformance() {
		val funct = [ incFunctCounter ]
		val actor = actor [ incActorCounter ]

		for(i : 1..20_000_000) actor.apply(i) // warmup

		val iterations = 1..1_000_000
		val threads = 10

		complete
			.call [ measure(threads) [ for(i : iterations) funct.apply(i) ] ]
			.then [ println('function took: ' + it) ]
			.call [ measure(threads) [ for(i : iterations) unsynced ] ]
			.then [ println('unsynced method took: ' + it) ]
			.call [ measure(threads) [ for(i : iterations) synced ] ]
			.then [ println('synced method took: ' + it) ]
			.call [ measure(threads) [ for(i : iterations) actor.apply(i) ] ]
			.then [ println('actor took: ' + it) ]
			.asFuture
			.get
	}

	/** 
	 * List processing vs Stream processing.
	 * <p>
	 * In this example, in small lists, the difference is performance is about a factor 2.
	 * <p>
	 * When the lists grow in size and are constantly pushed, it seems the GC can't keep 
	 * up and the difference grows to a factor 100 for lists of 100_000 items (processed a 1000 times).
	 * <p>
	 * In normal use cases the stream/list processing will not be the heaviest operation, but this
	 * does mean there is room for optimisation.
	 */
	@Test
	def void testStreamRelativeSingleThreadedPerformance() {
		val iterations = 1000
		val listSize = 1000
		val list = (1..listSize)
		val funct = [ list.map [ it + 1000].filter [ it % 2 == 0 ].forEach [ incFunctCounter ] ]
		val actor = actor [ list.stream.map [ it + 1000].filter [ it % 2 == 0 ].effect [ incActorCounter ].start ]

		// warm up
		for(i : 1..100) actor.apply(i)
		
		val functTimeMs = measure [ for(i : 1..iterations) funct.apply(i) ]
		println('function took: ' + functTimeMs)

		val streamTimeMs = measure [ for(i : 1..iterations) actor.apply(i) ]
		println('stream took: ' + streamTimeMs)
	}

	/** measure the duration of an action */
	def long measure(=>void actionFn) {
		val start = System.currentTimeMillis
		actionFn.apply
		val end = System.currentTimeMillis
		end - start
	}

	/** measure the duration of an action executed on multiple threads at once */
	@Async def measure(int threads, =>void actionFn) {
		val pool = newFixedThreadPool(threads)
		val start = System.currentTimeMillis;
		(1..threads)
			.stream
			// .effect [ println('starting thread ' + it) ]
			.map [ pool.task [| actionFn.apply ] ]
			.resolve(threads)
			.collect
			.first
			.map [ System.currentTimeMillis - start ]
	}
	
}
