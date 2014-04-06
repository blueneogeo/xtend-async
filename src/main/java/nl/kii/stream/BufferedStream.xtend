package nl.kii.stream

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import nl.kii.stream.impl.ThreadSafePublisher

/**
 * A Buffered Stream extends a stream with following features:
 * 
 * <li>a stream needs to be started before listeners get messages
 * <li>all registered listeners have to request a next value before an entry is streamed
 * <li>until the above requirements are matched, incoming entries are buffered in a queue 
 * <p>
 * This is useful for at least three scenarios:
 * <ol>
 * <li>you want to create a stream and push in data before you start listening
 * <li>you need flow control
 * <li>shortcutting
 * </ol>
 * 
 * <h1>Push before listening</h1>
 * 
 * If you create a normal stream, it will directly push to its listeners whatever you
 * put in. So say that you already have a list of ids and want to print them:
 * <p>
 * <pre>
 * val stream = new Stream<Integer> << 1 << 2 << 3 // items stream directly!
 * stream.each [ println(it) ] // so nothing gets printed
 * </pre>
 * <p>
 * In a BufferedStream, this will not happen, because streams will buffer:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer> << 1 << 2 << 3 // items get queued
 * stream.each [ println(it) ] // each starts the stream, items get printed 
 * </pre>
 * <p>
 * You can also choose to not start a stream with each:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer> << 1 << 2 << 3 // items get queued
 * stream.each(false) [ println(it) ] // nothing gets printed yet
 * stream.each(false) [ println(it + 'again') ] // adding another listener, still nothing gets printed
 * stream.start // starts the stream, everthing gets printed twice (two listeners added) 
 * </pre>
 * 
 * <h1>Flow Control</h1>
 * 
 * Flow control is necessary especially for asynchronous programming. Say that you push
 * a million values into a stream, and you map these to asynchronous functions. These
 * mappings will be instantanious. This means that a normal stream would push all of its
 * values through at once, and you'd be making a million parallel calls at the same time.
 * <p>
 * Example where this goes wrong:
 * <p>
 * <pre>
 * val stream = new Stream<Integer>
 * stream
 *     .async [ loadArticleAsync(it) ] // loadArticleAsync returns a Promise
 *     .each [ println('got article with title ' + title ]
 * 1..1000000.each [ it >> stream ] // this overwhelms the system
 * </pre>
 * <p>
 * How you can control it:
 * <p>
 * BufferedStream has a version of .each() which takes an onNext closure as a parameter.
 * By calling this closure, you signal that the listener you pass to .each() is ready to
 * recieve the next value. BufferedStream keeps track of its listeners and only lets the
 * next value flow from its buffer when all listeners are ready. (Normal .each() calls
 * always are ready). An example:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer>
 * stream.each [ it, next |
 *     val promise = loadArticleAsync
 *     promise.then [
 *         println('got article with title ' + title)
 *         next.apply // done! ask for the next article
 *     ]
 * ]
 * 1..1000000.each [ it >> stream ] // now handled one by one
 * </pre>
 * <p>
 * BufferedStreamExt adds many other versions of this, and a version of async that
 * lets you specify the amount of parallelism you wish for. It encapsulates the call
 * to next for you, so you don't need to consider it anymore in your code.
 * <p>
 * For 3 parallel calls maximum:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer>
 * stream
 *     .async(3) [ loadArticleAsync ]
 *     .each [ println('got article with title ' + title ]
 * 1..1000000.each [ it >> stream ] // now handled max 3 at the same time
 * </pre>
 * 
 * <h1>Shortcutting</h1>
 * 
 * Sometimes you know in advance you don't need to continue listening. For example,
 * if you just want the first 5 values from a stream, you can signal that you are 
 * done from then on. This is called shortcutting. Since bufferedstreams can indicate
 * when they need a value, they can simply stop asking. 
 * 
 * 
 * @see Stream<T>
 */
class BufferedStream<T> extends Stream<T> {
	
	/** 
	 * The buffer gets filled when there are entries entering the stream
	 * even though there are no listeners yet. The buffer will only grow
	 * upto the maxBufferSize. If more entries enter than the size, the
	 * buffer will overflow and discard these later entries.
	 */
	var Queue<Entry<T>> buffer
	
	/** 
	 * Amount of open listeners. Used to know if a stream is done. If there are
	 * no open listeners anymore, a stream can stop streaming until the next finish. 
	 */
	val readyListenerCount = new AtomicInteger(0) 

	// NEW /////////////////////////////////////////////////////////////////////

	/** Creates a new Stream. */
	new() {	this(new ThreadSafePublisher) }
	
	/** Most detailed constructor, where you can specify your own publisher. */
	new(Publisher<Entry<T>> publisher) { super(publisher) }
	
	// GETTERS & SETTERS ///////////////////////////////////////////////////////

	/**
	 * Returns the buffer that gets built up when values are pushed into a stream without the
	 * stream having started or listeners being ready.
	 */
	def getBuffer() {
		if(buffer == null) buffer = new ConcurrentLinkedQueue
		buffer
	}
	
	/**
	 * We are ready to process if the stream is started and ALL listeners have requested a next value
	 */
	def isReady() {
		isOpen.get && (readyListenerCount.get == subscriptionCount)
	}

	// PUSH ///////////////////////////////////////////////////////////////////

	/** 
	 * Start streaming. If anything was buffered and the listeners are ready, 
	 * it will also start processing.
	 */
	override open() {
		if(isOpen.get) throw new StreamException('cannot start an already started stream.')
		super.open
		if(ready) processNext
		this
	}
	
	/**
	 * Push an entry into the stream buffer. It will also try to process the entry, but that
	 * will only happen if the stream is started and all the listeners are ready.
	 */
	override void apply(Entry<T> value) {
		if(value == null) throw new NullPointerException('cannot stream a null value')
		getBuffer.add(value)
		if(ready) processNext
	}

	/**
	 * Takes a value from the buffer/queue and pushes it to the listeners for processing.
	 * @return true if a value was processed from the buffer.
	 */
	def boolean processNext() {
		// if anything was buffered, get the next value from the buffer and push it
		if(buffer != null && !buffer.empty) {
			stream.apply(buffer.poll)
			true
		} else false
	}
	
	// LISTEN /////////////////////////////////////////////////////////////////
	
	def each(boolean startStream, (T, =>void)=>void listener) {
		// create a readyFn that the listener can call when it is ready to process a value
		val =>void nextFn = [|
			// one more listener is ready
			readyListenerCount.incrementAndGet
			// if all listeners are now ready, process the next value
			if(ready) processNext
		]
		// start listening, passing along the readyFn
		each [ listener.apply(it, nextFn) ]
		// if startStream was set, start the stream
		if(startStream && !isOpen.get) open
		this
	}

	override toString() '''«this.class.name» { 
			open: «isOpen», buffer: «IF(buffer != null) » «buffer.length» «ELSE» none «ENDIF»
		}
	'''
	
}
