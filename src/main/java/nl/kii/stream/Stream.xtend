package nl.kii.stream

import java.util.Queue
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Observable

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

	val Queue<Entry<T>> queue

	@Atomic val boolean open = true
	@Atomic val boolean ready = false
	@Atomic val boolean skipping = false

	@Atomic val (Entry<T>)=>void entryListener
	@Atomic val (StreamNotification)=>void notifyListener

	/** create the stream with a memory concurrent queue and the given initial values */
	new(T... initalValues) {
		this(newConcurrentLinkedQueue)
		initalValues.forEach [ push ]
	}

	/** create the stream with a memory concurrent queue */
	new() { this(newConcurrentLinkedQueue) }

	/** create the stream with your own provided queue. Note: the queue must be threadsafe! */
	new(Queue<Entry<T>> queue) { this.queue = queue }
	
	/** get the queue of the stream. will only be an unmodifiable view of the queue. */
	def getQueue() { queue.unmodifiableView	}

	def isOpen() { getOpen }
	
	def isReady() { getReady }
	
	def isSkipping() { getSkipping }

	// CONTROL THE STREAM /////////////////////////////////////////////////////

	/** Ask for the next value in the buffer to be delivered to the change listener */
	def next() { apply(new Next) }
	
	/** Tell the stream to stop sending values until the next Finish(0) */
	def skip() { apply(new Skip) }
	
	/** Close the stream, which will stop the listener from recieving values */
	def close() { apply(new Close) }
	
	/** Queue a value on the stream for pushing to the listener */
	def push(T value) { apply(new Value(value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	def error(Throwable error) { apply(new Error(error)) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	def finish() { apply(new Finish(0)) }	

	/** Tell the stream a batch of the given level has finished. */
	def finish(int level) { apply(new Finish(level)) }	
	
	// LISTENERS //////////////////////////////////////////////////////////////
	
	/** 
	 * Listen for changes on the stream. There can only be a single change listener.
	 * this is used mostly internally, and you are encouraged to use the StreamExtensions
	 * instead. If you need more than one listener, use a StreamObserver by calling StreamExtensions.observe.
	 * The entryListener must be non blocking.
	 */
	override =>void onChange((Entry<T>)=>void entryListener) {
		this.entryListener = entryListener
		return [| this.entryListener = null ]
	}
	
	/**
	 * Listen for notifications from the stream. This is used mostly when chaining streams
	 * together and allows streams to inform eachother on actions taken.
	 */
	def void onNotification((StreamNotification)=>void notifyListener) {
		this.notifyListener = notifyListener
	}

	// STREAM INPUT PROCESSING ////////////////////////////////////////////////

	/**
	 * Process next incoming entry.
	 * Since the stream extends Actor, there is no more than one thread active.
	 */
	override protected act(StreamMessage entry, =>void done) {
		if(isOpen) {
			switch entry {
				Value<T>, Finish<T>, Error<T>: {
					queue.add(entry)
					publishNext
				}
				Entries<T>: {
					queue.addAll(entry.entries)
					publishNext
				}
				Next: {
					ready = true
					// try to publish the next from the queue
					val published = publishNext
					// if nothing was published, notify there parent stream we need a next entry
					if(!published) notify(entry)			
				}
				Skip: {
					if(isSkipping) return 
					else skipping = true		
					// discard everything up to finish from the queue
					while(isSkipping && !queue.empty) {
						switch it: queue.peek {
							Finish<T> case level==0: skipping = false
							default: queue.poll
						}
					}
					// if we are still skipping, notify the parent stream it needs to skip
					if(isSkipping) notify(entry)			
				}
				Close: {
					// and publish the closed command downwards
					queue.add(new Closed)
					publishNext
					notify(entry)
					setOpen = false
				}
			}
		} else {
			queue.clear
		}
		done.apply
	}
	
	// STREAM PUBLISHING //////////////////////////////////////////////////////
	
	/** take an entry from the queue and pass it to the listener */
	def protected boolean publishNext() {
		if(isOpen && isReady && entryListener != null && !queue.empty) {
			ready = false
			// get the next entry from the queue
			val entry = queue.poll
			try {
				// check for some exceptional cases
				switch it: entry {
					// a finish of level 0 stops the skipping
					Finish<T>: if(level == 0) skipping = false
				}
				// publish the value on the entryListener
				entryListener.apply(entry)
				true
			} catch (Throwable t) {
				// if we were already processing an error, throw and exit
				if(entry instanceof Error<?>) throw new StreamException('error handler gave error ' + t.message + ' when handling', entry, t)
				// otherwise push the error on the stream
				ready = true
				apply(new Error(new StreamException(t.message + ', when handling', entry, t)))
				false
			}
		} else false
	}

	/** helper function for informing the notify listener */
	def protected notify(StreamNotification notification) {
		if(notifyListener != null)
			notifyListener.apply(notification)
	}
	
	override toString() '''Stream { open: «isOpen», ready: «isReady», skipping: «isSkipping», queue: «queue.size», hasListener: «entryListener != null» }'''
	
}

class StreamException extends Exception {
	
	public val Entry<?> entry
	
	new(String message, Entry<?> entry, Throwable cause) {
		super(message + ': ' + entry, cause)
		this.entry = entry
	}
	
}
