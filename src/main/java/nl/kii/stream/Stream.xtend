package nl.kii.stream

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * <h1>what is a stream</h1>
 * 
 * A stream can receive values, and then transmit these values
 * to its listeners. To push a value into a stream, use .push().
 * To listen to values, call .each().
 * 
 * <h1>non-buffered</h1>
 * 
 * This basic stream implementation has no buffering. This means that
 * when you create it and push messages in, and then add a listener,
 * those first messages will have been missed by the listener. To
 * add flow control and buffering, @see(BufferedStream)
 * 
 * <h1>what a stream passes</h1>
 * 
 * A Stream is a publisher of three kinds of entries:
 * <li>a value
 * <li>a finish of a batch
 * <li>an error
 * 
 * <h1>finishing a batch</h1>
 * 
 * After pushing some values, you can finish that batch of values by 
 * by calling .finish(). This marks the end of a batch of values that 
 * you have inputted and is used as a signal to process the batch.
 * <p>
 * For example, the StreamExt.collect() extension uses it to know when
 * a bunch of values have to be collected and transformed into a list.
 * <p>
 * You can repeat this process multiple times on a stream. In the case
 * of collect(), this results in multiple lists being generated in
 * the resulting stream.
 * <p>
 * To just get the first value from a stream, call .then() or .first().
 * 
 * <h1>catching listener errors</h1>
 * 
 * If you have multiple asynchronous processes happening, it can be
 * practical to catch any errors thrown at the end of a stream chain,
 * and not inside the listeners. If you enable catchErrors, the stream
 * will catch any errors occurring in listeners and will instead pass
 * them to a listener that you can pass by calling onError().
 * 
 * <h1>extensions</h1>
 * 
 * The stream class only supports a basic publishing interface.
 * You can add extra functionality by importing extensions:
 * <li>StreamExt
 * <li>StreamPairExt
 * <p>
 * Or creating your own extensions.
 */
class Stream<T> implements Procedure1<Entry<T>> {

	/** 
	 * The queue is lazily constructed using this function.
	 */	
	val =>Queue<Entry<T>> queueFn

	/** 
	 * The queue gets filled when there are entries entering the stream
	 * even though there are no listeners yet. The queue will only grow
	 * upto the maxQueueSize. If more entries enter than the size, the
	 * queue will overflow and discard these later entries.
	 */
	var Queue<Entry<T>> queue
	
	/** If true, the value listener is ready for a next value */
	val _ready = new AtomicBoolean(false)
	
	/** If true, all values will be discarded upto the next incoming finish */
	val _skipping = new AtomicBoolean(false)
	
	/** Lets others listen when the stream starts skipping */
	var (Void)=>void onSkip
	
	/** Lets others listen for values in the stream */
	var (T)=>void onValue
	
	/** Lets others listen for errors occurring in the onValue listener */
	var (Throwable)=>void onError

	/** Lets others listen for the stream finishing a batch */
	var (Void)=>void onFinish
	
	// NEW /////////////////////////////////////////////////////////////////////

	/** Creates a new Stream. */
	new() {
		this(null)
	}

	/** Creates a new Stream that is connected to a parentStream. */
	new(Stream<?> parentStream) {
		this(parentStream) [| new ConcurrentLinkedQueue ]
	}
	
	/** Most detailed constructor, where you can specify your own queue factory. */
	new(Stream<?> parentStream, =>Queue<Entry<T>> queueFn) { 
		this.queueFn = queueFn
		// set up some default parent child relationships
		if(parentStream != null) {
			// default messaging up the chain
			onSkip [ parentStream.skip ]
			// default messaging down the chain
			parentStream.onFinish [ finish ]
			parentStream.onError [ error(it) ]
		}
	}
	
	
	
	// GETTERS & SETTERS ///////////////////////////////////////////////////////

	def isReady() {
		_ready.get
	}
	
	def isSkipping() {
		_skipping.get
	}
	
	def getQueue() {
		queue
	}
	
	// PUSH ///////////////////////////////////////////////////////////////////

	/** Push a value into the stream. */
	def push(T value) {
		if(value == null) throw new NullPointerException('cannot stream a null value')
		apply(new Value(value))
		this
	}

	/**
	 * Report an error to the stream. It is also pushed down substreams as a message,
	 * so you can listen for errors at any point below where the error is generated
	 * in a stream chain.
	 */
	def error(Throwable t) {
		apply(new Error(t))
	}

	/**
	 * Finish a batch of data that was pushed into the stream. Note that a finish may be called
	 * more than once, indicating that multiple batches were passed.
	 */	
	def finish() {
		apply(new Finish)
		this
	}
		
