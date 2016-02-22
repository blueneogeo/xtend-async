package nl.kii.act

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * An Actor is a computational unit that processes incoming messages, spawn other actors,
 * send messages to other actors and code, and that can contain its own state.
 * <p>
 * http://en.wikipedia.org/wiki/Actor_model
 * <p>
 * In Java an Actor<T> is a threadsafe procedure that guarantees that the execution of the act method is
 * single threaded. To accomplish this, it has an inbox (which is a queue) which gathers all
 * incoming messages of type T. Calls from one or more threads to the apply function simply add to
 * this queue. The actor then uses a singlethreaded process loop to process these messages one by one.
 * The acting is implemented by extending this actor and implementing the act method.
 * 
 * <h3>Asynchronous act method</h3>
 * 
 * The act method of this actor is asynchronous. This means that you have to call done.apply to indicate
 * you are done processing. This tells the actor that the asynchronous work has been completed.
 */
interface AsyncActor<T> extends Procedure1<T> {

	/** Give the actor something to process */
	override apply(T message)
	
	/** 
	 * Perform the actor on the next message in the inbox. 
	 * You must call done.apply when you have completed processing!
	 */
	def void act(T message, =>void done)
	
}
