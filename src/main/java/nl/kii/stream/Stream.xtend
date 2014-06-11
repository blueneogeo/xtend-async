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
	
	// CONTROL THE STREAM /////////////////////////////////////////////////////

	def next() { apply(new Next) }
	
	def skip() { apply(new Skip) }
	
	def close() { apply(new Close) }
	
	def push(T value) { apply(new Value(value)) }
	
	def error(Throwable error) { apply(new Error(error)) }
	
	def finish() { apply(new Finish(0)) }	

	def finish(int level) { apply(new Finish(level)) }	
	
	def getQueue() { queue.unmodifiableView	}

	// LISTENERS //////////////////////////////////////////////////////////////
	
	synchronized def void setListener((Entry<T>)=>void entryListener) {
		this.entryListener = entryListener
	}
	
	synchronized def void onNotification((StreamCommand)=>void notifyListener) {
		this.notifyListener = notifyListener
	}

	// STREAM INPUT PROCESSING ////////////////////////////////////////////////

	/**
	 * Process next incoming entry.
	 * Since the stream extends Actor, there is no more than one thread active.
	 */
	override protected act(StreamMessage entry, =>void done) {
		switch entry {
			Value<T>, Finish<T>, Error<T>: {
				queue.add(entry)
				publishNext
			}
			Entries<T>: {
				entry.entries.forEach [
					queue.add(it)
				]
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
					switch it: queue.peek {
						Finish<T> case level==0: skipping = false
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
	
	// STREAM PUBLISHING //////////////////////////////////////////////////////
	
	/** take an entry from the queue and pass it to the listener */
	def protected boolean publishNext() {
		if(listenerReady && entryListener != null && !queue.empty) {
			listenerReady = false
			val entry = queue.poll
			if(entry instanceof Finish<?>)
				if(entry.level == 0)
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
	
}
