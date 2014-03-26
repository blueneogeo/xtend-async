package nl.kii.stream

import java.util.List
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.stream.impl.ThreadSafePublisher
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * <h1>what is a stream</h1>
 * A stream can receive values, and then transmit these values
 * to its listeners. To push a value into a stream, use .push().
 * To listen to values, call .each()
 * 
 * <h1>what a stream passes</h1>
 * A Stream is a publisher of three kinds of entries:
 * <li>a value
 * <li>a finish of a batch
 * <li>an error
 * 
 * <h1>finishing a stream</h1>
 * A stream can be finished by calling .finish(). This marks the end
 * of a batch of values that you have inputted.
 * <p>
 * Finish is necessary to mark then end of a stream of pushed values.
 * For example, the StreamExt.collect() extension uses it to know when
 * a bunch of values have to be collected and transformed into a list.
 * <p>
 * You can repeat this process multiple times on a stream. In the case
 * of collect(), this results in multiple lists being generated in
 * the resulting stream.
 * <p>
 * To just get the first value from a stream, call .then() or .first().
 * 
 * <h1>buffering</h1>
 * A stream will only start streaming after start() has been called.
 * Until start is called, it will attempt to buffer the incoming
 * entries, until the maximum buffer size has been reached. Entries
 * pushed to the stream without listeners and a full buffer will be
 * discarded.
 * 
 * <h1>catching listener errors</h1>
 * If you have multiple asynchronous processes happening, it can be
 * practical to catch any errors thrown at the end of a stream chain,
 * and not inside the listeners. If you enable catchErrors, the stream
 * will catch any errors occurring in listeners and will instead pass
 * them to a listener that you can pass by calling onError().
 * 
 * <h1>extensions</h1>
 * The stream class only supports a basic publishing interface 
 * 
 * 
 */
class Stream<T> implements Publisher<Entry<T>> {
	
	/** Set these to set how all streams are set by default */
	public static var CATCH_ERRORS_DEFAULT = false
	public static var AUTOSTART_DEFAULT = true
	public static var MAX_BUFFERSIZE_DEFAULT = 1000
	
	/** 
	 * The publisher registers listeners and calls the listeners when
	 * there are new entries for them. You register a listener by
	 * calling stream.each(...)
	 */
	val Publisher<Entry<T>> stream
	
	/** 
	 * The buffer gets filled when there are entries entering the stream
	 * even though there are no listeners yet. The buffer will only grow
	 * upto the maxBufferSize. If more entries enter than the size, the
	 * buffer will overflow and discard these later entries.
	 */
	var List<Entry<T>> buffer
	
	/**
	 * Streams can be chained to filter, transform and redirect its data.
	 * However a chained stream must be able to tell its parent some messages.
	 * For this, you need to pass the parentStream to a stream when you chain.
	 * The stream then uses its parentStream to pass on these messages, such
	 * as the start command. This enables you to start a substream, and have that
	 * command travel all the way up to the stream chain and start the data flow.
	 */
	val Stream<?> parentStream

	/**
	 * A function that can be called by this stream to communicate to the parentStream 
	 * that the listener that spawned this substream is done. This means the parentStream
	 * can stop sending messages, and when all of its substreams are done, it can
	 * stop alltogether. This means no unnecessary messages will be sent by the
	 * parent stream, which saves performance.
	 */
	public var =>void onDone

	/** 
	 * Stores if a stream is started. A started stream pushes data to its listeners
	 * as it comes in.
	 */
	val isStarted = new AtomicBoolean(false)
	
	/** 
	 * Stores if a stream is done. A done stream stops streaming, until it recieves
	 * a finish command, at which it resets itself and starts again.
	 */
	val isStreamDone = new AtomicBoolean(false)

	/** 
	 * Amount of open listeners. Used to know if a stream is done. If there are
	 * no open listeners anymore, a stream can stop streaming until the next finish. 
	 */
	val openListenerCount = new AtomicInteger(0) 

	/** how many entries the stream, when not yet started, will buffer before overflowing */
	public var maxBufferSize = MAX_BUFFERSIZE_DEFAULT

	/**
	 * If true, once you call .each(true), the stream will automatically start.
	 * Sometimes this is unwanted behavior and you want to construct a stream chain
	 * and manually call .start() at the end. By setting autostart to false, the
	 * stream will only start once you call .start()
	 */
	public var autostart = AUTOSTART_DEFAULT	

	/** 
	 * If true, errors will be caught and propagated as error entries. You can then
	 * listen for errors at the end of the stream (but for the start/each call). 
	 * If left false, exceptions will throw inside of the stream like a normal throw.
	 */
	public var catchErrors = CATCH_ERRORS_DEFAULT

	// NEW /////////////////////////////////////////////////////////////////////

	new() {	
		this(null)
	}
	
	/** 
	 * pass a parentStream when creating a derived stream (filter, map, etc), 
	 * so the childstream can communicate messages up the chain.
	 */
	new(Stream<?> parentStream) {
		this(new ThreadSafePublisher<Entry<T>>, parentStream)
	}
	
	new(Publisher<Entry<T>> entryStream, Stream<?> parentStream) {
		this.stream = entryStream
		this.parentStream = parentStream
		// inherit properties from the parent stream, if any
		if(parentStream != null) {
			this.autostart = parentStream.autostart
			this.catchErrors = parentStream.catchErrors
		}
	}
	
	// GETTERS & SETTERS ///////////////////////////////////////////////////////

