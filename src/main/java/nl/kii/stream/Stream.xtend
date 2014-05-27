package nl.kii.stream

import java.util.Queue
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static com.google.common.collect.Queues.*

class Stream<T> implements Procedure1<Entry<T>> {

	val Queue<Entry<T>> queue
	var open = true
	var skipping = false
	var listenerReady = false

	var (StreamCommand)=>void commandListener
	var (Entry<T>, =>void, =>void, =>void)=>void entryListener
	
	val =>void nextFn = [| perform(new Next) ]
	val =>void skipFn = [| perform(new Skip) ]
	val =>void closeFn = [| perform(new Close) ]
	
	new() {
		this.queue = newLinkedBlockingQueue
	}
	
	new(Queue<Entry<T>> queueFn) { 
		this.queue = queue
	}

	override apply(Entry<T> entry) {
		if(!open) return;
		queue.add(entry)
		publishNext
	}
	
	def apply(Entry<T>... entries) {
		if(!open) return;
		entries.forEach [ queue.add(it) ]
		publishNext
	}

	def synchronized perform(StreamCommand cmd) {
		if(!open) return;
		switch cmd {
			Next: {
				listenerReady = true
				// try to publish the next from the queue
				val published = publishNext
				// if nothing was published, notify there parent stream we need a next entry
				if(!published) notify(cmd)
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
				if(skipping) notify(cmd)
			}
			Close: {
				open = false
				notify(cmd)
			}
		}
	}
	
	/** take an entry from the queue and pass it to the listener */
	def protected boolean publishNext() {
		if(listenerReady && entryListener != null && !queue.empty) {
			listenerReady = false
			val entry = queue.poll
			if(entry instanceof Finish<?>)
				skipping = false
			try {
				entryListener.apply(entry, nextFn, skipFn, closeFn)
				true
			} catch(Throwable t) {
				if(entry instanceof Error<?>) throw t
				listenerReady = true
				apply(new Error(t))
				false
			}
		} else false
	}
	
	/** notify the commandlistener that we've performed a command */
	def protected notify(StreamCommand cmd) {
		if(commandListener != null)
			commandListener.apply(cmd)
	}
	
	/** 
	 * Lets you listen to the stream. 
	 * The listener passes you a value, as well as functions for calling 
	 * next, skip and close on the stream. For example:
	 * 
	 * <pre>
	 * val s = int.stream
	 * s.listener = [ it, next, skip, close |
	 *     switch it {
	 *         Value<Integer>: {
	 *              println('got value ' + value)
	 *              next.apply // we are ready for the next value, request it
	 *         }
	 *         Finish<Integer>: {
	 *              println('we are done, closing the stream')
	 *              close.apply
	 *         }
	 *     }
	 * ]
	 * </pre>
	 */
	def synchronized void setListener((Entry<T>, =>void, =>void, =>void)=>void entryListener) {
		if(this.entryListener != null)
			throw new StreamException('a stream can only have a single entry listener, one was already assigned.')
		this.entryListener = entryListener
	}
	
	def synchronized void setCmdListener((StreamCommand)=>void commandListener) {
		if(this.commandListener!= null)
			throw new StreamException('a stream can only have a single command listener, one was already assigned.')
		this.commandListener = commandListener
	}
	
	/** you're not getting the actual queue, just a list of what is in it */
	def synchronized getQueue() {
		newImmutableList(queue)
	}
	
	def synchronized isOpen() {
		open
	}
	
}
