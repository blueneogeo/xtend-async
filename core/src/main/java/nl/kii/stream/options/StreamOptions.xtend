package nl.kii.stream.options

import java.util.Queue

interface StreamOptions {

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

	/** Create a new queue for streams actor queues. */	
	def <T> Queue<T> newActorQueue()
	/** Create a new queue for streams actor queues. */	
	def <T> Queue<T> newPromiseActorQueue()
	/** Create a new queue for streams entries. */	
	def <T> Queue<T> newStreamQueue()
	/** Make a copy of these StreamOptions */
	def StreamOptions copy()

}
