package nl.kii.observe

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * A threadsafe non-blocking distributor of events to its registered listeners.
 * <p>
 * A Publisher is more lightweight than a stream with a streamobserver.
 * It does not have any flow control or async support, and has only 
 * a single queue. Contrary to a stream, it allows for multiple
 * subscriptions, and each subscription can be unsubscribed by calling
 * the returned method.
 * <p>
 * For it to work correctly, the listeners should be non-blocking. 
 */
class Publisher<T> extends Actor<T> implements Procedure1<T>, Observable<T> {
	
	@Atomic public val boolean publishing = true
	@Atomic transient val List<Procedure1<T>> observers 

	new() { }
	
	new(boolean isPublishing) {
		publishing = isPublishing
	}

	/** Listen for publications from the publisher */
	override =>void onChange((T)=>void observeFn) {
		if(observers == null) observers = new CopyOnWriteArrayList 
		observers.add(observeFn)
		return [| observers.remove(observeFn) ]

		// notice that we constantly create new lists. combined with the observers
		// list being atomic, this guarantees that the list is fully thread-safe.
		// CopyOnWriteArrayList is not atomic so it cannot be used. It is also locking
		// while this solution does not lock. It is not that expensive to keep copying
		// the list since it is usually only contains 2 to 3 listeners.
//		if(observers == null) observers = newLinkedList(observeFn) 
//		else observers = Lists.newLinkedList(observers) => [ add(observeFn) ]
//		return [| observers = Lists.newLinkedList(observers) => [ remove(observeFn) ] ]
	}
	
	override act(T message, =>void done) {
		if(observers != null && publishing) {
			for(observer : observers) {
				observer.apply(message)
			}
		} 
		done.apply
	}
	
	def getSubscriptionCount() { if(observers != null) observers.size else 0 }
	
	override toString() '''Publisher { publishing: «publishing», observers: «observers.size», inbox: «inbox.size» } '''
	
}
