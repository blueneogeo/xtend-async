package nl.kii.act

import java.util.Queue
import static java.util.concurrent.TimeUnit.*
import java.util.concurrent.locks.ReentrantLock
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static com.google.common.collect.Queues.*

abstract class LoopingActor<T> implements Procedure1<T> {
	
	val Queue<T> inbox
	val processLock = new ReentrantLock
	//val processing = new AtomicBoolean(false)
	
	/** Create a new actor with a concurrentlinkedqueue as inbox */
	new() {	
		this(newConcurrentLinkedQueue)
	}

	/** 
	 * Create an actor with the given queue as inbox. 
	 * Queue implementation must be threadsafe and non-blocking.
	 */
	new(Queue<T> queue) { 
		inbox = queue
	}
	
	/** Give the actor something to process */
	override apply(T message) {
		inbox.add(message)
		process // let the applying thread do some work!
	}
	
	/** 
	 * Perform the actor on the next message in the inbox. 
	 * You must call done.apply when you have completed processing!
	 */
	protected abstract def void act(T message)

	/** 
	 * Start the process loop that takes input from the inbox queue one by one and calls the
	 * act method for each input.
	 */
	protected def void process() {
		// Only a single thread may run the process
		// The try period of the lock is to compensate for the small period in between finishing a process
		// loop and the unlocking and relocking.
		if(processLock.tryLock(1, MILLISECONDS)) {
			try {
				while(!inbox.empty) {
					val message = inbox.poll
					act(message)
				}
			} finally {
				processLock.unlock
			}
		}
	}
	
	def getInbox() {
		inbox.unmodifiableView
	}
	
}
