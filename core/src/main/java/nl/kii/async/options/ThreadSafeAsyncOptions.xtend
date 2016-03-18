package nl.kii.async.options

import java.util.concurrent.ConcurrentLinkedQueue
import nl.kii.stream.queues.ThreadSafeSingleItemQueue
import nl.kii.util.annotations.NamedParams
import org.eclipse.xtend.lib.annotations.Accessors
import nl.kii.util.annotations.*

@Accessors
class ThreadSafeAsyncOptions implements AsyncOptions {
	
	int concurrency
	boolean controlled
	int maxQueueSize
	String operation
	int actorMaxCallDepth
	
	@NamedParams
	new(@I(0) int concurrency, @B(true) boolean controlled, @I(1000) int maxQueueSize, @Null String operation, @I(100) int actorMaxCallDepth) {
		this.concurrency = concurrency
		this.controlled = controlled
		this.maxQueueSize = maxQueueSize
		this.operation = operation
		this.actorMaxCallDepth = actorMaxCallDepth
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
		copy
	}
	
	override copy() {
		new ThreadSafeAsyncOptions [
			it.concurrency = this.concurrency
			it.controlled = this.controlled
			it.maxQueueSize = this.maxQueueSize
			it.operation = this.operation
			it.actorMaxCallDepth = this.actorMaxCallDepth
		]
	}
	
	override toString() '''
		ThreadSafeStreamOptions: {
			- concurrency: «concurrency»
			- expectingNext: «controlled»
			- maxQueueSize: «maxQueueSize»
			- operation: «operation»
			- actorMaxCallDepth: «actorMaxCallDepth»
		}
	'''
	
}
