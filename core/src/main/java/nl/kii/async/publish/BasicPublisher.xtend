package nl.kii.async.publish

import java.util.Set
import nl.kii.async.stream.Controllable
import nl.kii.async.stream.Sink
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Hot
import co.paralleluniverse.fibers.Suspendable
import nl.kii.async.stream.Streams

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
	
	@Suspendable
	override publish(T value) {
		if(!publishing) return;
		for(subscription : subscriptions) {
			subscription.push(value)
		}
	}
	
	@Suspendable
	override publish(Throwable error) {
		if(!publishing) return;
		for(subscription : subscriptions) {
			subscription.push(error)
		}
	}
	
	@Hot @NoBackpressure
	override subscribe() {
		val stream = Streams.newSink
		stream.controllable = new Controllable {
			
			@Suspendable
			override next() {
				// basic publisher has no flow control support
			}
			
			@Suspendable
			override pause() {
				subscriptions.remove(stream)
			}
			
			@Suspendable
			override resume() {
				subscriptions.add(stream)
			}
			
			@Suspendable
			override close() {
				subscriptions.remove(stream)
			}
			
		}
		subscriptions.add(stream)
		stream
	}
	
	override getSubscriptionCount() {
		subscriptions.size
	}
	
	@Suspendable
	override closeSubscriptions() {
		for(subscription : subscriptions) {
			subscription.complete
			subscription.close
		}
	}
	
}
