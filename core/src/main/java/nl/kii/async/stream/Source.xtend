package nl.kii.async.stream

import nl.kii.async.observable.Observable

// import java.util.concurrent.atomic.AtomicBoolean
// import java.util.concurrent.atomic.AtomicReference

/** 
 * A source pushes values into a stream, and manages the flow
 * control. It also makes sure that only one thread is working
 * on the stream and that we don't get stack overflow.
 * <p>
 * Sources pass two values into a stream:
 * <li>a CONTEXT, information about how the value got pushed into
 * the stream. For example, it could be a message you can later
 * reply to, or meta information about the data.
 * <li>The IN, the input type of the stream, which simple is the
 * value data you are pushing in.
 * <p>
 * NOTE: If you do not need a context to be retained, use the Sink
 * class instead. 
 * <p>
 * Sources can be optionally flow controlled. You create flow
 * control by only pushing in new values when onNext() is
 * called.
 */
abstract class Source<CONTEXT, IN> extends Pipe<CONTEXT, IN> implements Observable<CONTEXT, IN> {

	var open = true
	var paused = false
	var ready = false
	var busy = false

//	/** ready means the next item may be processed */
//	val ready = new AtomicBoolean
//	/** busy means a thread is processing on this sink */
//	val busy = new AtomicBoolean
	/** optional listener for the stream being controlled */
	var Controllable controlListener
	
	/** What to do when the stream is asking for a next value */
	abstract def void onNext()

	/** What to do when the stream is being closed */	
	abstract def void onClose()

	/** 
	 * Gets the next message. This does a little magic so we can run this both multi threaded
	 * and single threaded. It keeps track if there is still a thread running the loop on the
	 * system. If so, busy is set to true. If the code is single threading, this will be the
	 * case and the code will simply return. This allows the callstack to unwind and the while
	 * loop to process the next item without growing the stack. 
	 */
	override next() {
		if(!open) return;
		ready = true
		if(busy || paused) return;
		busy = true
		while(ready) {
			ready = false
			onNext
			controlListener?.next
		}
		busy = false

//		if(!open) return;
//		ready.set(true)
//		if(paused) return;
//		if(!busy.compareAndSet(false, true)) return;
//		while(ready.compareAndSet(true, false)) {
//			onNext
//			controlListener?.next
//		}
//		busy.set(false)
	}
	
	override close() {
		open = false
		onClose
		controlListener?.close
	}

	override pause() {
		paused = true
		controlListener?.pause
	}
	
	override resume() {
		paused = false
		controlListener?.resume
		next
	}
	
	override isOpen() {
		open && !paused
	}
	
	/** Lets you listen in on the source being controlled */
	def void setControllable(Controllable controllable) {
		controlListener = controllable
	}

	override toString() '''Source(open: «open», ready: «ready», observed: «output != null», controllable set: «controlListener != null»)'''

}
