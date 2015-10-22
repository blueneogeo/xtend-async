package nl.kii.stream.message

import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

/** 
 * An entry is a stream message that contains either a value or stream state information.
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

/** 
 * Indicates that a batch of data has finished.
 * Batches of data can be of different levels. The finish has a level property that indicates
 * which level of data was finished.
 */
class Finish<I, O> implements Entry<I, O> {
	@Accessors(PUBLIC_GETTER) val I from
	public val int level
	new() { this(null, 0) }
	new(I from, int level) { this.from = from this.level = level }
	override toString() { 'finish(' + level + ')' }
	override equals(Object o) { o instanceof Finish<?,?> && (o as Finish<?,?>).level == level }
}

/** Indicates that the stream was closed and no more data will be passed */
class Closed<I, O> implements Entry<I, O> {
	override getFrom() { null }
	override toString() { 'closed stream' }
}

/**  Indicates that the stream encountered an error while processing information. */
class Error<I, O> implements Entry<I, O> {
	@Accessors(PUBLIC_GETTER) val I from
	public val Throwable error
	new(I from, Throwable error) { this.from = from this.error = error }
	override toString() { 'error: ' + error.message }
}
