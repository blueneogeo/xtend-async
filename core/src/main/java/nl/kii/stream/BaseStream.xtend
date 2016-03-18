package nl.kii.stream

import java.util.Queue
import nl.kii.act.NonBlockingAsyncActor
import nl.kii.async.UncaughtAsyncException
import nl.kii.async.annotation.Atomic
import nl.kii.stream.message.Close
import nl.kii.stream.message.Closed
import nl.kii.stream.message.Entries
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Error
import nl.kii.stream.message.Finish
import nl.kii.stream.message.Next
import nl.kii.stream.message.Overflow
import nl.kii.stream.message.Pause
import nl.kii.stream.message.Resume
import nl.kii.stream.message.Skip
import nl.kii.stream.message.StreamEvent
import nl.kii.stream.message.StreamMessage
import nl.kii.stream.message.Value
import nl.kii.stream.options.StreamOptions
import org.eclipse.xtend.lib.annotations.Accessors

abstract class BaseStream<I, O> extends NonBlockingAsyncActor<StreamMessage> implements IStream<I, O> {

	@Accessors(PUBLIC_GETTER) val protected StreamOptions options
	val protected Queue<Entry<I, O>> queue // queue for incoming values, for buffering when there is no ready listener

	@Atomic val int buffersize = 0 // keeps track of the size of the stream buffer
	@Atomic val boolean open = true // whether the stream is open
	@Atomic val boolean ready = false // whether the listener is ready
	@Atomic val boolean skipping = false // whether the stream is skipping incoming values to the finish
	@Atomic val boolean paused = false // whether the stream is paused

	@Atomic val (Entry<I, O>)=>void entryListener // listener for entries from the stream queue
	@Atomic val (StreamEvent)=>void notificationListener // listener for notifications give by this stream

	/** 
	 * Create the stream with your own provided queue. 
	 * Note: the queue must be threadsafe for streams to be threadsafe!
	 */
	
	new(StreamOptions options) {
		super(options.newActorQueue)
		this.queue = options.newStreamQueue
		this.options = options.copy
	}
	
	/** Get the queue of the stream. will only be an unmodifiable view of the queue. */
	override getQueue() { queue.unmodifiableView }

	override isOpen() { getOpen }
	
	override isReady() { getReady }
	
	override isSkipping() { getSkipping }
	
	override isPaused() { getPaused }
	
	override getBufferSize() { buffersize }
	
	override isBufferFull() { bufferSize >= options.maxQueueSize }
	
	// CONTROL THE STREAM /////////////////////////////////////////////////////

	/** Ask for the next value in the buffer to be delivered to the change listener */
	override next() { apply(new Next) }
	
	/** Tell the stream to stop sending values until the next Finish(0) */
	override skip() { apply(new Skip) }
	
	/** Close the stream, which will stop the listener from recieving values */
	override close() { apply(new Close) }
	
	override pause() { apply(new Pause) }
	
	override resume() { apply(new Resume) }
	
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
	override =>void onNotify((StreamEvent)=>void notificationListener) {
		this.notificationListener = notificationListener
		return [| this.notificationListener = null ]
	}
	
	// STREAM INPUT PROCESSING ////////////////////////////////////////////////

	/** Process a single incoming stream message from the actor queue. */
	override act(StreamMessage entry, =>void done) {
		if(isOpen) {
			switch entry {
				Value<I, O>, Finish<I, O>, Error<I, O>: {
					// check for buffer overflow or paused
					if(buffersize + 1 > options.maxQueueSize || isPaused) {
						notify(new Overflow(entry))
						done.apply
						return
					}
					// add the entry to the queue
					queue.add(entry)
					incBuffersize
					publishNext
				}
				Entries<I, O>: {
					// check for buffer overflow or paused
					if(buffersize + entry.entries.size >= options.maxQueueSize || isPaused) {
						for(e : entry.entries) {
							notify(new Overflow(e))
						}
						done.apply
						return
					}
					// add the entries to the queue 
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
							Finish<I, O> case level==0: { skipping = false }
							default: { queue.poll decBuffersize }
						}
					}
					// if we are still skipping, notify the parent stream it needs to skip
					if(isSkipping) notify(entry)			
				}
				Pause: {
					notify(entry)
					paused = true
				}
				Resume: {
					paused = false
					// notify that we resumed
					notify(entry)
					publishNext
				}
				Close: {
					// and publish the closed command downwards
					queue.add(new Closed)
					incBuffersize
					paused = false
					publishNext
					notify(entry)
					open = false
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
		if(!isOpen || !isReady || entryListener == null || queue.empty) return false
		// ok, lets get publishing
		ready = false
		// get the next entry from the queue
		val entry = queue.poll
		decBuffersize
		try {
			// check for some exceptional cases
			switch it: entry {
				// a finish of level 0 stops the skipping
				Finish<I, O>: if(level == 0) skipping = false
			}
			// publish the value on the entryListener
			entryListener.apply(entry)
			// something was published
			true
		} catch (UncaughtAsyncException e) {
			// this error is meant to break the publishing loop
			throw e
		} catch (Throwable t) {
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

	/** helper function for informing the notify listener */
	def protected notify(StreamEvent command) {
		notificationListener?.apply(command)
	}
	
	override toString() '''Stream { open: «isOpen», ready: «isReady», skipping: «isSkipping», queue: «queue.size», hasListener: «entryListener != null», options: «options» }'''
	
}
