package nl.kii.stream.test
import static extension nl.kii.stream.StreamExtensions.*
import org.junit.Test
import nl.kii.stream.StreamMonitor

class TestStreamMonitor {
	
	@Test
	def void testSimpleMonitoring() {
		
		val monitor = new StreamMonitor;
		
		(1..10).stream
			.map [ it % 3 ]
			.map [ 1 / it ]
			.monitor(monitor)
			.onError [ println(it) ]
			.onEach [ println(it) ]
		
		println(monitor.valueCount)
	}
	
}