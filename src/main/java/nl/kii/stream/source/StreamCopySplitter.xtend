package nl.kii.stream.source

import nl.kii.async.annotation.Atomic
import nl.kii.stream.IStream
import nl.kii.stream.message.Close
import nl.kii.stream.message.Entry
import nl.kii.stream.message.Next
import nl.kii.stream.message.Skip
import nl.kii.stream.message.StreamEvent

/**
 * This splitter simply tries to pass all incoming values
 * from the source stream to the piped streams.
 * <p>
 * Flow control is maintained by only allowing the next
 * entry from the source stream if all piped streams are ready.
 * <p>
 * Note: This means that you need to make sure that all connected
 * streams do not block their flow, since one blocking stream
 * will block all streams from flowing.
 */
class StreamCopySplitter<I, O> extends StreamSplitter<I, O> {
	
	@Atomic Entry<I, O> buffer
	
	new(IStream<I, O> source) {
		super(source)
	}
	
	/** Handle an entry coming in from the source stream */
	protected override onEntry(Entry<I, O> entry) {
		buffer = entry
		// only proceed if all streams are ready
		if(streams.all[ready]) publish
	}
	
	protected override onCommand(extension StreamEvent msg) {
		switch msg {
			Next: next
			Skip: skip
			Close: close
		}
	}
	
	protected def publish() {
		if(buffer != null) {
			for(s : streams) s.apply(buffer)
			buffer = null
		}
	}
	
	protected def next() {
		if(!streams.all[ready]) return;
		source.next
		publish
	}
	
	protected def skip() {
		if(!streams.all[skipping]) return;
		publish
		source.skip
	}
	
	protected def close() {
		if(!streams.all[!open]) return;
		publish
		source.close
	}
	
}
