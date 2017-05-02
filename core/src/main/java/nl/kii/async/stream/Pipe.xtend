package nl.kii.async.stream

import co.paralleluniverse.fibers.Suspendable
import nl.kii.async.annotation.Suspending
import nl.kii.async.observable.Observer

/* 
 * A pipe is a controllable stream connection. It is the basic building block for building stream chains.
 */
abstract class Pipe<IN, OUT> implements Observer<IN, OUT>, Stream<IN, OUT> {

	volatile protected Observer<IN, OUT> output
	
	override setObserver(@Suspending Observer<IN, OUT> observer) {
		this.output = observer
	}
	
	@Suspendable
	override value(IN in, OUT value) {
		if(output === null) return;
		try {
			output.value(in, value)
		} catch(Throwable t) {
			error(in, t)
		}
	}
	
	@Suspendable
	override error(IN in, Throwable t) {
		if(output === null) return;
		// if something goes wrong, let the exception escalate 
		output.error(in, t)
	}
	
	@Suspendable
	override complete() {
		if(output === null) return;
		try {
			output.complete
			// no longer need the output reference
			output = null
		} catch(Throwable t) {
			error(null, t)
		}
	}
	
	@Suspendable
	override close() {
		// allow garbage collect
		output = null
	}
	
	override toString() '''Pipe(open: «open», observed: «output !== null»)'''
	
}
