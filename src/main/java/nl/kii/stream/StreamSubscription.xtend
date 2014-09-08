package nl.kii.stream

import nl.kii.async.annotation.Atomic
import nl.kii.promise.Task
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

interface StreamObserver<T> extends Procedure1<Entry<T>> {
	
	/** handle each incoming value */
	def void each((T)=>void handler)
	
	/** handle each incoming error */
	def void error((Throwable)=>void handler)
	
	/** handle each incoming finish */
	def void finish((Finish<T>)=>void handler)
	
	/** handled that the stream has closed */
	def void closed((Void)=>void stream)
	
	def Stream<T> getStream()
	def Task toTask()
	
}

/**
 * A builder for stream listening. Combine with StreamExtensions.on like this:
 * <p>
 * <pre>
 * stream.on [
 *    each [ ... stream.next ]
 *    finish [ ... ]
 *    error [ ... ]
 * ]
 * stream.next
 */
class StreamSubscription<T> implements StreamObserver<T> {
	
	val protected Stream<T> stream
	
	val task = new Task
	@Atomic Procedure1<T> valueFn
	@Atomic Procedure1<Throwable> errorFn
	@Atomic Procedure0 finish0Fn
	@Atomic Procedure1<Finish<T>> finishFn
	@Atomic Procedure1<Void> closedFn
	
	new(Stream<T> stream) {
		this.stream = stream
		// start listening to the stream
		stream.onChange [ apply ]
	}

	/** process each entry from the stream */
	override apply(Entry<T> entry) {
		switch it : entry {
			
			Value<T>: try {
				valueFn?.apply(value)
			} catch(Throwable t) {
				stream.error(new StreamException('stream.on.each handler while processing', entry, t))
				stream.next
			}
			
			Finish<T>: try {
				finishFn?.apply(it)
				if(level == 0) finish0Fn?.apply
				task.complete
			} catch(Throwable t) {
				stream.error(new StreamException('stream.on.finish handler while processing', entry, t))
				stream.next
			}

			Error<T>: try {
				errorFn?.apply(error)
			} catch(Throwable t) {
				task.error(new StreamException('stream.on.error handler while processing', entry, t))
				stream.next
			} finally {
				task.error(error)
			}

			Closed<T>: try {
				closedFn?.apply(null)
				task.complete
			} catch(Throwable t) {
				stream.error(new StreamException('stream.on.closed handler while processing', entry, t))
			}
		}
	}
	
	/** listen for each incoming value */
	override each((T)=>void handler) {
		this.valueFn = handler
	}
	
	/** listen for a finish of level 0 */
	def finish(=>void handler) {
		this.finish0Fn = handler
	}
	
	/** listen for any finish */
	override finish((Finish<T>)=>void handler) {
		this.finishFn = handler
	}

	/** listen for any uncaught errors */
	override error((Throwable)=>void handler) {
		this.errorFn = handler
	}
	
	/** listen for when the stream closes */
	override closed((Void)=>void handler) {
		this.closedFn = handler
	}
	
	/** get the stream the subscription is subscribed to */
	override getStream() {
		stream
	}
	
	/** get the result of the subscription  */
	override Task toTask() {
		task
	}
	
}
