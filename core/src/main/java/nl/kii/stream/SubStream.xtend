package nl.kii.stream

import nl.kii.async.options.AsyncOptions
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

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

	new(AsyncOptions options) {
		super(options.copy)
	}

	new(IStream<?, ?> parent) {
		super(parent.options.copy)
	}

	// APPLYING PAIRS TO THE SUBSTREAM

	/** Queue a value on the stream for pushing to the listener */
	def push(I from, O value) { apply(new Value(from, value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	def error(I from, Throwable error) { apply(new Error(from, error)) }
	
}
