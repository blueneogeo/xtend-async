package nl.kii.stream

import java.util.Collection
import nl.kii.observe.Observable
import nl.kii.stream.message.Entry
import nl.kii.stream.message.StreamEvent
import nl.kii.stream.message.StreamMessage
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/**
 * A sequence of elements supporting sequential and parallel aggregate operations.
 * <p>
 * It has the following features:
 * <ul>
 * <li>threadsafe
 * <li>all operations (including extensions) are non-blocking
 * <li>supports asynchronous processing through flow control (next)
 * <li>it is queued. you can optionally provide your own queue
 * <li>only allows a single listener (use a StreamObserver to listen with multiple listeners)
 * <li>supports aggregation through batches (data is separated through finish entries)
 * <li>supports multiple levels of aggregation through multiple batch/finish levels
 * <li>wraps errors and lets you listen for them at the end of the stream chain
 * </ul>
 */
interface IStream<I, O> extends Procedure1<StreamMessage>, Observable<Entry<I, O>> {

	// PUSH DATA IN ///////////////////////////////////////////////////////////

	override apply(StreamMessage message)

	// CONTROL ////////////////////////////////////////////////////////////////

	def void next()
	def void skip()
	def void close()
	
	// LISTEN /////////////////////////////////////////////////////////////////
	
	override =>void onChange((Entry<I, O>)=>void observeFn)
	def =>void onNotify((StreamEvent)=>void notificationListener)

	// STATUS /////////////////////////////////////////////////////////////////
	
	def boolean isOpen()
	def boolean isReady()
	def boolean isSkipping()
	
	def Integer getConcurrency()
	def void setConcurrency(Integer concurrency)

	def int getBufferSize()
	def Collection<Entry<I, O>> getQueue()

	def void setOperation(String operationName)
	def String getOperation()
	
}


