package nl.kii.stream

import java.util.Queue
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static com.google.common.collect.Queues.*
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.atomic.AtomicBoolean

class StreamOLD<T> implements Procedure1<Entry<T>> {

	val Queue<Entry<T>> queue
	val open = new AtomicBoolean(true)
	val skipping = new AtomicBoolean(false)
	val listenerReady = new AtomicBoolean(false)

	var (StreamCommand)=>void commandListener
	var (Entry<T>, =>void, =>void, =>void)=>void entryListener
	
	val =>void nextFn = [| perform(new Next) ]
	val =>void skipFn = [| perform(new Skip) ]
	val =>void closeFn = [| perform(new Close) ]
	
	val inputLock = new ReentrantLock
	val outputLock = new ReentrantLock
	val controlLock = new ReentrantLock
	
	new() {
		this.queue = newLinkedBlockingQueue
	}
	
	new(Queue<Entry<T>> queueFn) { 
		this.queue = queue
	}

	override apply(Entry<T> entry) {
		if(!open.get) return;
		inputLock.lock
		queue.add(entry)
		inputLock.unlock
		publishNext
	}
	
	def apply(Entry<T>... entries) {
		if(!open.get) return;
		inputLock.lock
		entries.forEach [ queue.add(it) ]
		inputLock.unlock
		publishNext
	}

	def perform(StreamCommand cmd) {
		if(!open.get) return;
		try {
			controlLock.lock
			switch cmd {
				Next: {
					listenerReady.set(true)
					// try to publish the next from the queue
					val published = publishNext
					// if nothing was published, notify there parent stream we need a next entry
					if(!published) notify(cmd)
				}
				Skip: {
					if(skipping.get) return 
					else skipping.set(true)		
					// discard everything up to finish from the queue
					while(skipping.get && !queue.empty) {
						switch queue.peek {
							Finish<T>: skipping.set(false)
							default: queue.poll
						}
					}
					// if we are still skipping, notify the parent stream it needs to skip
					if(skipping.get) notify(cmd)
				}
				Close: {
					open.set(false)
					notify(cmd)
				}
			}
		} finally {
			controlLock.unlock
		}
	}
	
	/** take an entry from the queue and pass it to the listener */
	def protected boolean publishNext() {
		try {
			outputLock.tryLock
			if(listenerReady.get && entryListener != null && !queue.empty) {
				listenerReady.set(false)
				val entry = queue.poll
				if(entry instanceof Finish<?>)
					skipping.set(false)
				try {
					entryListener.apply(entry, nextFn, skipFn, closeFn)
					true
				} catch(Throwable t) {
					if(entry instanceof Error<?>) throw t
					listenerReady.set(true)
					apply(new Error(t))
					false
				}
			} else false
		} finally {
			outputLock.unlock
		}
	}
	
	/** notify the commandlistener that we've performed a command */
	def protected notify(StreamCommand cmd) {
		if(commandListener != null)
			commandListener.apply(cmd)
	}
	
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
		open.get
	}
	
}


