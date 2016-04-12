package nl.kii.stream.message
import nl.kii.stream.message.StreamEvent
import org.eclipse.xtend.lib.annotations.Data

interface StreamEvent extends StreamMessage {
	
}

@Data
class Overflow<I> implements StreamEvent {
	
	/** The incoming object that caused the stream overflow */
	I from
	
}