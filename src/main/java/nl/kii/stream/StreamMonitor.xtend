package nl.kii.stream

import nl.kii.async.annotation.Atomic
import nl.kii.observe.Observable
import nl.kii.observe.Publisher
import nl.kii.stream.internal.StreamObserver

@SuppressWarnings("unchecked")
class StreamMonitor implements StreamObserver, Observable<StreamMonitor> {

	val publisher = new Publisher<StreamMonitor>
	
	@Atomic public boolean active
	@Atomic public IStream<?, ?> stream
	// counts
	
	@Atomic public long valueCount
	@Atomic public long finishCount
	@Atomic public long errorCount

	// moments
	
	@Atomic public long startTS
	@Atomic public long closeTS
	
	@Atomic public long firstEntryTS
	@Atomic public long firstValueTS
	@Atomic public long firstErrorTS
	@Atomic public long firstFinishTS

	@Atomic public long lastEntryTS
	@Atomic public long lastValueTS
	@Atomic public long lastFinishTS
	@Atomic public long lastErrorTS

	// values

	@Atomic public Object lastValue
	@Atomic public Throwable lastError

	def setPublishing(boolean isOn) {
		publisher.publishing = isOn
	}
	
	override onValue(Object from, Object value) {
		println('yo!')
		if(!active) return;
		if(firstEntryTS == null) firstEntryTS = now
		if(firstValueTS == null) firstValueTS = now
		lastEntryTS = now
		lastValueTS = now
		lastValue = value
		incValueCount
		publisher.apply(this)
	}
	
	override onError(Object from, Throwable t) {
		if(!active) return;
		if(firstEntryTS == null) firstEntryTS = now
		if(firstErrorTS == null) firstErrorTS = now
		lastEntryTS = now
		lastErrorTS = now
		lastError = t
		incErrorCount
		publisher.apply(this)
	}
	
	override onFinish(Object from, int level) {
		if(!active) return;
		if(firstEntryTS == null) firstEntryTS = now
		if(firstFinishTS == null) firstFinishTS = now
		lastEntryTS = now
		lastFinishTS = now
		incFinishCount
		publisher.apply(this)
	}
	
	override onClosed() {
		if(!active) return;
		lastEntryTS = now
		closeTS = now
		publisher.apply(this)
	}
	
	override onChange((StreamMonitor)=>void observeFn) {
		publisher.onChange(observeFn)
	}

	private def now() { System.currentTimeMillis }
	
}
