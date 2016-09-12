package nl.kii.async.fibers

import co.paralleluniverse.fibers.Suspendable
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.stream.Stream

import static extension nl.kii.async.fibers.FiberExtensions.*
import java.util.Iterator

/**
 * An iterator implementation that awaits each value from the stream.
 */
class StreamIterator<T> implements Iterator<T> {
	
	val Stream<?, T> stream
	val last = new AtomicReference<T>
	
	new(Stream<?, T> stream) {
		this.stream = stream
	}
			
	@Suspendable
	override hasNext() {
		last.set(stream.awaitNext)
		last.get != null
	}
	
	@Suspendable
	override next() {
		if(last.get != null) last.get
		else stream.awaitNext
	}
	
	override remove() {
		throw new UnsupportedOperationException('you cannot remove items from a stream iterator')
	}
	
}