package nl.kii.async.rx.test

import nl.kii.async.stream.Sink
import org.junit.Test
import rx.Observable

import static org.junit.Assert.*

import static extension nl.kii.async.promise.PromiseExtensions.*
import static extension nl.kii.async.rx.RXExtensions.*
import static extension nl.kii.async.stream.StreamExtensions.*

class TestRXExtensions {

	static var counter = 0
	
	@Test
	def void testRXRecursion() {
		val iterations = 1_000
		// create an observable
		val observable = Observable.from(1..iterations)
		// create a stream
		val sink = new Sink<Integer> {
			override onNext() {}
			override onClose() {}
		}
		// count the results from the stream
		val count = sink.count
		// now connect the observable to the stream, which will fulfill the count promise
		observable.pipe(sink)
		// now all the data from the observable should have been counted
		assertEquals(iterations, count.await as int)
	}
	
}
