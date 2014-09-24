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
	public val Entry<?> entry
	new(Entry<?> entry) { this.entry = entry }
}

// ENTRIES ////////////////////////////////////////////////////////////////////

/** 
 * An entry is a stream message that contains either a value or stream state information.
 * Entries travel downwards a stream towards the listeners of the stream at the end.
 */
interface Entry<T> extends StreamMessage { }

/** 
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
class Entries<T> implements StreamMessage {
	public val List<Entry<T>> entries
	new(Entry<T>... entries) { this.entries = entries }
	override toString() { entries.toString }
	// FIX: this is not correct, needs to iterate through entries
	override equals(Object o) { o instanceof Entries<?> && (o as Entries<?>).entries == this.entries }
}

/** Wraps a streamed data value of type T */
class Value<T> implements Entry<T> {
	public val T value
	new(T value) { this.value = value }
	override toString() { value.toString }
	override equals(Object o) { o instanceof Value<?> && (o as Value<?>).value == this.value }
}

/** 
 * Indicates that a batch of data has finished.
 * Batches of data can be of different levels. The finish has a level property that indicates
 * which level of data was finished.
 */
class Finish<T> implements Entry<T> {
	public val int level
	new() { this(0) }
	new(int level) { 
		this.level = level
	}
	override toString() { 'finish(' + level + ')' }
	override equals(Object o) { o instanceof Finish<?> && (o as Finish<?>).level == level }
}

/** Indicates that the stream was closed and no more data will be passed */
class Closed<T> implements Entry<T> {
	override toString() { 'closed stream' }
}

/**  Indicates that the stream encountered an error while processing information. */
class Error<T> implements Entry<T> {
	public val Throwable error
	new(Throwable error) { this.error = error }
	override toString() { 'error: ' + error.message }
}
