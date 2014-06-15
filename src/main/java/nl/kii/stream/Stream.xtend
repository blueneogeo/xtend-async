package nl.kii.stream

import java.util.Queue
import nl.kii.act.Actor

import static com.google.common.collect.Queues.*

/**
 * A sequence of elements supporting sequential and parallel aggregate operations.
 * <p>
 * It has the following features:
 * <ul>
 * <li>threadsafe
 * <li>all operations (including extensions) are non-blocking
 * <li>supports asynchronous processing through flow control (next)
 * <li>it is queued. you can optionally provide your own queue
 * <li>only allows a single listener (use a StreamObserver to listen with multiple listeners)
 * <li>supports aggregation through batches (data is separated through finish entries)
 * <li>supports multiple levels of aggregation through multiple batch/finish levels
 * <li>wraps errors and lets you listen for them at the end of the stream chain
 * </ul>
 */
class Stream<T> extends Actor<StreamMessage> implements Observable<Entry<T>> {

	var open = true
	var listenerReady = false
	var skipping = false

	val Queue<Entry<T>> queue
	var (Entry<T>)=>void entryListener
	var (StreamCommand)=>void notifyListener

	new() { this(newConcurrentLinkedQueue) }

	/** create the stream with your own provided queue. Note: the queue must be threadsafe! */
	new(Queue<Entry<T>> queue) {
		this.queue = queue
	}
	
	// CONTROL THE STREAM /////////////////////////////////////////////////////

	/** ask for the next value in the buffer to be delivered to the change listener */
	def next() { apply(new Next) }
	
	/** tell the stream to stop sending values until the next Finish(0) */
	def skip() { apply(new Skip) }
	
	/** close the stream, which will stop the listener from recieving values */
	def close() { apply(new Close) }
	
	/** queue a value on the stream for pushing to the listener */
	def push(T value) { apply(new Value(value)) }
	
	/** 
	 * tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	def error(Throwable error) { apply(new Error(error)) }
	
	/**
	 * tell the stream the current batch of data is finished. The same as finish(0).
	 */
	def finish() { apply(new Finish(0)) }	

	/** tell the stream a batch of the given level has finished. */
	def finish(int level) { apply(new Finish(level)) }	
	
	/** get the queue of the stream. will only be an unmodifiable view of the queue. */
	def getQueue() { queue.unmodifiableView	}

	// LISTENERS //////////////////////////////////////////////////////////////
	
	/** 
	 * listen for changes on the stream. There can only be a single listener.
	 * this is used mostly internally, and you are encouraged to use the StreamExtensions
	 * instead. If you need more than one listener, use a StreamObserver by calling stream.observe.
	 */
	override synchronized def =>void onChange((Entry<T>)=>void entryListener) {
		this.entryListener = entryListener
		return [| this.entryListener = null ]
	}
	
	/**
	 * listen for notifications from the stream. This is used mostly when chaining streams
	 * together and allows streams to inform eachother on actions taken.
	 */
	synchronized def void onNotification((StreamCommand)=>void notifyListener) {
		this.notifyListener = notifyListener
	}

	// STREAM INPUT PROCESSING ////////////////////////////////////////////////

	/**
	 * Process next incoming entry.
	 * Since the stream extends Actor, there is no more than one thread active.
	 */
	override protected act(StreamMessage entry, =>void done) {
		switch entry {
			Value<T>, Finish<T>, Error<T>: {
				queue.add(entry)
				publishNext
			}
			Entries<T>: {
				entry.entries.forEach [
					queue.add(it)
				]
				publishNext
			}
			Next: {
				listenerReady = true
				// try to publish the next from the queue
				val published = publishNext
				// if nothing was published, notify there parent stream we need a next entry
				if(!published) notify(entry)			
			}
			Skip: {
				if(skipping) return 
				else skipping = true		
				// discard everything up to finish from the queue
				while(skipping && !queue.empty) {
					switch it: queue.peek {
						Finish<T> case level==0: skipping = false
						default: queue.poll
					}
				}
				// if we are still skipping, notify the parent stream it needs to skip
				if(skipping) notify(entry)			
			}
			Close: {
				publishNext
				open = false
				notify(entry)
			}
		}
		done.apply
	}
	
	// STREAM PUBLISHING //////////////////////////////////////////////////////
	
	/** take an entry from the queue and pass it to the listener */
	def protected boolean publishNext() {
		if(listenerReady && entryListener != null && !queue.empty) {
			listenerReady = false
			val entry = queue.poll
			if(entry instanceof Finish<?>)
				if(entry.level == 0)
					skipping = false
			try {
				entryListener.apply(entry)
				true
			} catch (Throwable t) {
				if(entry instanceof Error<?>) throw t
				listenerReady = true
				apply(new Error(t))
				false
			}
		} else false
	}

	/** helper function for informing the notify listener */
	def protected notify(StreamCommand notification) {
		if(notifyListener != null)
			notifyListener.apply(notification)
	}
	
}
