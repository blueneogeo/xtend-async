package nl.kii.promise

import nl.kii.stream.message.Error
import nl.kii.stream.message.Value

class SubPromise<I, O> extends BasePromise<I, O> {
	
	// should be a val, but is a var to allow the BasePromise.xtend hack to set it after construction
//	var protected IPromise<I, ?> input

	/** Never use!only used to allow a hack in BasePromise.xtend to avoid an Xtend generics bug */
//	package new() {
//		this(null as IPromise<I, O>)
//	}

	/** Create a promise that was based on a parent value */
//	new(I parentValue) {
//		this(new Promise(parentValue))
//	}

	/** Constructor for easily creating a child promise. Listenes for errors in the parent. */
//	new(IPromise<I, ?> parentPromise) {
//		this(parentPromise, true)
//	}

	/** Constructor to allow control of error listening */	
//	new(IPromise<I, ?> parentPromise, boolean listenForErrors) {
////		this.input = parentPromise?.input
//		if(listenForErrors) this.input?.on(Throwable, true) [ i, it | error(i, it) ]
//	}

//	override getInput() { input }

//	/** set the promised value */
//	override set(I value) {
//		input?.set(value)
//	}
//
//	/** report an error to the listener of the promise. */
//	override error(Throwable t) {
//		input?.error(t) 
//	}
	
	/** set the promised value */
	def void set(I from, O value) { apply(new Value(from, value)) }

	def void error(I from, Throwable t) { apply(new Error(from, t)) }

}
