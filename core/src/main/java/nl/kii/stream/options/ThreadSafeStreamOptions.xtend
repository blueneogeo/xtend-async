package nl.kii.stream.options

import java.util.concurrent.ConcurrentLinkedQueue
import nl.kii.stream.queues.ThreadSafeSingleItemQueue
import nl.kii.util.annotations.NamedParams
import org.eclipse.xtend.lib.annotations.Accessors

class ThreadSafeStreamOptions implements StreamOptions {
	
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
		new ThreadSafeSingleItemQueue<T>
	}

	override <T> newActorQueue() {
		switch maxQueueSize {
			case 0, case 1: new ThreadSafeSingleItemQueue<T>
			default: new ConcurrentLinkedQueue<T>
		}
	}

	override <T> newStreamQueue() {
		switch maxQueueSize {
			case 0, case 1: new ThreadSafeSingleItemQueue<T>
			default: new ConcurrentLinkedQueue<T>
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
		ThreadSafeStreamOptions: {
			- concurrency: «concurrency»
			- expectingNext: «controlled»
			- maxQueueSize: «maxQueueSize»
			- operation: «operation»
		}
	'''
	
}
