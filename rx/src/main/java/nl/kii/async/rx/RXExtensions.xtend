package nl.kii.async.rx

import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Uncontrolled
import nl.kii.async.promise.Input
import nl.kii.async.promise.Promise
import nl.kii.async.stream.Controllable
import nl.kii.async.stream.Sink
import nl.kii.async.stream.Stream
import nl.kii.async.stream.Streams
import rx.Observable

class RXExtensions {
	
	/**
	 * Basic implementation of piping an rx.Observable to an xtend-async stream.
	 * It will only actually subscribe the sink to the observable the first time
	 * you ask next from the sink.
	 * TODO: implement correct backpressure and async support
	 */
	@Uncontrolled @NoBackpressure
	def static <S, OUT> void pipe(Observable<OUT> observable, Sink<OUT> sink) {
		val subscription = new AtomicReference

		sink.controllable = new Controllable {
			
			override next() {
				if(subscription.get == null) {
					subscription.set(observable.subscribe(
						[ sink.push(it) ],
						[ sink.push(it) ],
						[ sink.complete ]
					))
				}
				
			}
			
			override pause() {
				// not supported
			}
			
			override resume() {
				// not supported
			}
			
			override close() {
				subscription.get?.unsubscribe
			}
			
		}
	}

	/**
	 * Basic implementation of piping an rx.Observable to an xtend-async stream.
	 * It will only actually subscribe the sink to the observable the first time
	 * you ask next from the returned stream.
	 * TODO: implement correct backpressure and async support
	 */
	@Uncontrolled @NoBackpressure
	def static <OUT> Stream<?, OUT> stream(Observable<OUT> observable) {
		val sink = Streams.newSink
		observable.pipe(sink)
		sink
	}

	/** When the observable has a value, it sets it in the input. If there is an error, it sets it in the input as well. */	
	def static <OUT> void pipe(Observable<OUT> observable, Input<OUT> input) {
		val subscription = new AtomicReference
		subscription.set(observable.subscribe(
			[ input.set(it) ],
			[ input.error(it) ],
			[ ]
		))
	}

	/** Promise the first value from the observable, or the first error from the observable. */
	def static <OUT> Promise<?, OUT> promise(Observable<OUT> observable) {
		val input = new Input<OUT>
		observable.pipe(input)
		input
	}
	
}
