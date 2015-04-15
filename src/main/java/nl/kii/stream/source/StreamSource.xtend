package nl.kii.stream.source

import nl.kii.stream.IStream

/**
 * A source is a streamable source of information.
 */
interface StreamSource<I, O> {

	/** Create a new stream and pipe source stream to this stream */	
	def IStream<I, O> stream()
	
	/** Connect an existing stream as a listener to the source stream */
	def StreamSource<I, O> pipe(IStream<I, ?> stream)

}
