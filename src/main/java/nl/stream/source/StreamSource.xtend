package nl.stream.source

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import nl.kii.stream.Entry
import nl.kii.stream.Stream
import nl.kii.stream.StreamCommand
import nl.kii.stream.StreamMessage

/**
 * A source is a streamable source of information.
 */
interface StreamSource<T> {

	/** Create a new stream and pipe source stream to this stream */	
	def Stream<T> stream()
	
	/** Connect an existing stream as a listener to the source stream */
	def StreamSource<T> pipe(Stream<T> stream)

}

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
abstract class StreamSplitter<T> extends Actor<StreamMessage> implements StreamSource<T> {
	
	/** the source stream that gets distributed */
	protected val Stream<T> source
	
	/** the connected listening streams */
	@Atomic protected val List<Stream<T>> streams
	
	new(Stream<T> source) {
		this.source = source
		this.streams = new CopyOnWriteArrayList
		source.onChange [ apply ]
	}
	
	override StreamSource<T> pipe(Stream<T> stream) {
		streams += stream
		stream.onCommand [ apply ]
		// if the stream already asked for a next value, 
		// try again, so this time this splitter can react to it
		if(stream.ready) stream.next
		this
	}
	
	override Stream<T> stream() {
		new Stream<T> => [ pipe ]
	}
	
	/** we are wrapping in an actor to make things threadsafe */
	override protected act(StreamMessage message, =>void done) {
		switch message {
			Entry<T>: onEntry(message)
			StreamCommand: onCommand(message)
		}
		done.apply
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
