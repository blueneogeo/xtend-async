package nl.kii.stream

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import nl.kii.async.annotation.Atomic

/**
 * A source is a streamable source of information.
 */
interface Source<T> {

	/** Create a new stream and pipe source stream to this stream */	
	def Stream<T> stream()
	
	/** Connect an existing stream as a listener to the source stream */
	def Source<T> pipe(Stream<T> stream)

}

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
abstract class Splitter<T> implements Source<T> {
	
	/** the source stream that gets distributed */
	protected val Stream<T> source
	
	/** the connected listening streams */
	@Atomic protected val List<Stream<T>> streams
	
	new(Stream<T> source) {
		this.source = source
		this.streams = new CopyOnWriteArrayList
		source.onChange [ onEntry ]
	}
	
	override Stream<T> stream() {
		new Stream<T> => [ pipe ]
	}
	
	override Source<T> pipe(Stream<T> stream) {
		streams += stream
		stream.onNotification [ onCommand ]
		this
	}
	
	/** Handle an entry coming in from the source stream */
	abstract protected def void onEntry(Entry<T> entry)

	/** Handle a message coming from a piped stream */
	abstract protected def void onCommand(StreamCommand msg)

	/** Utility method that only returns true if all members match the condition */	
	protected static def <T> boolean all(Iterable<T> list, (T)=>boolean conditionFn) {
		list.findFirst[!conditionFn.apply(it)] == null
	}
	
}
