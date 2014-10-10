package nl.kii.stream

import java.util.List

/** A message used by streams and promises */
interface StreamMessage { }

// NOTIFICATIONS //////////////////////////////////////////////////////////////

/** A command given to a stream. 
 * Commands travel upwards towards the source of a stream, to control the stream.
 */
interface StreamNotification extends StreamMessage { }

/** Request the next entry from the stream */
class Next implements StreamNotification { }

/** Request the stream to stop sending entries until after the next finish entry */
class Skip implements StreamNotification { }

/** Request the stream to close and stop sending */
class Close implements StreamNotification{ }

/** Warns that the buffer is full */
class Overflow implements StreamNotification{
	public val Entry<?,?> entry
	new(Entry<?,?> entry) { this.entry = entry }
}

// ENTRIES ////////////////////////////////////////////////////////////////////

/** 
 * An entry is a stream message that contains either a value or stream state information.
 * Entries travel downwards a stream towards the listeners of the stream at the end.
 */
interface Entry<R, T> extends StreamMessage { }

/** 
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
class Entries<R, T> implements StreamMessage {
	public val List<Entry<R, T>> entries
	new(Entry<R, T>... entries) { this.entries = entries }
	override toString() { entries.toString }
	// FIX: this is not correct, needs to iterate through entries
	override equals(Object o) { o instanceof Entries<?,?> && (o as Entries<?,?>).entries == this.entries }
}

/** Wraps a streamed data value of type T */
class Value<R, T> implements Entry<R, T> {
	public val R from
	public val T value
	new(R from, T value) { this.from = from this.value = value }
	override toString() { value.toString }
	override equals(Object o) { o instanceof Value<?, ?> && (o as Value<?, ?>).value == this.value }
}

/** 
 * Indicates that a batch of data has finished.
 * Batches of data can be of different levels. The finish has a level property that indicates
 * which level of data was finished.
 */
class Finish<R, T> implements Entry<R, T> {
	public val R from
	public val int level
	new() { this(null, 0) }
	new(R from, int level) { this.from = from this.level = level }
	override toString() { 'finish(' + level + ')' }
	override equals(Object o) { o instanceof Finish<?,?> && (o as Finish<?,?>).level == level }
}

/** Indicates that the stream was closed and no more data will be passed */
class Closed<R, T> implements Entry<R, T> {
	override toString() { 'closed stream' }
}

/**  Indicates that the stream encountered an error while processing information. */
class Error<R, T> implements Entry<R, T> {
	public val R from
	public val Throwable error
	new(R from, Throwable error) { this.from = from this.error = error }
	override toString() { 'error: ' + error.message }
}
