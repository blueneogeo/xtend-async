package nl.kii.async.options

import com.google.common.collect.Queues
import nl.kii.stream.queues.SingleThreadedSingleItemQueue
import nl.kii.util.annotations.B
import nl.kii.util.annotations.I
import nl.kii.util.annotations.NamedParams
import nl.kii.util.annotations.Null
import org.eclipse.xtend.lib.annotations.Accessors

@Accessors
class SingleThreadedAsyncOptions implements AsyncOptions {
	
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
		new SingleThreadedSingleItemQueue<T>
	}

	override <T> newActorQueue() {
		switch maxQueueSize {
			case 0, case 1: new SingleThreadedSingleItemQueue<T>
			default: Queues.newArrayDeque
		}
	}

	override <T> newStreamQueue() {
		switch maxQueueSize {
			case 0, case 1: new SingleThreadedSingleItemQueue<T>
			default: Queues.newArrayDeque
		}
	}

	override clone() throws CloneNotSupportedException {
		copy
	}
	
	override copy() {
		new SingleThreadedAsyncOptions [
			it.concurrency = this.concurrency
			it.controlled = this.controlled
			it.maxQueueSize = this.maxQueueSize
			it.operation = this.operation
			it.actorMaxCallDepth = actorMaxCallDepth
		]
	}
	
	override toString() '''
		SingleThreadedStreamOptions: {
			- concurrency: «concurrency»
			- expectingNext: «controlled»
			- maxQueueSize: «maxQueueSize»
			- actorMaxCallDepth: «actorMaxCallDepth»
			- operation: «operation»
		}
	'''
	
}