	/** Add an entry to the stream. If there is no ready listener, it will queue the value. */
	override apply(Entry<T> entry) {
		if(entry == null) throw new NullPointerException('cannot stream a null value')
		if((queue == null || queue.empty) && ready) {
			// if possible, skip the queue and publish the entry directly
			publish(entry)
		} else {
			// otherwise, queue it
			queue(entry)
			publishFromQueue
		}
	}
	
	
		
	// CONTROL ////////////////////////////////////////////////////////////////

	/** 
	 * Skip incoming values until the stream receives a Finish. Then unskip and
	 * resume normal operation.
	 */	
	def skip() {
		if(!skipping) {
			_skipping.set(true)
			// clearQueueUntil [ it instanceof Finish<?> ]
			if(onSkip != null) onSkip.apply(null)
			publishFromQueue
		}
	}

	/** Indicate that the listener is ready for the next value */	
	def next() {
		_ready.set(true)
		publishFromQueue
	}
	
	

	// LISTEN /////////////////////////////////////////////////////////////////
	
	/** Listen for values on the stream. Will accept incoming values directly. */
	def void forEach((T)=>void onValue) {
		this.onValue = [ onValue.apply(it); next ]
		next
	}

	/** 
	 * Listen for values from the stream, but call next and skip to control the stream.
	 * <p>
	 * Use the passed stream to control the flow:
	 * <li>call stream.next() to tell the stream to send the next message when available,
	 * <li>call stream.skip() to tell the stream to stop sending until the next finish.
	 */
	def void forNext((T, Stream<T>)=>void onValue) {
		this.onValue = [ onValue.apply(it, this) ] 
	}
		
	/**
	 * Listen for a finish call being done on the stream. For each finish call,
	 * the passed listener will be called with this stream.
	 */	
	def onFinish(Procedure1<Void> listener) {
		this.onFinish = listener
		this
	}
	
	/**
	 * Listen for errors in the stream or parent streams. 
	 * The stream only catches errors if catchErrors is set to true.
	 */
	def onError(Procedure1<Throwable> listener) {
		this.onError = listener
		this
	}
	
	/** 
	 * Let a parent stream listen when this stream skips to the finish. 
	 * Only supports a single listener.
	 */
	def onSkip(Procedure1<Void> listener) {
		this.onSkip = listener
		this
	}
	
	// OTHER //////////////////////////////////////////////////////////////////

	/** Put a value on the queue. Creates the queue if necessary */
	protected def queue(Entry<T> value) {
		if(queue == null) queue = queueFn.apply
		queue.add(value)	
	}
	
	/** If there is anything on the queue, and while the listener is ready, push it out */
	protected def boolean publishFromQueue() {
		if(queue != null && !queue.empty) {
			while(ready) publish(queue.poll)
			true
		} else false
	}
	
	/** Send an entry directly (no queue) to the listeners (onValue, onError, onFinish) */
	protected def boolean publish(Entry<T> it) {
		switch it {
			Value<T>: {
				var applied = false
				if(onValue != null && ready && !isSkipping) {
					_ready.set(false)
					if(onError == null) { 
						onValue.apply(value)
						applied = true
					} else try {
						onValue.apply(value)
						applied = true
					} catch(Exception e) {
						error(e)
					}
				}
				if(ready) next
				applied
			}
			Finish<T>: {
				if(isSkipping) _skipping.set(false)
				if(onFinish != null) onFinish.apply(null)
				false
			}
			Error<T>: {
				if(onError != null) onError.apply(error)
				false
			}
		}
	}
	
	override toString() '''«this.class.name» { 
			queue: «IF(queue != null) » «queue.length» «ELSE» none «ENDIF»
		}
	'''
	
}



// STREAM EXCEPTION ///////////////////////////////////////////////////////////

class StreamException extends Exception {
	new(String msg) { super(msg) }
	new(Throwable e) { super(e) }
	new(String msg, Throwable e) { super(msg + ': ' + e.message, e) }
}



// STREAM ENTRY ///////////////////////////////////////////////////////////////

interface Entry<T> {
}

class Value<T> implements Entry<T> {
	public val T value
	new(T value) { this.value = value }
	override toString() { value.toString }
	override equals(Object o) { o instanceof Value<?> && (o as Value<?>).value == this.value }
}

class Finish<T> implements Entry<T> {
	override toString() { 'finish' }
	override equals(Object o) { o instanceof Finish<?> }
}

class Error<T> implements Entry<T> {
	public val Throwable error
	new(Throwable error) { this.error = error }
	override toString() { 'error: ' + error.message }
}
