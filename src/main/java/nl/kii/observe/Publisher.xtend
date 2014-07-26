package nl.kii.observe

import java.util.List
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * A threadsafe distributor of events to its registered listeners.
 * <p>
 * A Publisher is more lightweight than a stream with a streamobserver.
 * It does not have any flow control or async support, and has only 
 * a single queue. Contrary to a stream, it allows for multiple
 * subscriptions, and each subscription can be unsubscribed by calling
 * the returned method.
 * <p>
 * For it to work correctly, the listeners should be non-blocking. 
 */
class Publisher<T> extends Actor<T> implements Observable<T> {
	
	@Atomic public val boolean publishing = true
	@Atomic transient val List<Procedure1<T>> observers 

	/** Listen for publications from the publisher */
	synchronized override =>void onChange((T)=>void observeFn) {
		if(observers == null) observers = newLinkedList(observeFn) 
		else observers.add(observeFn)
		return [| observers.remove(observeFn) ]
	}
	
	override act(T message, =>void done) {
		if(observers != null && publishing) {
			for(observer : observers) {
				observer.apply(message)
			}
		} 
		done.apply
	}
	
	override toString() '''Publisher { publishing: «publishing», observers: «observers.size», inbox: «inbox.size» } '''
	
}
