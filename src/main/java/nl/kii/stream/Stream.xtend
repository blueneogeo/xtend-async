package nl.kii.stream
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import java.util.Queue
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Observable

import static com.google.common.collect.Queues.*
import nl.kii.promise.Task

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
class Stream<T> extends Actor<StreamMessage> implements Procedure1<StreamMessage>, Observable<Entry<T>> {

	val static DEFAULT_MAX_BUFFERSIZE = 10000 // default max size for the queue

	val Queue<Entry<T>> queue // queue for incoming values, for buffering when there is no ready listener

	@Atomic val int buffersize = 0 // keeps track of the size of the stream buffer
	@Atomic val boolean open = true // whether the stream is open
	@Atomic val boolean ready = false // whether the listener is ready
	@Atomic val boolean skipping = false // whether the stream is skipping incoming values to the finish

	@Atomic val (Entry<T>)=>void entryListener // listener for entries from the stream queue
	@Atomic val (StreamNotification)=>void notificationListener // listener for notifications give by this stream

	@Atomic public val int maxBufferSize // the maximum size of the queue
	@Atomic public val String operation // name of the operation the listener is performing

	/** create the stream with a memory concurrent queue */
	new() { 
		this(newConcurrentLinkedQueue, DEFAULT_MAX_BUFFERSIZE)
	}

	new(int maxBufferSize) { 
		this(newConcurrentLinkedQueue, maxBufferSize)
	}

	/** create the stream with a memory concurrent queue and the given initial values */
	new(T... initalValues) {
		this(newConcurrentLinkedQueue, DEFAULT_MAX_BUFFERSIZE)
		initalValues.forEach [ push ]
	}

	/** create the stream with your own provided queue. Note: the queue must be threadsafe! */
	new(Queue<Entry<T>> queue, int maxBufferSize) { 
		this.queue = queue
		this.maxBufferSize = maxBufferSize
	}
	
	/** get the queue of the stream. will only be an unmodifiable view of the queue. */
	def getQueue() { queue.unmodifiableView	}

	def isOpen() { getOpen }
	
	def isReady() { getReady }
	
	def isSkipping() { getSkipping }
	
	def getBufferSize() { buffersize }
	
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
	 * <p>
	 * this is used mostly internally, and you are encouraged to use .observe() or
	 * the StreamExtensions instead.
	 * @return unsubscribe function
	 */
	override =>void onChange((Entry<T>)=>void entryListener) {
		this.entryListener = entryListener
		return [| this.entryListener = null ]
	}
	
	/** 
	 * Listen for notifications from the stream.
	 * <p>
	 * this is used mostly internally, and you are encouraged to use .monitor() or
	 * the StreamExtensions instead.
	 * @return unsubscribe function
	 */
	def =>void onNotify((StreamNotification)=>void notificationListener) {
		this.notificationListener = notificationListener
		return [| this.notificationListener = null ]
	}
	
	/**
	 * Observe the entries coming off this stream using a StreamObserver.
	 * Note that you can only have ONE stream observer for every stream!
	 * If you want more than one observer, you can split the stream.
	 * <p>
	 * If you are using Xtend, it is recommended to use the StreamExtensions.on [ ]
	 * instead, for a more concise and elegant builder syntax.
	 * <p>
	 * @return a Task that can be listened to for an error, or for completion if
	 * all values were processed (until finish or close).
	 * <p>
	 * @throws UncaughtStreamException if you have no onError listener(s) for the returned task.
	 * <p>
	 * Even if you process an error, the error is always exported. If the task has
	 * an error listener, the error is passed to that task. If the task has no
	 * error listener, then an UncaughtStreamException will be thrown.
	 * <p>
	 * To prevent errors from passing down the stream, you have to filter them. You can do this
	 * by using the StreamExtensions.onError[] extension (or write your own filter).
	 */
	def observe(StreamObserver<T> observer) {
		val task = new Task
		operation = 'observe'
		onChange [ entry |
			// println('performing ' + operation + ' on ' + entry)
			switch it : entry {
				Value<T>: observer.onValue(value)
				Finish<T>: { observer.onFinish(level) if(level==0) task.complete }
				Error<T>: {
					val escalate = observer.onError(error)
					if(escalate) {
						if(task.hasErrorHandler) task.error(new StreamException(operation, entry, error)) 
						else throw new UncaughtStreamException(operation, entry, error)
					}
				}
				Closed<T>: { observer.onClosed task.complete }
			}
		]
		task
	}
	
	/**
	 * Monitor commands given to this stream.
	 */
	def void monitor(StreamMonitor monitor) {
		onNotify [ notification |
			try {
				switch it : notification {
					Next: monitor.onNext
					Skip: monitor.onSkip
					Close: monitor.onClose
					Overflow: monitor.onOverflow(entry)
				}
			} catch(UncaughtStreamException t) {
				throw t // these should not be caught but escalated
			} catch(Exception t) {
				throw new StreamException(operation, null, t)
			}
		]
	}

	// STREAM INPUT PROCESSING ////////////////////////////////////////////////

	/**
	 * Process a single incoming stream message from the actor queue.
	 */
	override protected act(StreamMessage entry, =>void done) {
		if(isOpen) {
			switch entry {
				Value<T>, Finish<T>, Error<T>: {
					if(buffersize >= maxBufferSize) {
						notify(new Overflow(entry))
						return
					} 
					queue.add(entry)
					incBuffersize
					publishNext
				}
				Entries<T>: {
					queue.addAll(entry.entries)
					incBuffersize(entry.entries.size)
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
	
	/**
	 * Publish a single entry from the stream queue.
	 */
	def protected boolean publishNext() {
		if(isOpen && isReady && entryListener != null && !queue.empty) {
			ready = false
			// get the next entry from the queue
			val entry = queue.poll
			decBuffersize
			try {
				// check for some exceptional cases
				switch it: entry {
					// a finish of level 0 stops the skipping
					Finish<T>: if(level == 0) skipping = false
				}
				// publish the value on the entryListener
				entryListener.apply(entry)
				true
			} catch (UncaughtStreamException e) {
				// this error is meant to break the publishing loop
				throw e
			} catch (Throwable t) {
				// if we were already processing an error, throw and exit. do not re-wrap existing stream exceptions
				if(entry instanceof Error<?>) {
					switch t {
						StreamException: throw t
						default: throw new StreamException(operation, entry, t)
					}
				}
				// otherwise push the error on the stream
				ready = true
				apply(new Error(new StreamException(operation, entry, t)))
				false
			}
		} else false
	}

	/** helper function for informing the notify listener */
	def protected notify(StreamNotification command) {
		if(notificationListener != null)
			notificationListener.apply(command)
	}
	
	override toString() '''Stream { operation: «operation», open: «isOpen», ready: «isReady», skipping: «isSkipping», queue: «queue.size», hasListener: «entryListener != null» }'''
	
}
