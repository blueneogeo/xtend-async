package nl.kii.async.benchmark

import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncDefault
import nl.kii.async.options.ThreadSafeAsyncOptions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static nl.kii.async.benchmark.MeasureUtils.*
import static nl.kii.async.options.AsyncDefault.*

import static extension nl.kii.stream.StreamExtensions.*

@Ignore
class BenchmarkStream {
	
	@Before
	def void setup() {
		AsyncDefault.options = new ThreadSafeAsyncOptions(0, true, 1000, 'input', 50)
		// AsyncDefault.options = new SingleThreadedAsyncOptions [ ]
	}
	
	@Atomic int actorCounter = 0
	@Atomic int functCounter = 0

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
	def void benchmarkStreamRelativeSingleThreadedPerformance() {
		val iterations = 1000
		val listSize = 1000
		val list = (1..listSize)
		val funct = [ list.map [ it + 1000].filter [ it % 2 == 0 ].forEach [ incFunctCounter ] ]
		val actor = [ list.stream.map [ it + 1000].filter [ it % 2 == 0 ].effect [ incActorCounter ].start ]

		// warm up
		for(i : 1..100) actor.apply(i)
		
		val functTimeMs = measure [ for(i : 1..iterations) funct.apply(i) ]
		println('function took: ' + functTimeMs)

		val streamTimeMs = measure [ for(i : 1..iterations) actor.apply(i) ]
		println('stream took: ' + streamTimeMs)
	}

	
}
