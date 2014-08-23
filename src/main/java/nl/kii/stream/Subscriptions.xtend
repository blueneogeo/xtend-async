package nl.kii.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import nl.kii.async.annotation.Async
import nl.kii.promise.Task

class Subscription<T> implements Procedure1<Entry<T>> {
	
	val protected Stream<T> stream
	protected Task task
	protected (Entry<T>)=>void onEntryFn
	protected (T)=>void onValueFn
	protected (Throwable)=>void onErrorFn
	protected =>void onFinish0Fn
	protected (Finish<T>)=>void onFinishFn
	protected =>void onClosedFn
	
	new(Stream<T> stream) {
		this.stream = stream
		stream.onChange [ apply ]
	}

	override apply(Entry<T> it) {
		onEntryFn?.apply(it)
		switch it {
			Value<T>: onValueFn?.apply(value)
			Error<T>: {
				onErrorFn?.apply(error)
				task?.error(error)
			}
			Finish<T>: {
				onFinishFn?.apply(it)
				if(level == 0)
					onFinish0Fn?.apply
				task?.complete
			}
			Closed<T>: onClosedFn?.apply
		}
	}
	
	def getStream() {
		stream
	}
	
	def entry((Entry<T>)=>void onEntryFn) {
		this.onEntryFn = onEntryFn
	}

	def each((T)=>void onValueFn) {
		this.onValueFn = onValueFn
	}
	
	/** listen for a finish (of level 0) */
	def finish(=>void onFinish0Fn) {
		this.onFinish0Fn = onFinish0Fn
	}
	
	/** listen for any finish */
	def finish((Finish<T>)=>void onFinishFn) {
		this.onFinishFn = onFinishFn
	}

	def error((Throwable)=>void onErrorFn) {
		this.onErrorFn = onErrorFn
	}
	
	def closed(=>void onClosedFn) {
		this.onClosedFn = onClosedFn
	}
	
	@Async def toTask(Task task) {
		this.task = task
	}
		
}

class CommandSubscription implements Procedure1<StreamCommand> {
	
	val Stream<?> stream
	var (Void)=>void onNextFn
	var (Void)=>void onSkipFn
	var (Void)=>void onCloseFn
	
	new(Stream<?> stream) {
		this.stream = stream
		stream.onNotification[ apply ]
	}

	override apply(StreamCommand it) {
		switch it {
			Next: onNextFn?.apply(null)
			Skip: onSkipFn?.apply(null)
			Close: onCloseFn?.apply(null)
		}
	}

	def onNext((Void)=>void onNextFn) {
		this.onNextFn = onNextFn
	}
	
	def onSkip((Void)=>void onSkipFn) {
		this.onSkipFn = onSkipFn
	}

	def onClose((Void)=>void onCloseFn) {
		this.onCloseFn = onCloseFn
	}
	
}
