package nl.kii.stream

import java.util.List
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import static extension nl.kii.stream.StreamExtensions.*

interface Observable<T> {
	
	/** 
	 * Observe changes on the observable.
	 * @return a function that can be called to stop observing
	 */
	def =>void onChange((T)=>void observeFn)
	
}

/** 
 * Transforms a stream into an observable.
 * Note that by doing so you lose flow control.
 */
class StreamObserver<T> implements Observable<T> {
	
	val observers = new AtomicReference<List<Procedure1<T>>>
	
	new(Stream<T> source) {
		source.onEach [ value |
			observers.get?.forEach [
				apply(value)
			]
		]
	}
	
	override =>void onChange((T)=>void observeFn) {
		if(observers.get == null) observers.set(newLinkedList(observeFn))
		else observers.get.add(observeFn)
		return [| observers.get.remove(observeFn) ]
	}
	
}