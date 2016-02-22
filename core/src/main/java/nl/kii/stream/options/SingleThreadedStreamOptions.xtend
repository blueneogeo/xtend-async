package nl.kii.stream.options

import com.google.common.collect.Queues
import nl.kii.stream.queues.SingleThreadedSingleItemQueue
import nl.kii.util.annotations.NamedParams
import org.eclipse.xtend.lib.annotations.Accessors

class SingleThreadedStreamOptions implements StreamOptions {
	
	@Accessors int concurrency
	@Accessors boolean controlled
	@Accessors int maxQueueSize
	@Accessors String operation
	
	@NamedParams
	new(int concurrency, boolean controlled, int maxQueueSize, String operation) {
		this.concurrency = concurrency
		this.controlled = controlled
		this.maxQueueSize = maxQueueSize
		this.operation = operation
	}
	
	override <T> newPromiseActorQueue() {
		new SingleThreadedSingleItemQueue<T>
	}

	override <T> newActorQueue() {
		switch maxQueueSize {
			case 0, case 1: new SingleThreadedSingleItemQueue<T>
			default: Queues.newLinkedBlockingQueue
		}
	}

	override <T> newStreamQueue() {
		switch maxQueueSize {
			case 0, case 1: new SingleThreadedSingleItemQueue<T>
			default: Queues.newLinkedBlockingQueue
		}
	}

	override clone() throws CloneNotSupportedException {
		new ThreadSafeStreamOptions [
			it.concurrency = concurrency
			it.controlled = controlled
			it.maxQueueSize = maxQueueSize
			it.operation = operation
		]
	}
	
	override copy() {
		new ThreadSafeStreamOptions [
			it.concurrency = this.concurrency
			it.controlled = this.controlled
			it.maxQueueSize = this.maxQueueSize
			it.operation = this.operation
		]
	}
	
	override toString() '''
		SingleThreadedStreamOptions: {
			- concurrency: «concurrency»
			- expectingNext: «controlled»
			- maxQueueSize: «maxQueueSize»
			- operation: «operation»
		}
	'''
	
}
