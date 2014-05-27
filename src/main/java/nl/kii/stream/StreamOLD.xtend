package nl.kii.stream

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static nl.kii.util.SynchronizeExt.*

/**
 * <h1>what is a stream</h1>
 * 
 * A stream can receive values, and then transmit these values
 * to its listeners. To push a value into a stream, use .push().
 * To listen to values, call .each().
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
class StreamOLD<T> implements Procedure1<Entry<T>> {

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
	val _readyForNext = new AtomicBoolean(false)
	
	/** If true, all values will be discarded upto the next incoming finish */
	val _skipping = new AtomicBoolean(false)

	/** If true, all values will be discarded upto the next incoming finish */
	val _open = new AtomicBoolean(false)
	
	/** Lets others listen when the stream is asked skip to the finish */
	var =>void onSkip

	/** Called when this stream is out of data */
	var =>void onReadyForNext
	
	/** Lets others listen when the stream is closed */
	var =>void onClose

	/** Lets others listen for values in the stream */
	var (T)=>void onValue
	
	/** Lets others listen for errors occurring in the onValue listener */
	var (Throwable)=>void onError

	/** Lets others listen for the stream finishing a batch */
	var =>void onFinish

	// NEW /////////////////////////////////////////////////////////////////////

	/** Creates a new Stream. */
	new() {
		this(null)
	}

	/** Creates a new Stream that is connected to a parentStream. */
	new(Stream<?> parentStream) {
		this(parentStream) [| 
			new ConcurrentLinkedQueue
		]
	}
	
	/** Most detailed constructor, where you can specify your own queue factory. */
	new(Stream<?> parentStream, =>Queue<Entry<T>> queueFn) { 
		this.queueFn = queueFn
		// set up some default parent child relationships
		if(parentStream != null) {
			// default messaging up the chain
			this.onReadyForNext [| 
				parentStream.next
			]
			this.onSkip [| 
				parentStream.skip
			]
			this.onClose [|
				parentStream.close
			]
			// default messaging down the chain
			parentStream.onNextFinish [| 
				finish
				parentStream.next
			]
			parentStream.onNextError [ 
				error(it)
				parentStream.next
			]
		}
		this._open.set(true) // auto opens
	}
	
	// GETTERS & SETTERS ///////////////////////////////////////////////////////

	protected def setReadyForNext(boolean isReady) {
		_readyForNext.set(isReady)
	}

	package def isReadyForNext() {
		_readyForNext.get
	}
	
	protected def setSkipping(boolean isSkipping) {
		_skipping.set(isSkipping)
	}
	
	package def isSkipping() {
		_skipping.get
	}
	
	package def getQueue() {
		queue
	}
	
	package def isOpen() {
		_open.get
	}
	
	// PUSH ///////////////////////////////////////////////////////////////////

	/** Push a value into the stream, and publishes it to the listeners */
	def push(T value) {
		synchronize(this) [
			if(value == null) throw new NullPointerException('cannot stream a null value')
			apply(new Value(value))
			publish
			this
		]
	}

	/**
	 * Finish a batch of data that was pushed into the stream. Note that a finish may be called
	 * more than once, indicating that multiple batches were passed. Ends any skipping.
	 */	
	def finish() {
		synchronize(this) [
			apply(new Finish)
			publish
			this
		]
	}

	/**
	 * Report an error to the stream. It is also pushed down substreams as a message,
	 * so you can listen for errors at any point below where the error is generated
	 * in a stream chain.
	 */
	def error(Throwable t) {
		synchronize(this) [
			apply(new Error(t))
			publish
			null
		]
	}

	/** Queue a stream entry */
	override apply(Entry<T> entry) {
		synchronize(this) [
			if(entry == null) throw new NullPointerException('cannot stream a null entry')
			if(!_open.get) throw new StreamException('cannot apply an entry to a closed stream')
			if(queue == null) queue = queueFn.apply
			switch entry {
				Finish<T>: if(skipping) skipping = false
			}
			queue.add(entry)
		]
	}
		
	// CONTROL ////////////////////////////////////////////////////////////////

	/** 
	 * Discards incoming values until the stream receives a Finish. Then unskip and
	 * resume normal operation.
	 */
	package def void skip() {
		synchronize(this) [
			if(skipping) return null 
			else skipping = true		
			// discard everything up to finish from the queue
			while (
				skipping &&
				switch queue.peek {
					Value<T>: true
					Error<T>: true
					Finish<T>: false
					case null: false
				}
			) {
				queue.poll
			}
			// if we ended with a finish, we done skipping
			if(queue.peek instanceof Finish<?>)
				skipping = false
			// if we're not done skipping, escalate to the parent stream
			if(skipping && onSkip != null) 
				onSkip.apply
			null
		]
	}

	/** Indicate that the listener is ready for the next value */	
	package def void next() {
		synchronize(this) [
			readyForNext = true
			publish
			null
		]
	}
	
	/**
	 * Closes a stream and stops it being possible to push new entries to the stream or have 
	 * the stream have any output. In most cases it is unnecessary to close a stream. However
	 * some 
	 */
	def close() {
		synchronize(this) [
			_open.set(false)
			if(onClose != null)
				onClose.apply
			null
		]
	}
	
	// LISTEN /////////////////////////////////////////////////////////////////
	
	/** 
	 * Listen for values coming down the stream.
	 * Requires next to be called to get a value. 
	 */
	package def void onNextValue((T)=>void listener) {
		this.onValue = listener
	}
	
	/**
	 * Listen for a finish call being done on the stream. For each finish call,
	 * the passed listener will be called with this stream.
	 */	
	package def onNextFinish(=>void listener) {
		this.onFinish = listener
		this
	}
	
	/**
	 * Listen for errors in the stream or parent streams. 
	 * The stream only catches errors if catchErrors is set to true.
	 * Automatically moves the stream forward.
	 */
	package def onNextError((Throwable)=>void listener) {
		this.onError = listener
		this
	}

	/** 
	 * Let a parent stream listen when this stream skips to the finish. 
	 * Only supports a single listener.
	 */
	package def onSkip(=>void listener) {
		this.onSkip = listener
		this
	}
	
	/** 
	 * Let a parent stream listen when this stream is asked for the next value. 
	 * Only supports a single listener.
	 */
	package def onReadyForNext(=>void listener) {
		this.onReadyForNext = listener
		this
	}

	/** 
	 * Let a parent stream listen when this stream is asked for the next value. 
	 * Only supports a single listener.
	 */
	def onClose(=>void listener) {
		this.onClose = listener
		this
	}
	
	// OTHER //////////////////////////////////////////////////////////////////

	/** Try to publish the next value from the queue or the parent stream */
	package def void publish() {
		synchronize(this) [
			if(!readyForNext || !open) return null
			if(queue != null && !queue.empty) {
				// if there is something in the queue, publish it
				publish(queue.poll)
			} else if(onReadyForNext != null) {
				// otherwise, ask the parent stream for the next value
				onReadyForNext.apply
			}
			null
		]
	}

	/** 
	 * Send an entry directly (no queue) to the listeners
	 * (onValue, onError, onFinish). If a value was processed,
	 * ready is set to false again, since the value was published.
	 * @return true if a value was published
	 */
	protected def boolean publish(Entry<T> entry) {
		synchronize(this) [
			switch it : entry {
				Value<T>: {
					if(onValue == null || !readyForNext || skipping)
						return false
					var applied = false
					try {
						readyForNext = false
						onValue.apply(value)
						applied = true
					} catch(Exception e) {
						if(onError != null) 
							onError.apply(e)
						else throw(e)
					}
					applied
				}
				Finish<T>: {
					if(isSkipping) 
						skipping = false
					if(onFinish != null) 
						onFinish.apply
					false
				}
				Error<T>: {
					if(onError != null) 
						onError.apply(error)
					false
				}
			}
		]
	}
	
	override toString() '''«this.class.name» { 
			queue size: «IF(queue != null) » «queue.length» «ELSE» none «ENDIF»
			open: «isOpen»
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
