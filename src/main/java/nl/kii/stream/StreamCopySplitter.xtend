package nl.kii.stream

/**
 * This splitter simply tries to pass all incoming values
 * from the source stream to the piped streams.
 * <p>
 * Flow control is maintained by only allowing the next
 * entry from the source stream if all piped streams are ready.
 * This means that you need to make sure that all connected
 * streams do not block their flow, since one blocking stream
 * will block all streams from flowing.
 */
class StreamCopySplitter<T> extends StreamSplitter<T> {
	
	new(Stream<T> source) {
		super(source)
	}
	
	/** Handle an entry coming in from the source stream */
	protected override onEntry(Entry<T> entry) {
		// only proceed if all streams are ready
		if(!streams.all[ready]) return;
		// all streams are ready, push it
		for(it : streams) apply(entry)
	}
	
	protected override onCommand(extension StreamCommand msg) {
		switch msg {
			Next: next
			Skip: skip
			Close: close
		}
	}
	
	protected def next() {
		println(streams)
		if(!streams.all[ready]) return;
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
