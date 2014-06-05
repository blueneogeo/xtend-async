package nl.kii.stream

interface StreamMessage { }

// COMMANDS ///////////////////////////////////////////////////////////////////

interface StreamCommand extends StreamMessage { }

class Next implements StreamCommand { }

class Skip implements StreamCommand { }

class Close implements StreamCommand{ }

// ENTRIES ////////////////////////////////////////////////////////////////////

interface Entry<T> extends StreamMessage { }

/** 
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
class Entries<T> implements StreamMessage {
	public val Entry<T>[] entries
	new(Entry<T>[] entries) { this.entries = entries }
	override toString() { entries.toString }
	// TODO: this is not correct, needs to iterate through entries
	override equals(Object o) { o instanceof Entries<?> && (o as Entries<?>).entries == this.entries }
}

class Value<T> implements Entry<T> {
	public val T value
	new(T value) { this.value = value }
	override toString() { value.toString }
	override equals(Object o) { o instanceof Value<?> && (o as Value<?>).value == this.value }
}

class Finish<T> implements Entry<T> {
	public val int level
	new() { this(0) }
	new(int level) { 
		this.level = level
	}
	override toString() { 'finish(' + level + ')' }
	override equals(Object o) { o instanceof Finish<?> && (o as Finish<?>).level == level }
}

class Error<T> implements Entry<T> {
	public val Throwable error
	new(Throwable error) { this.error = error }
	override toString() { 'error: ' + error.message }
}
