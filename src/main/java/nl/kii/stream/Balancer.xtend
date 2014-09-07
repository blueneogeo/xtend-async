package nl.kii.stream

/**
 * This splitter sends each message to the first stream that is ready.
 * This means that each attached stream receives different messages. 
 */
class LoadBalancer<T> extends StreamSplitter<T> {
	
	new(Stream<T> source) {
		super(source)
	}
	
	/** Handle an entry coming in from the source stream */
	protected override onEntry(Entry<T> entry) {
		switch entry {
			Value<T>: {
				for(stream : streams) {
					if(stream.ready) {
						stream.apply(entry)
						return
					}
				}
			}
			Finish<T>: {
				for(stream : streams) {
					stream.finish
				}
			}
			Error<T>: {
				for(stream : streams) {
					stream.error(entry.error)
				}
			}
		}
	}
	
	protected override onCommand(extension StreamCommand msg) {
		switch msg {
			Next: next
			Skip: skip
			Close: close
		}
	}
	
	protected def next() {
		source.next
	}
	
	protected def skip() {
		if(!streams.all[skipping]) return;
		source.skip
	}
	
	protected def close() {
		if(!streams.all[!open]) return;
		source.close
	}
	
}
