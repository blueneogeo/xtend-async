package nl.kii.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

abstract class Subscription<T> implements Procedure1<Entry<T>> {
	
	val protected Stream<T> stream
	var (T)=>void onValueFn
	var (Throwable)=>void onErrorFn
	var (Void)=>void onFinishFn
	
	new(Stream<T> stream) {
		this.stream = stream
		stream.onEntry [ apply ]
	}

	override apply(Entry<T> it) {
		switch it {
			Value<T>: onValueFn?.apply(value)
			Error<T>: onErrorFn?.apply(error)
			Finish<T>: onFinishFn?.apply(null)
		}
	}

	def each((T)=>void onValueFn) {
		this.onValueFn = onValueFn
	}
	
	def finish((Void)=>void onFinishFn) {
		this.onFinishFn = onFinishFn
	}

	def error((Throwable)=>void onErrorFn) {
		this.onErrorFn = onErrorFn
	}
		
	def close() {
		stream.close
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
			stream.next
		}
	}

}

class AsyncSubscription<T> extends Subscription<T> {
	
	new(Stream<T> stream) {
		super(stream)
	}
	
	def next() {
		stream.next
	}
	
	def skip() {
		stream.skip
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
