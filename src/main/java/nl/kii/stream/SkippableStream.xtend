package nl.kii.stream

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * A SkippableStream extends Stream with the possibility to skip to the end of
 * a batch of incoming data. In Java8 this feature is called shortcutting a stream.
 * <p>
 * This is useful for optimising streams. Say you have a million entries coming into
 * a stream but only need the top 3. With a normal stream, your listeners would get
 * all of the values. 
 * <p>
 * In a skippable stream, you not only get a value but also a done
 * function that you can call. By calling this function, the listener can indicate that
 * it no longer needs data. When all listeners have indicated they no longer have a need
 * for data, the listener will skip all data until a finish command is given/received.
 * <p>
 * After this, a new batch is started, and new values pushed to the stream will be sent
 * to the listeners again, until they call the done function again, etc.
 */
class SkippableStream<T> extends Stream<T> {
	
	/** 
	 * Amount of done listeners. Used to know if a stream is done. If there are
	 * no open listeners anymore, a stream can stop streaming until the next finish. 
	 */
	val skippingToFinish = new AtomicBoolean(false) 
	
	/** Times the stream was finished */
	val timesFinished = new AtomicInteger(0)
	
	/** How many of the listeners are done for this batch */
	val doneListenerCount = new AtomicInteger(0)
	
	/** Optional listener for when skipToFinish is called */
	var =>void skipListener

	// NEW /////////////////////////////////////////////////////////////////////

	new() {	super(null) }
	new(Stream<?> parentStream) { super(parentStream) }
	new(Publisher<Entry<T>> publisher, Stream<?> parentStream) { super(publisher, parentStream) }
	
	// PUSH ///////////////////////////////////////////////////////////////////
	
	/**
	 * Push an entry into the stream. An entry can be a Value, a Finish or an Error.
	 */
	override apply(Entry<T> entry) {
		switch entry {
			case null: throw new NullPointerException('cannot stream a null entry')
			Value<T>: if(!skippingToFinish.get) publisher.apply(entry)
			Finish<T>:  { 
				timesFinished.incrementAndGet
				skippingToFinish.set(false)
				doneListenerCount.set(0)
				publisher.apply(entry)
			}
			Error<T>: publisher.apply(entry)
		}
	}
	
	/** 
	 * Setting this to true will disregard all next incoming values, until we get a Finish entry/call,
	 * which will resume normal settings. It will also communicate this to the parentStream, if available.
	 * This allows the parent streams to stop streaming unnecessarily as well.
	 * 
	 * NOTE: there may be a problem here when pushing this up the chain when we process async commands.
	 * The parent streams may be further in processing than the substream...
	 */
	def Stream<T> skipToFinish() {
		skippingToFinish.set(true)
		if(skipListener != null) skipListener.apply
		this
	}
	
	// LISTEN /////////////////////////////////////////////////////////////////
	
	/**
	 * Call the listener when skipping. Useful for calling parentStreams when
	 * chaining streams together. That way, parent streams can also skip.
	 */
	def onSkip(=>void listener) {
		skipListener = listener
	}
	
	/**
	 * Listen to values coming from the stream. For each new value, the passed
	 * listerer will be called with the value.
	 */
	def each((T, =>void)=>void listener) {
		// we track timesCalled so we can compare to timesFinished. The reason
		// for this is that done can be called for every batch, and finish
		// resets the batch
		val timesCalled = new AtomicInteger(0)
		// create the onDone function that the listener can call to indicate
		// that it no longer needs any values for this batch
		val =>void onDone = [|
			// we can only call onDone as many times as we finished + 1
			if(timesCalled.get <= timesFinished.get + 1) {
				// now we can't call done anymore until the batch is finished
				timesCalled.incrementAndGet
				// one more listener is done for this batch
				val amountDone = doneListenerCount.incrementAndGet
				// if they are all done, we can safely skip all the next values of this batch
				if(amountDone == subscriptionCount)	skipToFinish
			}
		]
		// start listening
		onChange [
			// only process if we did not call done yet
			if(timesCalled.get <= timesFinished.get + 1) {
				switch it { Value<T>: listener.apply(value, onDone) }
			}
		]
		this
	}
		
}