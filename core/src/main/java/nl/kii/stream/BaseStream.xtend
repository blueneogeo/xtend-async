package nl.kii.stream

import java.util.Queue
import nl.kii.act.NonBlockingAsyncActor
import nl.kii.async.AsyncException
import nl.kii.async.annotation.Atomic
import nl.kii.async.options.AsyncOptions
import nl.kii.stream.message.Entries
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.StreamEvent
import nl.kii.stream.message.StreamMessage
import nl.kii.stream.message.Value
import org.eclipse.xtend.lib.annotations.Accessors

import static nl.kii.stream.message.StreamEvent.*

import static extension nl.kii.async.util.AsyncUtils.*
import nl.kii.stream.message.Closed

abstract class BaseStream<I, O> extends NonBlockingAsyncActor<StreamMessage> implements IStream<I, O> {

	@Accessors(PUBLIC_GETTER) val protected AsyncOptions options
	val protected Queue<Entry<I, O>> queue // queue for incoming values, for buffering when there is no ready listener

	@Atomic val int buffersize = 0 // keeps track of the size of the stream buffer
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
		this.queue = options.newStreamQueue
		this.options = options.copy
	}
	
	/** Get the queue of the stream. will only be an unmodifiable view of the queue. */
	override getQueue() { queue.unmodifiableView }

	override isOpen() { getOpen }
	
	override isReady() { getReady }
	
	override isPaused() { getPaused }
	
	override getBufferSize() { buffersize }
	
	override isBufferFull() { bufferSize >= options.maxQueueSize }
	
	// CONTROL THE STREAM /////////////////////////////////////////////////////

	/** Ask for the next value in the buffer to be delivered to the change listener */
	override next() { apply(StreamEvent.next) }
	
	/** Close the stream, which will close parent streams and stop the listener from recieving values */
	override close() {
		apply(new Closed) // travels down to listeners
		apply(StreamEvent.close) // travels up, closing parent streams
	}
	
	override pause() { apply(StreamEvent.pause) }
	
	override resume() { apply(StreamEvent.resume) }
	
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
		if(isOpen) {
			switch entry {
				Value<I, O>, Error<I, O>, Closed<I, O>: {
					// check for buffer overflow or paused
					if(buffersize + 1 > options.maxQueueSize || isPaused) {
						eventListener?.apply(StreamEvent.overflow)
						done.apply
						return
					}
					// add the entry to the queue
					queue.add(entry)
					incBuffersize
					// publish the entry
					publishNext
					// and if it was a finish, close the stream
					if(entry instanceof Closed<?, ?>) {
						this.open = false
					}
				}
				Entries<I, O>: {
					// check for buffer overflow or paused
					if(buffersize + entry.entries.size >= options.maxQueueSize || isPaused) {
						for(e : entry.entries) {
							eventListener?.apply(StreamEvent.overflow)
						}
						done.apply
						return
					}
					// add the entries to the queue 
					queue.addAll(entry.entries)
					incBuffersize(entry.entries.size)
					publishNext
				}
				StreamEvent case next: {
					ready = true
					if(!isPaused) {
						// try to publish the next from the queue
						val published = publishNext
						// if nothing was published, notify there parent stream we need a next entry
						if(!published) eventListener?.apply(entry)
					}
				}
				StreamEvent case overflow: {
				}
				StreamEvent case pause: {
					paused = true
					eventListener?.apply(entry)
				}
				StreamEvent case resume: {
					paused = false
					publishNext
				}
				StreamEvent case close: {
					paused = false
					open = false
					eventListener?.apply(entry)
				}
			}
		} else {
			queue.clear
			buffersize = 0
		}
		done.apply
	}
	
	// STREAM PUBLISHING //////////////////////////////////////////////////////
	
	/** Publish a single entry from the stream queue. */
	def protected boolean publishNext() {
		// should we publish at all?
		if(!isOpen || isPaused || !isReady || entryListener == null || queue.empty) return false
		// ok, lets get publishing
		ready = false
		// get the next entry from the queue
		val entry = queue.poll
		decBuffersize
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
			val nextCalled = isReady
			// make the stream ready again
			ready = true
			// otherwise push the error on the stream
			switch entry {
				Value<I, O>, Error<I, O>: apply(new Error(entry.from, t))
			}
			// if next was not called, it would halt the stream, so call it now
			if(!nextCalled) this.next
			// ok, done, nothing was published
			false
		}
	}

	override toString() '''Stream { open: «isOpen», ready: «isReady», queue: «queue.size», hasListener: «entryListener != null», options: «options» }'''

	
}
