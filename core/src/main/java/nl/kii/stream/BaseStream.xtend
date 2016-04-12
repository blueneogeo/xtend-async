package nl.kii.stream

import java.util.Queue
import nl.kii.act.NonBlockingAsyncActor
import nl.kii.async.AsyncException
import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncOptions
import nl.kii.stream.message.Closed
import nl.kii.stream.message.Entries
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Overflow
import nl.kii.stream.message.StreamEvent
import nl.kii.stream.message.StreamEvents
import nl.kii.stream.message.StreamMessage
import nl.kii.stream.message.Value
import org.eclipse.xtend.lib.annotations.Accessors

import static extension nl.kii.async.util.AsyncUtils.*

abstract class BaseStream<I, O> extends NonBlockingAsyncActor<StreamMessage> implements IStream<I, O> {

	@Accessors(PUBLIC_GETTER) val protected AsyncOptions options
	@Atomic protected Queue<Entry<I, O>> entryQueue // queue for incoming values, for buffering when there is no ready listener

	@Atomic val int queueSize = 0 // keeps track of the size of the stream buffer
	@Atomic val boolean open = true // whether the stream is open
	@Atomic val boolean ready = false // whether the listener is ready
	@Atomic val boolean skipping = false // whether the stream is skipping incoming values to the finish
	@Atomic val boolean paused = false // whether the stream is paused

	@Atomic val (Entry<I, O>)=>void entryListener // listener for entries from the stream queue
	@Atomic val (StreamEvent)=>void eventListener // listener for events from this stream

	/** 
	 * Create the stream with your own provided queue. 
	 * Note: the queue must be threadsafe for streams to be threadsafe!
	 */
	
	new(AsyncOptions options) {
		super(options.newActorQueue, options.actorMaxCallDepth)
		this.options = options.copy
	}
	
	/** Get the queue of the stream. Will create it JIT. */
	override getQueue() { 
		if(entryQueue == null) entryQueue = options.newStreamQueue
		entryQueue
	}

	override isOpen() { getOpen }
	
	override isReady() { getReady }
	
	override isPaused() { getPaused }
	
	override isQueueFull() { queueSize >= options.maxQueueSize }
	
	// CONTROL THE STREAM /////////////////////////////////////////////////////

	/** Ask for the next value in the buffer to be delivered to the change listener */
	override next() { apply(StreamEvents.next) }
	
	/** 
	 * Close the stream, which will close parent streams and prevents new input.
	 * Queued entries may still be processed!
	 */
	override close() {
		apply(new Closed) // travels down to listeners
		apply(StreamEvents.close) // travels up, closing parent streams
	}
	
	override pause() { apply(StreamEvents.pause) }
	
	override resume() { apply(StreamEvents.resume) }
	
	// LISTENERS //////////////////////////////////////////////////////////////
	
	/** 
	 * Listen for changes on the stream. There can only be a single change listener.
	 * <p>
	 * this is used mostly internally, and you are encouraged to use .observe() or
	 * the StreamExtensions instead.
	 * @return unsubscribe function
	 */
	override =>void onChange((Entry<I, O>)=>void entryListener) {
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
	override =>void onEvent((StreamEvent)=>void notificationListener) {
		this.eventListener = notificationListener
		return [| this.eventListener = null ]
	}
	
	// STREAM INPUT PROCESSING ////////////////////////////////////////////////

	/** Process a single incoming stream message from the actor queue. */
	override act(StreamMessage entry, =>void done) {
		switch entry {
			Value<I, O>, Error<I, O>, Closed<I, O>: {
				if(isOpen) {
					// check for buffer overflow or paused
					if(queueSize + 1 > options.maxQueueSize || isPaused) {
						eventListener?.apply(new Overflow(entry.from))
						done.apply
						return
					}
					// add the entry to the queue if necessary, otherwise execute immediately!
					// (we must check if there is a listener, to prevent pushing things out before
					// a stream chain had a chance to be set up)
//					if(false && queueSize == 0 && eventListener != null) {
//						// publish the entry immediately, bypassing the queue
//						publishNext(entry)
//					} else {
						// add it to the queue
						queue.add(entry)
						incQueueSize
						// publish the next entry
						publishNext(null)
//					}
					// and if it was a finish, close the stream
					if(entry instanceof Closed<?, ?>) {
						this.open = false
					}
				}
			}
			Entries<I, O>: {
				if(isOpen) {
					// check for buffer overflow or paused
					if(queueSize + entry.entries.size >= options.maxQueueSize || isPaused) {
						for(e : entry.entries) {
							eventListener?.apply(new Overflow(e.from))
						}
						done.apply
						return
					}
					// add the entries to the queue 
					queue.addAll(entry.entries)
					incQueueSize(entry.entries.size)
					publishNext(null)
				}
			}
			StreamEvents case StreamEvents.next: {
				ready = true
				if(!isPaused) {
					// try to publish the next from the queue
					val published = publishNext(null)
					// if nothing was published, notify there parent stream we need a next entry
					if(!published) eventListener?.apply(entry)
				}
			}
			Overflow<I>: {
			}
			StreamEvents case StreamEvents.pause: {
				paused = true
				eventListener?.apply(entry)
			}
			StreamEvents case StreamEvents.resume: {
				paused = false
				publishNext(null)
			}
			StreamEvents case StreamEvents.close: {
				paused = false
				open = false
				eventListener?.apply(entry)
			}
		}
		done.apply
	}
	
	// STREAM PUBLISHING //////////////////////////////////////////////////////
	
	/** Publish a single entry from the stream queue. */
	def protected boolean publishNext(Entry<I, O> nextEntry) {
		// are we allowed to publish? Note: we do not check for !open on purpose. 
		if(isPaused || !isReady || entryListener == null) return false
		// is there something to publish?
		if(nextEntry == null && queueSize == 0) return false
		// ok, lets get publishing
		ready = false
		// get the next entry to publish. either get the passed one or get one from the queue
		val entry = if(nextEntry != null) {
			nextEntry 
		} else {
			// get the next entry from the queue and update the queue size
			val polledEntry = queue.poll
			if(polledEntry == null) return false else decQueueSize
			polledEntry
		}
		try {
			// publish the value on the entryListener
			entryListener?.apply(entry)
			// something was published
			true
		} catch (AsyncException t) {
			// clean up the stacktrace
			t.cleanStackTrace
			// this error is meant to break the publishing loop
			throw t
		} catch (Throwable t) {
			// clean up the stacktrace
			t.cleanStackTrace
			// if we were already processing an error, throw and exit. do not re-wrap existing stream exceptions
			if(entry instanceof Error<?, ?>) throw t
			// check if next was called by the handler
			// val nextCalled = isReady
			// make the stream ready again
			ready = true
			// publish the throwable as an error to the entryListener
			entryListener?.apply(new Error(entry.from, t))
			// if next was not called, it would halt the stream, so call it now
			// if(!nextCalled) this.next
			// ok, done, nothing was published
			true
		}
	}

	override toString() '''Stream { open: «isOpen», ready: «isReady», queue: «queue.size», hasListener: «entryListener != null», options: «options» }'''

	
}
