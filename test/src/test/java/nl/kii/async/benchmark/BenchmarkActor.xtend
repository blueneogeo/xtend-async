package nl.kii.async.benchmark

import java.util.concurrent.TimeUnit
import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncDefault
import nl.kii.async.options.ThreadSafeAsyncOptions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static nl.kii.act.ActorExtensions.*
import static nl.kii.async.benchmark.MeasureUtils.*
import static nl.kii.async.options.AsyncDefault.*

import static extension nl.kii.promise.PromiseExtensions.*

@Ignore
class BenchmarkActor {
	
	@Before
	def void setup() {
		AsyncDefault.options = new ThreadSafeAsyncOptions(0, true, 1000, 'input', 50)
	}
	
	@Atomic int actorCounter = 0
	@Atomic int functCounter = 0
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
	def void benchmarkActorRelativeSingleThreadedPerformance() {
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
	def void benchmarkActorRelativeMultiThreadedPerformance() {
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
			.get(20, TimeUnit.SECONDS)
	}
	
}
