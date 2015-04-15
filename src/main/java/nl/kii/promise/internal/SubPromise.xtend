package nl.kii.promise.internal

import nl.kii.promise.IPromise
import nl.kii.promise.Promise
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

class SubPromise<I, O> extends FixedBasePromise<I, O> {
	
	val protected IPromise<I, ?> root

	new() {
		this.root = null
	}
	
	/** Create a promise that was based on a parent value */
	new(I parentValue) {
		this(new Promise(parentValue))
	}


	/** Constructor for easily creating a child promise. Listenes for errors in the parent. */
	new(IPromise<I, ?> parentPromise) {
		this(parentPromise, true)
	}

	/** Constructor to allow control of error listening */	
	new(IPromise<I, ?> parentPromise, boolean listenForErrors) {
		this.root = parentPromise.root
		if(listenForErrors) this.root.on(Throwable) [ i, it | error(i, it) ]
	}

	override getRoot() { root }

	/** set the promised value */
	override set(I value) { root?.set(value) }

	/** report an error to the listener of the promise. */
	override error(Throwable t) { root?.error(t) this }
	
	/** set the promised value */
	def void set(I from, O value) { apply(new Value(from, value)) }

	def void error(I from, Throwable t) { apply(new Error(from, t)) }

}
