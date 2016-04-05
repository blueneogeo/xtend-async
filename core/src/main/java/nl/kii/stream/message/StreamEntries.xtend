package nl.kii.stream.message

import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

/** 
 * Stream entries contain data for the listener.
 * Entries travel downwards a stream towards the listeners of the stream at the end.
 */
interface Entry<I, O> extends StreamMessage {
	/** Get the data the generated the entry */
	def I getFrom()
}

/** 
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
class Entries<I, O> implements StreamMessage {
	public val List<Entry<I, O>> entries
	new(Entry<I, O>... entries) { this.entries = entries }
	override toString() { entries.toString }
	// FIX: this is not correct, needs to iterate through entries
	override equals(Object o) { o instanceof Entries<?,?> && (o as Entries<?,?>).entries == this.entries }
}

/** Wraps a streamed data value of type T */
class Value<I, O> implements Entry<I, O> {
	@Accessors(PUBLIC_GETTER) val I from
	public val O value
	new(I from, O value) { 
		this.from = from
		if (value == null) throw new IllegalArgumentException('value in stream entry cannot be null')
		this.value = value
	}
	override toString() { value.toString }
	override equals(Object o) { o instanceof Value<?, ?> && (o as Value<?, ?>).value == this.value }
}

/**  Indicates that the stream encountered an error while processing information. */
class Error<I, O> implements Entry<I, O> {
	@Accessors(PUBLIC_GETTER) val I from
	public val Throwable error
	new(I from, Throwable error) { this.from = from this.error = error }
	override toString() { 'error: ' + error.message }
}

/** Indicates that there is no more data and the stream can be closed */
class Closed<I, O> implements Entry<I, O> {
	override getFrom() { null }
	override equals(Object o) { o instanceof Closed<?, ?> }
	override toString() { 'finish' }
}
