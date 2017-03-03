package nl.kii.async.publish

import java.util.Set
import nl.kii.async.annotation.Hot
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.stream.Controllable
import nl.kii.async.stream.Sink

/**
 * Simple but fully functional implementation of a publisher.
 * Automatically publishes directly, no flow control, not threadsafe.
 */
class BasicPublisher<T> implements Publisher<T> {
	
	var publishing = false
	val Set<Sink<T>> subscriptions = newHashSet
	
	override start() {
		publishing = true
	}
	
	override stop() {
		publishing = false
		closeSubscriptions
	}
	
	override isPublishing() {
		publishing
	}
	
	
	override publish(T value) {
		if(!publishing) return;
		for(subscription : subscriptions) {
			subscription.push(value)
		}
	}
	
	
	override publish(Throwable error) {
		if(!publishing) return;
		for(subscription : subscriptions) {
			subscription.push(error)
		}
	}
	
	@Hot @NoBackpressure
	override subscribe() {
		val source = new Sink<T> {
			// note: below handlers are implemented in the controllable
			override onNext() {}
			override onClose() { }
		}
		source.controllable = new Controllable {
			
			
			override next() {
				// basic publisher has no flow control support
			}
			
			
			override pause() {
				subscriptions.remove(source)
			}
			
			
			override resume() {
				subscriptions.add(source)
			}
			
			
			override close() {
				subscriptions.remove(source)
			}
			
		}
		subscriptions.add(source)
		source
	}
	
	override getSubscriptionCount() {
		subscriptions.size
	}
	
	
	override closeSubscriptions() {
		for(subscription : subscriptions) {
			subscription.complete
			subscription.close
		}
	}
	
}
