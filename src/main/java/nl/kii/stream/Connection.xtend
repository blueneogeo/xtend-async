package nl.kii.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

abstract class Connection<T> implements Procedure1<T> {
	
	// set by the stream that is being connected to
	protected Stream<T> stream
	protected =>void openFn
	protected =>void nextFn
	protected =>void skipFn
	protected =>void closeFn
	
	// control of the stream
	def open() { openFn.apply }
	def next() { nextFn.apply }
	def skip() { skipFn.apply }
	def close() { closeFn.apply }
	
	// listening
	override abstract void apply(T entry)
	
}
