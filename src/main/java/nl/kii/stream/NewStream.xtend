package nl.kii.stream

import java.util.Queue
import nl.kii.act.Actor

import static com.google.common.collect.Queues.*

class Stream<T> extends Actor<StreamMessage> {

	var open = true
	var listenerReady = false
	var skipping = false

	val Queue<Entry<T>> queue
	var (Entry<T>)=>void entryListener
	var (StreamCommand)=>void notifyListener

	new() { this(newConcurrentLinkedQueue) }

	new(Queue<Entry<T>> queue) {
		this.queue = queue
	}

	/**
	 * Process next incoming entry.
	 * Since the stream extends Actor, there is no more than one thread active.
	 */
	override act(StreamMessage entry, =>void done) {
		switch entry {
			Value<T>, Finish<T>, Error<T>: {
				queue.add(entry)
				publishNext
			}
			Next: {
				listenerReady = true
				// try to publish the next from the queue
				val published = publishNext
				// if nothing was published, notify there parent stream we need a next entry
				if(!published) notify(entry)			
			}
			Skip: {
				if(skipping) return 
				else skipping = true		
				// discard everything up to finish from the queue
				while(skipping && !queue.empty) {
					switch queue.peek {
						Finish<T>: skipping = false
						default: queue.poll
					}
				}
				// if we are still skipping, notify the parent stream it needs to skip
				if(skipping) notify(entry)			
			}
			Close: {
				publishNext
				open = false
				notify(entry)
			}
		}
		done.apply
	}
	
	/** take an entry from the queue and pass it to the listener */
	def protected boolean publishNext() {
		if(listenerReady && entryListener != null && !queue.empty) {
			listenerReady = false
			val entry = queue.poll
			if(entry instanceof Finish<?>)
				skipping = false
			try {
				entryListener.apply(entry)
				true
			} catch (Throwable t) {
				if(entry instanceof Error<?>) throw t
				listenerReady = true
				apply(new Error(t))
				false
			}
		} else false
	}

	def protected notify(StreamCommand notification) {
		if(notifyListener != null)
			notifyListener.apply(notification)
	}	
	
	synchronized def void onEntry((Entry<T>)=>void entryListener) {
		this.entryListener = entryListener
	}
	
	synchronized def void onNotification((StreamCommand)=>void notifyListener) {
		this.notifyListener = notifyListener
	}
	
	def next() { apply(new Next) }
	
	def skip() { apply(new Skip) }
	
	def close() { apply(new Close) }
	
}

// STREAM ENTRY ///////////////////////////////////////////////////////////////

interface StreamMessage { }

interface StreamCommand extends StreamMessage { }

interface Entry<T> extends StreamMessage { }

class Next implements StreamCommand { }

class Skip implements StreamCommand { }

class Close implements StreamCommand{ }

class Value<T> implements Entry<T> {
	public val T value
	new(T value) { this.value = value }
	override toString() { value.toString }
	override equals(Object o) { o instanceof Value<?> && (o as Value<?>).value == this.value }
}

class Finish<T> implements Entry<T> {
	override toString() { 'finish' }
	override equals(Object o) { o instanceof Finish<?> }
}

class Error<T> implements Entry<T> {
	public val Throwable error
	new(Throwable error) { this.error = error }
	override toString() { 'error: ' + error.message }
}


