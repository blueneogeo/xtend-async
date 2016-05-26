package nl.kii.async.stream

import nl.kii.async.observable.Observer

/* 
 * A pipe is a controllable stream connection. It is the basic building block for building stream chains.
 * <p>
 * Usually, to create a new chain in a stream chain, you use:
 * <p>
 * <pre>val subStream = Pipe.connect(parentStream)</pre>
 * <p>
 * This will create a stream that communicates the control commands directly up to the parent stream.
 * <p>
 * Note: the Controllable aspects of a stream are not implemented. This allows these to be
 * propagated to the source. This way, the Stream.open(), close(), pause(), resume() and next() etc calls
 * can all be propagated to the top of the stream chain (the Source). This saves many checks on the
 * streams and allows more freedom in how a source controls a stream.
 */
abstract class Pipe<IN, OUT> implements Observer<IN, OUT>, Stream<IN, OUT> {

	def static <IN, OUT> Pipe<IN, OUT> connect(Stream<IN, ?> stream) {
		new Pipe<IN, OUT> {

			override next() { 
				stream.next
			}

			override void close() {
				super.close
				stream.close
			}
			
			override pause() {
				stream.pause
			}
			
			override resume() {
				stream.resume
				stream.next
			}
			
			override isOpen() {
				stream.isOpen
			}
			
		}
	}
	
	volatile protected Observer<IN, OUT> output
	
	override setObserver(Observer<IN, OUT> observer) {
		this.output = observer
	}
	
	override value(IN in, OUT value) {
		if(output == null) return;
		try {
			output.value(in, value)
		} catch(Throwable t) {
			error(in, t)
		}
	}
	
	override error(IN in, Throwable t) {
		if(output == null) return;
		// if something goes wrong, let the exception escalate 
		output.error(in, t)
	}
	
	override complete() {
		if(output == null) return;
		try {
			output.complete
			// no longer need the output reference
			output = null
		} catch(Throwable t) {
			error(null, t)
		}
	}
	
	override close() {
		// allow garbage collect
		output = null
	}
	
	override toString() '''Pipe(open: «open», observed: «output != null»)'''
	
}
