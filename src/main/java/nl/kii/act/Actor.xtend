package nl.kii.act

import java.util.Queue
import static java.util.concurrent.TimeUnit.*
import java.util.concurrent.locks.ReentrantLock
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static com.google.common.collect.Queues.*

/**
 * An Actor<T> is a threadsafe procedure that guarantees that the execution of the act method is
 * single threaded. To accomplish this, it has an inbox (which is a queue) which gathers all
 * incoming messages of type T. Calls from one or more threads to the apply function simply add to
 * this queue. The actor then uses a singlethreaded process loop to process these messages one by one.
 * The acting is implemented by extending this actor and implementing the act method.
 *  
 * <h3>Asynchronous act method</h3>
 * 
 * The act method of this actor is asynchronous. This means that you have to call done.apply to indicate
 * you are done processing. This allows the actor to signal when asynchronous work has been completed.
 * 
 * <h3>Thread borrowing</h3>
 * 
 * This actor is built to work well in asynchronous, single threaded, non blocking environments and is
 * built to be lightweight. As such it has no threadpool of its own for the process loop.
 * Instead, this actor implementation borrows the processing power of the tread that applies something
 * to perform its act loop. Once the loop is empty, it releases the thread. If while one thread is being 
 * borrowed and processing, another applies, that other thread is let go. This way, only one thread is ever
 * processing the act loop. This allows you to use the actor as a single-threaded yet threadsafe worker.
 * 
 * <h3>Usage:</h3>
 * 
 * <pre>
 * // anonymous class version
 * val Actor<String> greeter = new Actor() {
 *     def act(String message, =>void done) {
 *         println('hello ' + message + '!')
 *         done.apply // signal we are done processing the message
 *     }
 * }
 * greeter.apply('world')
 * <p>
 * // lambda version
 * val Actor<String> greeter = [ String message, done |
 *     println('hello ' + message + '!')
 *     done.apply
 * ]
 * greeter.apply('world')
 * <p>
 * // using import static extension nl.kii.actor.ActorExtensions
 * val greeter = actor [ println('hello' + it) ]
 * 'world' >> greeter
 * </pre>
 */
abstract class Actor<T> implements Procedure1<T> {
	
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
	protected abstract def void act(T message, =>void done)

	/** 
	 * Start the process loop that takes input from the inbox queue one by one and calls the
	 * act method for each input.
	 */
	protected def void process() {
		// Only a single thread may run the process
		// The try period of the lock is to compensate for the small period in between finishing a process
		// loop and the unlocking and relocking.
		if(processLock.tryLock(1, MILLISECONDS)) {
			// get the next item from the inbox
			val message = inbox.poll
			if(message != null) {
				// perform the act on the item, and wait for the asynchronous closure call
				try {
					act(message, onProcessDone) // onProcessDone is run by the thread that called it
				} catch(Throwable t) {
					onProcessDone.apply
					throw t
				}
			} else {
				// nothing more in the inbox, we're done, unlock the process
				processLock.unlock
			}
		}
	}
	
	// TODO: probably this could be done smarter, currently the process gets unlocked, the inbox checked
	// and then process called again which locks again. This could be what requires the trylock in process(),
	// since there is a short time in which things are unlocked, performed and then locked again.
	// also, it is unnecessary to unlock when we know we will lock again.
	val private =>void onProcessDone = [|
		// we're done, unlock the process
		processLock.unlock
		// recursively call process again if there is more to do
		if(!inbox.empty) process
	]
	
	def getInbox() {
		inbox.unmodifiableView
	}
	
}

