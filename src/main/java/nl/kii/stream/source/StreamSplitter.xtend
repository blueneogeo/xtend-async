package nl.kii.stream.source

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import nl.kii.act.Actor
import nl.kii.async.annotation.Atomic
import nl.kii.stream.IStream
import nl.kii.stream.internal.SubStream
import nl.kii.stream.message.Entry
import nl.kii.stream.message.StreamEvent
import nl.kii.stream.message.StreamMessage

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
abstract class StreamSplitter<I, O> extends Actor<StreamMessage> implements StreamSource<I, O> {
	
	/** the source stream that gets distributed */
	protected val IStream<I, O> source
	
	/** the connected listening streams */
	@Atomic protected val List<IStream<I, ?>> streams
	
	new(IStream<I, O> source) {
		this.source = source
		this.streams = new CopyOnWriteArrayList
		source.onChange [ apply ]
	}
	
	override StreamSource<I, O> pipe(IStream<I, ?> stream) {
		streams += stream
		stream.onNotify [ apply ]
		// if the stream already asked for a next value, 
		// try again, so this time this splitter can react to it
		if(stream.ready) stream.next
		this
	}
	
	override IStream<I, O> stream() {
		new SubStream<I, O>(source) => [ pipe ]
	}
	
	/** we are wrapping in an actor to make things threadsafe */
	override protected act(StreamMessage message, =>void done) {
		switch message {
			Entry<I, O>: onEntry(message)
			StreamEvent: onCommand(message)
		}
		done.apply
	}
	
	/** Handle an entry coming in from the source stream */
	abstract protected def void onEntry(Entry<I, O> entry)

	/** Handle a message coming from a piped stream */
	abstract protected def void onCommand(StreamEvent msg)

	/** Utility method that only returns true if all members match the condition */	
	protected static def <T> boolean all(Iterable<T> list, (T)=>boolean conditionFn) {
		list.findFirst[!conditionFn.apply(it)] == null
	}
	
}
