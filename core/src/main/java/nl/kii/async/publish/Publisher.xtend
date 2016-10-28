package nl.kii.async.publish

import nl.kii.async.stream.Stream
import nl.kii.async.annotation.NoBackpressure
import nl.kii.async.annotation.Hot
import co.paralleluniverse.fibers.Suspendable

/**
 * A Publisher acts like a stream that you can subscribe to.
 * Unlike a stream, a publisher supports multiple subscribers.
 * Also unlike a stream, a publisher does not support backpressure.
 * A user subscribes by calling the subscribe() method and listening
 * to the returned stream. The user unsubscribes by closing the stream. 
 */
@Suspendable
interface Publisher<T> {

	/** Publish a single value to subscribers */
	def void publish(T value)

	/** Publish to subscribers that something went wrong */
	def void publish(Throwable error)

	/** 
	 * Add a subscriber to values and errors from the publisher.
	 * @return a stream that lets the subscriber respond to published values and 
	 * errors from the publisher. Pausing the stream makes the publisher skip
	 * the subscriber. Resuming resumes the values. Closing the stream unsubscribes.
	 */
	@Hot @NoBackpressure
	def Stream<T, T> subscribe()

	/** Start publishing. */
	def void start()
	
	/** Stop publishing. */
	def void stop()
	
	/** Lets you know if the publisher is started and not stopped. */
	def boolean isPublishing()

	/** The amount of subscribers to this publisher. */
	def int getSubscriptionCount()

	/** Unsubscribes all currently open subscriptions, closing all streams. */
	def void closeSubscriptions()
	
}
