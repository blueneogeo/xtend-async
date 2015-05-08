package nl.kii.stream.internal

import nl.kii.stream.message.Error
import nl.kii.stream.message.Finish
import nl.kii.stream.message.Value
import nl.kii.stream.IStream
import nl.kii.async.AsyncException

/**
 * Streams can be chained with operations, such as map, effect, and onEach.
 * Each of these operations creates a new stream from the starting stream,
 * and these new streams are called sub streams.
 * <p>
 * Since they are based on a stream, they must be constructed with a parent stream.
 * <p>
 * Pushing a value to a substream actually pushes it into the root of the chain of streams.
 */
class SubStream<I, O> extends BaseStream<I, O> {

	val protected IStream<I, I> input

	new() {
		input = null
	}

	new (IStream<I, ?> parent) {
		this.input = parent.input
		this.concurrency = parent.concurrency
	}

	new (IStream<I, ?> parent, int maxSize) {
		super(maxSize)
		this.input = parent.input
	}

	override getInput() { input }

	// APPLYING VALUES TO THE ROOT STREAM

	/** Queue a value on the stream for pushing to the listener */
	override push(I value) { 
		if(input != null) input.push(value) 
		else throw new AsyncException('This substream cannot push(value), since it has no input stream. Either construct with an input stream, or use substream.push(from, value) instead.', value, null)
	}
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	override error(Throwable t) { input.error(t) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	override finish() { input.finish }	

	/** Tell the stream a batch of the given level has finished. */
	override finish(int level) { input.finish(level) }	

	// APPLYING PAIRS TO THE SUBSTREAM

	/** Queue a value on the stream for pushing to the listener */
	def push(I from, O value) { apply(new Value(from, value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	def error(I from, Throwable error) { apply(new Error(from, error)) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	def finish(I from) { apply(new Finish(from, 0)) }	

	/** Tell the stream a batch of the given level has finished. */
	def finish(I from, int level) { apply(new Finish(from, level)) }	
	
}

