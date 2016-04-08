package nl.kii.async.benchmark

import nl.kii.async.annotation.Async

import static java.util.concurrent.Executors.*

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class MeasureUtils {
	

	/** measure the duration of an action */
	def static long measure(=>void actionFn) {
		val start = System.currentTimeMillis
		actionFn.apply
		val end = System.currentTimeMillis
		end - start
	}

	/** measure the duration of an action executed on multiple threads at once */
	@Async def static measure(int threads, =>void actionFn) {
		val pool = newFixedThreadPool(threads)
		val start = System.currentTimeMillis;
		(1..threads)
			.stream
			// .effect [ println('starting thread ' + it) ]
			.map [ pool.task [| actionFn.apply ] ]
			.resolve(threads)
			.collect
			.map [ System.currentTimeMillis - start ]
	}

}