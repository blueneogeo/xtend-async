package nl.kii.async.stream

import nl.kii.async.observable.Observable

/** 
 * A source pushes values into a stream, and manages the flow
 * control. It also makes sure that only one thread is working
 * on the stream and that we don't get stack overflow.
 * <p>
 * Sources pass two values into a stream:
 * <li>IN: information about how the value got pushed into
 * the stream. For example, it could be a message you can later
 * reply to, or meta information about the data.
 * <li>OUT: the value data that gets pushed out from the source.
 * <p>
 * Sources can be optionally flow controlled. You create flow
 * control by only pushing in new values when onNext() is
 * called.
 * <p>
 * Notes:
 * <li>If you do not need pass input, use Sink\<OUT\> instead. 
 * <li>Source is NOT threadsafe! Use StreamExtensions.synchronize to make threadsafe.
 */
abstract class Source<IN, OUT> extends Pipe<IN, OUT> implements Observable<IN, OUT> {

	var open = true
	var paused = false
	var ready = false
	var busy = false

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
