package nl.kii.async.rx

import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.Observer
import nl.kii.async.annotation.Hot
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.stream.Sink
import nl.kii.async.stream.Stream
import rx.Observable
import rx.subjects.AsyncSubject

class RXExtensions {

	/**
	 * Very basic implementation of conversion of an xtend-async Stream to an rx.Observable.
	 * TODO: implement correct backpressure and async support
	 */
	@Hot @NoBackpressure 	
	def static <IN, OUT> Observable<OUT> toRXObservable(Stream<IN, OUT> stream) {
		val subject = AsyncSubject.create
		stream.observer = new Observer<IN, OUT> {
			
			override value(IN in, OUT value) {
				subject.onNext(value)
				stream.next
			}
			
			override error(IN in, Throwable t) {
				subject.onError(t)
				stream.next
			}
			
			override complete() {
				subject.onCompleted
				stream.close
			}
			
		}
		subject
	}
	
	/**
	 * Basic implementation of piping an rx.Observable to an xtend-async stream.
	 * TODO: implement correct backpressure and async support
	 */
	@Hot @NoBackpressure
	def static <OUT> void pipe(Observable<OUT> observable, Sink<OUT> sink) {
		val subscription = new AtomicReference
		subscription.set(observable.subscribe(
			[ sink.push(it) ],
			[ sink.push(it) ],
			[ sink.complete ]
		))
	}
	
}