package nl.kii.stream.source

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import nl.kii.stream.Entry
import nl.kii.stream.IStream
import nl.kii.stream.StreamMessage
import nl.kii.stream.StreamNotification
import nl.kii.stream.SubStream

/**
 * A source is a streamable source of information.
 */
interface StreamSource<R, T> {

	/** Create a new stream and pipe source stream to this stream */	
	def IStream<R, T> stream()
	
	/** Connect an existing stream as a listener to the source stream */
	def StreamSource<R, T> pipe(IStream<R, T> stream)

}

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
abstract class StreamSplitter<R, T> extends Actor<StreamMessage> implements StreamSource<R, T> {
	
	/** the source stream that gets distributed */
	protected val IStream<R, T> source
	
	/** the connected listening streams */
	@Atomic protected val List<IStream<R, T>> streams
	
	new(IStream<R, T> source) {
		this.source = source
		this.streams = new CopyOnWriteArrayList
		source.onChange [ apply ]
	}
	
	override StreamSource<R, T> pipe(IStream<R, T> stream) {
		streams += stream
		stream.onNotify [ apply ]
		// if the stream already asked for a next value, 
		// try again, so this time this splitter can react to it
		if(stream.ready) stream.next
		this
	}
	
	override IStream<R, T> stream() {
		new SubStream<R, T>(source) => [ pipe ]
	}
	
	/** we are wrapping in an actor to make things threadsafe */
	override protected act(StreamMessage message, =>void done) {
		switch message {
			Entry<R, T>: onEntry(message)
			StreamNotification: onCommand(message)
		}
		done.apply
	}
	
	/** Handle an entry coming in from the source stream */
	abstract protected def void onEntry(Entry<R, T> entry)

	/** Handle a message coming from a piped stream */
	abstract protected def void onCommand(StreamNotification msg)

	/** Utility method that only returns true if all members match the condition */	
	protected static def <T> boolean all(Iterable<T> list, (T)=>boolean conditionFn) {
		list.findFirst[!conditionFn.apply(it)] == null
	}
	
}
