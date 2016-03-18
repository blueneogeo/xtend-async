package nl.kii.async.options

import java.util.Queue

/** 
 * Options for the Async library. 
 * <p>
 * These are centralised for Actors, Promises and Streams, because streams and promises can 
 * be chained, and will pass these options down the chain. Since Streams and Promises and Tasks 
 * are Actors and uses Actors and queues, the actor settings are part of these options as well.
 * <p>
 * You can set the default options to use in the AsyncDefault.options property. However you can
 * also set up stream and promise chains with specific options.
 */
interface AsyncOptions {

	// SETTINGS

	/** The defailt concurrency to use for processing a stream asynchronously. */
	def int getConcurrency()
	/** The defailt concurrency to use for processing a stream asynchronously. */
	def void setConcurrency(int concurrency)
	
	/** Controlled streams do not push unless asked for the next value. */
	def boolean isControlled()
	/** Controlled streams do not push unless asked for the next value. */
	def void setControlled(boolean isControlled)
	
	/** The maximum size of the queue being used, in number of entries kept. */
	def int getMaxQueueSize()
	/** The maximum size of the queue being used, in number of entries kept. */
	def void setMaxQueueSize(int maxSize)
	
	/** Get the name of the current operation performed by the stream */
	def String getOperation()
	/** Set the name of the current operation performed by the stream */
	def void setOperation(String operationDesciption)

	/** Set how many steps deep an actor will go until it breaks out of the stacktrace */
	def int getActorMaxCallDepth()

	// FACTORIES

	/** Create a new queue for streams actor queues. */	
	def <T> Queue<T> newActorQueue()
	/** Create a new queue for streams actor queues. */	
	def <T> Queue<T> newPromiseActorQueue()
	/** Create a new queue for streams entries. */	
	def <T> Queue<T> newStreamQueue()

	// OTHER

	/** Make a copy of these StreamOptions */
	def AsyncOptions copy()

}
