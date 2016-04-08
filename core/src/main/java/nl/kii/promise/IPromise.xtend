package nl.kii.promise

import nl.kii.observe.Observable
import nl.kii.stream.message.Entry
import nl.kii.util.Opt
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2
import nl.kii.async.options.AsyncOptions

interface IPromise<I, O> extends Procedure1<Entry<I, O>>, Observable<Entry<I, O>> {

	def Opt<Entry<I, O>> get()
	def boolean getFulfilled()

	def IPromise<I, O> then(Procedure1<O> valueFn)
	def IPromise<I, O> then(Procedure2<I, O> valueFn)

	def <T extends Throwable> IPromise<I, O> on(Class<T> exceptionType, (T)=>void errorFn)
	def <T extends Throwable> IPromise<I, O> on(Class<T> exceptionType, boolean swallow, (I, T)=>void errorFn)

	def AsyncOptions getOptions()
	
}
