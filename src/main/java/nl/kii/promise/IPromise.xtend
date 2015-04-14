package nl.kii.promise

import nl.kii.stream.message.Entry
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

interface IPromise<R, T> extends Procedure1<Entry<R, T>> {
	
	def IPromise<R, ?> getRoot()
	
	def Boolean getFulfilled()
	def Entry<R, T> get()
	def void set(R value)
	def IPromise<R, T> error(Throwable t)
	
//	def IPromise<R, T> onError(Procedure1<Throwable> errorFn)
//	def IPromise<R, T> onError(Procedure2<R, Throwable> errorFn)

	def IPromise<R, T> on(Class<? extends Throwable> exceptionType, Procedure1<Throwable> errorFn)
	def IPromise<R, T> on(Class<? extends Throwable> exceptionType, Procedure2<R, Throwable> errorFn)

	def Task then(Procedure1<T> valueFn)
	def Task then(Procedure2<R, T> valueFn)
	
	def void setOperation(String operation)
	def String getOperation()
	
}
