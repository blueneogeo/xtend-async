package nl.kii.stream

import nl.kii.async.annotation.Atomic
import nl.kii.promise.Task
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class Subscription<T> implements Procedure1<Entry<T>> {
	
	val protected Stream<T> stream
	
	val task = new Task
	@Atomic Procedure1<Entry<T>> onEntryFn
	@Atomic Procedure1<T> onValueFn
	@Atomic Procedure1<Throwable> onErrorFn
	@Atomic Procedure0 onFinish0Fn
	@Atomic Procedure1<Finish<T>> onFinishFn
	@Atomic Procedure0 onClosedFn
	
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
				task.error(error)
			}
			Finish<T>: {
				onFinishFn?.apply(it)
				if(level == 0)
					onFinish0Fn?.apply
				task.complete
			}
			Closed<T>: {
				onClosedFn?.apply
				task.complete
			}
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
	
	def Task toTask() {
		task
	}
		
}

class CommandSubscription implements Procedure1<StreamCommand> {
	
	val Stream<?> stream
	
	@Atomic Procedure1<Void> onNextFn
	@Atomic Procedure1<Void> onSkipFn
	@Atomic Procedure1<Void> onCloseFn
	
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
