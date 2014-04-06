package nl.kii.stream

import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import nl.kii.stream.impl.ThreadSafePublisher
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
class Stream<T> implements Closeable, Publisher<Entry<T>> {
	
	/** Set these to set how all streams are set by default */
	public static var CATCH_ERRORS_DEFAULT = false
	
	/** 
	 * If true, errors will be caught and propagated as error entries. You can then
	 * listen for errors at the end of the stream (but for the start/each call). 
	 * If left false, exceptions will throw inside of the stream like a normal throw.
	 */
	public var catchErrors = CATCH_ERRORS_DEFAULT

	/** 
	 * The publisher registers listeners and calls the listeners when
	 * there are new entries for them. You register a listener by
	 * calling stream.each(...)
	 */
	protected val Publisher<Entry<T>> stream
	
	/** Basic streams start open */
	protected val isOpen = new AtomicBoolean(true)
	
	/** Optional handler to call when the stream is openend */
	protected (Void)=>void onOpen
	
	/** Optional handler to call when the stream is closed */
	protected (Void)=>void onClose
	

	// NEW /////////////////////////////////////////////////////////////////////

	/** Creates a new Stream. */
	new() {	this(new ThreadSafePublisher) }
	
	/** Most detailed constructor, where you can specify your own publisher. */
	new(Publisher<Entry<T>> publisher) { this.stream = publisher }
	
	
	// PUBLISHER ///////////////////////////////////////////////////////////////
	
	/**
	 * Push an entry into the stream. An entry can be a Value, a Finish or an Error.
	 */
	override apply(Entry<T> entry) {
		// only allow if we have a value and we are not skipping
		if(entry == null) throw new NullPointerException('cannot stream a null entry')
		if(isOpen.get) stream.apply(entry)
	}

	/**
	 * Listen to entries (value/finish/error) coming from the stream. For each new entry, 
	 * the passed listerer will be called with the value.
	 */
	override onChange(Procedure1<Entry<T>> listener) {
		// start listening
		stream.onChange [
			try {
				listener.apply(it)
			} catch(Throwable t) {
				if(catchErrors) stream.apply(new Error(t))
				else throw new StreamException(t)
			}
		]
	}
	
	override getSubscriptionCount() {
		stream.subscriptionCount
	}
	
	
	// GETTERS & SETTERS ///////////////////////////////////////////////////////

	/** 
	 * If true, the stream will catch listener handler errors. You can listen for these errors
	 * using the method .onError(handler).
	 */
	def setCatchErrors(boolean catchErrors) {
		this.catchErrors = catchErrors
	}
	
	
	// CONTROL ////////////////////////////////////////////////////////////////

	/** 
	 * Start a stream. A basic stream like this is auto-started, so start does nothing.
	 */
	def open() {
		isOpen.set(true)
		if(onOpen != null) onOpen.apply(null)
		this
	}
	
	/**
	 * Close a the stream, and disconnects it from any parent.
	 */
	override close() {
		if(!isOpen.get) throw new StreamException('cannot close a closed stream.')
		isOpen.set(false)
		if(onClose != null) onClose.apply(null)
	}
	

	// PUSH ///////////////////////////////////////////////////////////////////

	/**
	 * Push a value into the stream.
	 */
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
	def Stream<T> finish() {
		apply(new Finish)
		this
	}
	

	// LISTEN /////////////////////////////////////////////////////////////////

		
	/**
	 * Listen to values coming from the stream. For each new value, the passed
	 * listerer will be called with the value.
	 */
	def each((T)=>void listener) {
		onChange [ switch it { Value<T>: listener.apply(value) } ]
		this
	}
	
	/**
	 * Listen for a finish call being done on the stream. For each finish call,
	 * the passed listener will be called with this stream.
	 */	
	def onFinish(Procedure1<Void> listener) {
		onChange [ switch it { Finish<T>: listener.apply(null) } ]
		this
	}
	/**
	 * Listen for errors in the stream or parent streams. 
	 * The stream only catches errors if catchErrors is set to true.
	 */
	def onError(Procedure1<Throwable> listener) {
		stream.onChange [ switch it { Error<T>: listener.apply(error) } ]
		this
	}
	
	/** Let a parent stream listen when this stream opens */
	def onOpen(Procedure1<Void> listener) {
		this.onOpen = listener
		this
	}
	
	/** Let a parent stream listen when this stream closes */
	def onClose(Procedure1<Void> listener) {
		this.onClose = listener
		this
	}
	
	
	// OTHER //////////////////////////////////////////////////////////////////
	
	override toString() '''«this.class.name»'''
	
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
