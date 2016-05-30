package nl.kii.async.publish

import nl.kii.async.stream.Stream
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Hot

/**
 * A Publisher acts like a stream that you can subscribe to.
 * Unlike a stream, a publisher supports multiple subscribers.
 * Also unlike a stream, a publisher does not support backpressure.
 * A user subscribes by calling the subscribe() method and listening
 * to the returned stream. The user unsubscribes by closing the stream. 
 */
interface Publisher<T> {

	def void publish(T value)

	def void publish(Throwable error)

	@Hot @NoBackpressure
	def Stream<T, T> subscribe()

	def void start()
	
	def void stop()
	
	def boolean isPublishing()

	def int getSubscriptionCount()

	def void closeSubscriptions()
	
}
