package nl.kii.promise

import nl.kii.observe.Observable
import nl.kii.stream.message.Entry
import nl.kii.stream.options.StreamOptions
import nl.kii.util.Opt
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

interface IPromise<I, O> extends Procedure1<Entry<I, O>>, Observable<Entry<I, O>> {

	def Opt<Entry<I, O>> get()
	def boolean getFulfilled()

	def SubPromise<I, O> then(Procedure1<O> valueFn)
	def SubPromise<I, O> then(Procedure2<I, O> valueFn)

	def <T extends Throwable> SubPromise<I, O> on(Class<T> exceptionType, boolean swallow, (T)=>void errorFn)
	def <T extends Throwable> SubPromise<I, O> on(Class<T> exceptionType, boolean swallow, (I, T)=>void errorFn)

	def StreamOptions getOptions()
	
}
