package nl.kii.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

abstract class Subscription<T> implements Procedure1<Entry<T>> {
	
	val protected Stream<T> stream
	var (T)=>void onValueFn
	var (Throwable)=>void onErrorFn
	var (Void)=>void onFinishFn
	
	new(Stream<T> stream) {
		this.stream = stream
		stream.listener = [ it, next, skip, close |	apply ]
	}

	override apply(Entry<T> it) {
		switch it {
			Value<T>: onValueFn?.apply(value)
			Error<T>: onErrorFn?.apply(error)
			Finish<T>: onFinishFn?.apply(null)
		}
	}

	def forEach((T)=>void onValueFn) {
		this.onValueFn = onValueFn
	}
	
	def onFinish((Void)=>void onFinishFn) {
		this.onFinishFn = onFinishFn
	}

	def onError((Throwable)=>void onErrorFn) {
		this.onErrorFn = onErrorFn
	}
		
	def close() {
		stream.perform(new Close)
	}
	
}

class SyncSubscription<T> extends Subscription<T> {
	
	new(Stream<T> stream) {
		super(stream)
	}
	
	override apply(Entry<T> it) {
		try {
			super.apply(it)
		} finally {
			stream.perform(new Next)
		}
	}

}

class AsyncSubscription<T> extends Subscription<T> {
	
	new(Stream<T> stream) {
		super(stream)
	}
	
	def next() {
		stream.perform(new Next)
	}
	
	def skip() {
		stream.perform(new Skip)
	}
	
}

interface StreamCommand { }
class Next implements StreamCommand { }
class Skip implements StreamCommand { }
class Close implements StreamCommand { }

class CommandSubscription<T> implements Procedure1<StreamCommand> {
	
	val Stream<T> stream
	var (Void)=>void onNextFn
	var (Void)=>void onSkipFn
	var (Void)=>void onCloseFn
	
	new(Stream<T> stream) {
		this.stream = stream
		stream.cmdListener = [ apply ]
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