	/**
	 * Che
	 */
	def isStreamDone() {
		isStreamDone.get
	}
	
	/** meant for substreams to call when they no longer require values */
	def void setStreamDone() {
		isStreamDone.set(true)
		if(onDone != null) onDone.apply
	}

	def autostart(boolean enabled) { 
		this.autostart = enabled
		this
	}
	
	/** If true, errors will be caught and propagated as error entries. default: false */
	def catchErrors(boolean enabled) { 
		this.catchErrors = enabled
		this
	}
		
	// PUSH ///////////////////////////////////////////////////////////////////

	/** 
	 * Start the streaming. If items were buffered by calling push or apply before the
	 * stream was started, these will be pushed into the stream first.
	 */
	def Stream<T> start() {
		if(isStarted.get) throw new StreamException('cannot start an already started stream.')
		isStarted.set(true)
		if(parentStream != null) parentStream.start		
		if(buffer != null) {
			buffer.forEach [ apply(it) ]
			buffer.clear
		}
		this
	}
	
	def isStarted() {
		isStarted
	}
	
	def setCatchErrors(boolean catchErrors) {
		this.catchErrors = catchErrors
		if(parentStream != null) parentStream.catchErrors = catchErrors
	}

	/**
	 * Push an entry into the stream. An entry can be a Value, a Finish or an Error.
	 * If the stream was not yet started, a buffer will be created and the entry buffered,
	 * up to the maxBufferSize that was set in the stream.
	 */
	override apply(Entry<T> value) {
		if(value == null) throw new NullPointerException('cannot stream a null value')
		if(isStarted.get) {
			println('applying ' + value)
			switch value {
				Value<T>: {
					if(openListenerCount.get > 0) 
						stream.apply(value)
				}
				Finish<T>: {
					openListenerCount.set(0)
					stream.apply(value)
				}
				default: stream.apply(value)
			}
		} else {
			if(buffer == null) buffer = newLinkedList
			if(buffer.length < maxBufferSize) buffer.add(value)
			else throw new StreamException('stream buffer overflow, max buffer size is ' + maxBufferSize + '. adding ' + value)
		}
	}

	/**
	 * Push a value into the stream.
	 * If the stream was not yet started, a buffer will be created and the entry buffered,
	 * up to the maxBufferSize that was set in the stream.
	 */
	def push(T value) {
		if(value == null) throw new NullPointerException('cannot stream a null value')
		apply(new Value(value))
		this
	}

	def getBuffer() {
		buffer
	}

	/**
	 * Finish a batch of data that was pushed into the stream. Note that a finish may be called
	 * more than once, indicating that multiple batches were passed. This is unusual,
	 * but some streaming mechanisms make use of this feature. 
	 */	
	def Stream<T> finish() {
		apply(new Finish)
		this
	}

	// LISTEN /////////////////////////////////////////////////////////////////
	
	/**
	 * Listen to values coming from the stream. For each new value, the passed
	 * listerer will be called with the value. If startStream is true, and
	 * autostart is true, the stream will be started.
	 * <p>
	 * Each listener gets passed an onDone procedure. The handler of the
	 * listener can call this procedure to indicate that it no longer requires
	 * data from the stream. This allows the stream to to stop streaming values
	 * when all of its listeners are done.
	 */
	def each(boolean startStream, (T, =>void)=>void listener) {
		// create a proc that can be called by the listener when it needs
		// no further values.
		val doneCalled = new AtomicBoolean(false)
		val onDone = [|
			if(doneCalled.get) return;
			doneCalled.set(true)
			// stop streaming once all listeners are done 
			if(openListenerCount.decrementAndGet <= 0) 
				setStreamDone
		]
		// keep track of open listeners
		openListenerCount.incrementAndGet
		onChange [ 
			switch it { 
				Value<T>: if(!doneCalled.get) {
					listener.apply(value, onDone)
				}
			}
		]
		if(startStream && !isStarted.get && autostart) start
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
	
	def onError(Procedure1<Throwable> listener) {
		stream.onChange [ switch it { Error<T>: listener.apply(error) } ]
		setCatchErrors(true)
		this
	}
	
	/**
	 * Listen to entries (value/finish/error) coming from the stream. For each new entry, 
	 * the passed listerer will be called with the value.
	 */
	override onChange(Procedure1<Entry<T>> listener) {
		stream.onChange [
			try {
				listener.apply(it)
			} catch(Throwable t) {
				println(t)
				if(catchErrors) stream.apply(new Error(t))
				else throw new StreamException('onChange listener threw an error:', t)
			}
		]
	}
	
	override toString() '''«this.class.name» { 
		started: «isStarted», buffer: «IF(buffer != null) » «buffer.length» of «maxBufferSize» «ELSE» none «ENDIF»,
		parent: «if(parentStream != null) parentStream else 'none'»
	'''
	
}

// STREAM EXCEPTION ///////////////////////////////////////////////////////////

class StreamException extends Exception {
	new(String msg) { super(msg) }
	new(String msg, Throwable e) { super(msg, e) }
}

// STREAM ENTRY ///////////////////////////////////////////////////////////////

interface Entry<T> {
}

class Value<T> implements Entry<T> {
	public val T value
	new(T value) { this.value = value }
	override toString() { value.toString }
}

class Finish<T> implements Entry<T> {
	override toString() { 'finish' }
}

class Error<T> implements Entry<T> {
	public val Throwable error
	new(Throwable error) { this.error = error }
	override toString() { 'error: ' + error.message }
}
