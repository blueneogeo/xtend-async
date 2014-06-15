package nl.kii.observe

import java.util.List
import java.util.concurrent.atomic.AtomicReference
import nl.kii.act.Actor
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A threadsafe distributor of events to its registered listeners.
 * <p>
 * A Publisher is more lightweight than a stream with a streamobserver.
 * It does not have any flow control or async support, and has only 
 * a single queue.
 * <p>
 * For it to work correctly, the listeners should be non-blocking. 
 */
class Publisher<T> extends Actor<T> implements Observable<T> {
	
	val _publishing = new AtomicBoolean(true)
	val observers = new AtomicReference<List<Procedure1<T>>>

	synchronized override =>void onChange((T)=>void observeFn) {
		if(observers.get == null) observers.set(newLinkedList(observeFn))
		else observers.get.add(observeFn)
		return [| observers.get.remove(observeFn) ]
	}
	
	override protected act(T message, =>void done) {
		if(publishing) {
			for(observer : observers.get) {
				observer.apply(message)
			}
		} 
		done.apply
	}
	
	def isPublishing() { _publishing.get }
	
	def setPublishing(boolean value) { _publishing.set(value) }
	
}