package nl.kii.promise

import nl.kii.stream.message.Entry
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

interface IPromise<I, O> extends Procedure1<Entry<I, O>> {
	
	def IPromise<I, ?> getInput()
	
	def Boolean getFulfilled()
	def Entry<I, O> get()
	def void set(I value)
	def IPromise<I, O> error(Throwable t)
	
	def IPromise<I, O> on(Class<? extends Throwable> exceptionType, boolean swallow, Procedure2<I, Throwable> errorFn)

	def Task then(Procedure1<O> valueFn)
	def Task then(Procedure2<I, O> valueFn)

	def void setOperation(String operation)
	def String getOperation()
	
}
