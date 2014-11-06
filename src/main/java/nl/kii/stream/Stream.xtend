package nl.kii.stream

import java.util.Collection
import java.util.Queue
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import nl.kii.observe.Observable
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

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
interface IStream<I, O> extends Procedure1<StreamMessage>, Observable<Entry<I, O>> {

	// PUSH DATA IN ///////////////////////////////////////////////////////////

	override apply(StreamMessage message)

	def void push(I value) 
	def void error(Throwable error)
	def void finish()
	def void finish(int level)

	// CONTROL ////////////////////////////////////////////////////////////////

	def void next()
	def void skip()
	def void close()
	
	// LISTEN /////////////////////////////////////////////////////////////////
	
	override =>void onChange((Entry<I, O>)=>void observeFn)
	def =>void onNotify((StreamEvent)=>void notificationListener)

	// STATUS /////////////////////////////////////////////////////////////////
	
	def IStream<I, I> getInput()
	
	def boolean isOpen()
	def boolean isReady()
	def boolean isSkipping()
	
	def Integer getConcurrency()
	def void setConcurrency(Integer concurrency)

	def int getBufferSize()
	def Collection<Entry<I, O>> getQueue()

	def void setOperation(String operationName)
	def String getOperation()
	
}

class Stream<T> extends BaseStream<T, T> {

	override getInput() { this }

	/** Queue a value on the stream for pushing to the listener */
	override push(T value) { apply(new Value(value, value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	override error(Throwable error) { apply(new Error(null, error)) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	override finish() { apply(new Finish(null, 0)) }	

	/** Tell the stream a batch of the given level has finished. */
	override finish(int level) { apply(new Finish(null, level)) }	
 	
}

class SubStream<I, O> extends BaseStream<I, O> {

	val protected IStream<I, I> input

	new (IStream<I, ?> parent) {
		this.input = parent.input
		this.concurrency = parent.concurrency
	}

	new (IStream<I, ?> parent, int maxSize) {
		super(maxSize)
		this.input = parent.input
	}

	override getInput() { input }

	// APPLYING VALUES TO THE ROOT STREAM

	/** Queue a value on the stream for pushing to the listener */
	override push(I value) { input.push(value) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	override error(Throwable t) { input.error(t) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	override finish() { input.finish }	

	/** Tell the stream a batch of the given level has finished. */
	override finish(int level) { input.finish(level) }	

	// APPLYING PAIRS TO THE SUBSTREAM

	/** Queue a value on the stream for pushing to the listener */
	def push(I from, O value) { apply(new Value(from, value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	def error(I from, Throwable error) { apply(new Error(from, error)) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	def finish(I from) { apply(new Finish(from, 0)) }	

	/** Tell the stream a batch of the given level has finished. */
	def finish(I from, int level) { apply(new Finish(from, level)) }	
	
}

abstract class BaseStream<I, O> extends Actor<StreamMessage> implements IStream<I, O> {

	val public static DEFAULT_MAX_BUFFERSIZE = 1000 // default max size for the queue

	val protected Queue<Entry<I, O>> queue // queue for incoming values, for buffering when there is no ready listener

	@Atomic val int buffersize = 0 // keeps track of the size of the stream buffer
	@Atomic val boolean open = true // whether the stream is open
	@Atomic val boolean ready = false // whether the listener is ready
	@Atomic val boolean skipping = false // whether the stream is skipping incoming values to the finish

	@Atomic val (Entry<I, O>)=>void entryListener // listener for entries from the stream queue
	@Atomic val (StreamEvent)=>void notificationListener // listener for notifications give by this stream

	@Atomic public val int concurrency = 0 // the default concurrency to use for async processing
	@Atomic public val int maxBufferSize // the maximum size of the queue
	@Atomic public val String operation // name of the operation the listener is performing

	/** Create the stream with a memory concurrent queue */
	new() { this(newConcurrentLinkedQueue, DEFAULT_MAX_BUFFERSIZE) }

	new(int maxBufferSize) { this(newConcurrentLinkedQueue, maxBufferSize) }

	/** 
	 * Create the stream with your own provided queue. 
	 * Note: the queue must be threadsafe for streams to be threadsafe!
	 */
	new(Queue<Entry<I, O>> queue, int maxBufferSize) { 
		this.queue = queue
		this.maxBufferSize = maxBufferSize
	}
	
	/** Get the queue of the stream. will only be an unmodifiable view of the queue. */
	override getQueue() { queue.unmodifiableView	}

	override isOpen() { getOpen }
	
	override isReady() { getReady }
	
	override isSkipping() { getSkipping }
	
	override getBufferSize() { buffersize }
	
	// CONTROL THE STREAM /////////////////////////////////////////////////////

	/** Ask for the next value in the buffer to be delivered to the change listener */
	override next() { apply(new Next) }
	
	/** Tell the stream to stop sending values until the next Finish(0) */
	override skip() { apply(new Skip) }
	
	/** Close the stream, which will stop the listener from recieving values */
	override close() { apply(new Close) }
	
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
	override protected act(StreamMessage entry, =>void done) {
		if(isOpen) {
			switch entry {
				Value<I, O>, Finish<I, O>, Error<I, O>: {
					// check for buffer overflow
					if(buffersize >= maxBufferSize) {
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
							Finish<I, O> case level==0: skipping = false
							default: { queue.poll decBuffersize }
						}
					}
					// if we are still skipping, notify the parent stream it needs to skip
					if(isSkipping) notify(entry)			
				}
				Close: {
					// and publish the closed command downwards
					queue.add(new Closed)
					incBuffersize
					publishNext
					notify(entry)
					setOpen = false
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
		} catch (UncaughtStreamException e) {
			// this error is meant to break the publishing loop
			throw e
		} catch (Throwable t) {
			// if we were already processing an error, throw and exit. do not re-wrap existing stream exceptions
			if(entry instanceof Error<?, ?>) {
				switch t {
					StreamException: throw t
					default: throw new StreamException(operation, entry, t)
				}
			}
			// check if next was called by the handler
			val nextCalled = isReady
			// make the stream ready again
			ready = true
			// otherwise push the error on the stream
			switch entry {
				Value<I, O>: apply(new Error(entry.from, new StreamException(operation, entry, t)))
				Error<I, O>: apply(new Error(entry.from, new StreamException(operation, entry, t)))
			}
			// if next was not called, it would halt the stream, so call it now
			if(!nextCalled) this.next
			// ok, done, nothing was published
			false
		}
	}

	/** helper function for informing the notify listener */
	def protected notify(StreamEvent command) {
		if(notificationListener != null)
			notificationListener.apply(command)
	}
	
	override toString() '''Stream { operation: «operation», open: «isOpen», ready: «isReady», skipping: «isSkipping», queue: «queue.size», hasListener: «entryListener != null» }'''
	
}
