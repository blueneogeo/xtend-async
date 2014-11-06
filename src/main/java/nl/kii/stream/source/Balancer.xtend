package nl.kii.stream.source

import nl.kii.stream.Close
import nl.kii.stream.Entry
import nl.kii.stream.Error
import nl.kii.stream.Finish
import nl.kii.stream.IStream
import nl.kii.stream.Next
import nl.kii.stream.Skip
import nl.kii.stream.Value
import nl.kii.stream.StreamEvent

/**
 * This splitter sends each message to the first stream that is ready.
 * This means that each attached stream receives different messages. 
 */
class LoadBalancer<I, O> extends StreamSplitter<I, O> {
	
	new(IStream<I, O> source) {
		super(source)
	}
	
	/** Handle an entry coming in from the source stream */
	protected override onEntry(Entry<I, O> entry) {
		switch entry {
			Value<I, O>: {
				for(stream : streams) {
					if(stream.ready) {
						stream.apply(entry)
						return
					}
				}
			}
			Finish<I, O>: {
				for(stream : streams) {
					stream.finish
				}
			}
			Error<I, O>: {
				for(stream : streams) {
					stream.error(entry.error)
				}
			}
		}
	}
	
	protected override onCommand(extension StreamEvent msg) {
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
