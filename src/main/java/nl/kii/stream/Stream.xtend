package nl.kii.stream

import nl.kii.stream.internal.BaseStream
import nl.kii.stream.message.Error
import nl.kii.stream.message.Finish
import nl.kii.stream.message.Value

class Stream<T> extends BaseStream<T, T> {

	override getInput() { this }

	/** Queue a value on the stream for pushing to the listener */
	override push(T value) { apply(new Value(value, value)) }
	
	/** 
	 * Tell the stream an error occurred. the error will not be thrown directly,
	 * but passed and can be listened for down the stream.
	 */
	override error(Throwable error) { apply(new Error(null, error)) }
	
	/** Tell the stream the current batch of data is finished. The same as finish(0). */
	override finish() { apply(new Finish(null, 0)) }	

	/** Tell the stream a batch of the given level has finished. */
	override finish(int level) { apply(new Finish(null, level)) }	
 	
}

