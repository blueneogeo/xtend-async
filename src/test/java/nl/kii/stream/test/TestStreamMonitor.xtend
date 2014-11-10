package nl.kii.stream.test

import nl.kii.stream.StreamStats
import org.junit.Test

import static extension nl.kii.stream.StreamExtensions.*

class TestStreamMonitor {
	
	@Test
	def void testSimpleMonitoring() {
		
		val stats = new StreamStats;
		
		(1..10).stream
			.map [ it % 3 ]
			.map [ 1 / it ]
			.monitor(stats)
			.onError [ println(it) ]
			.onEach [ println(it) ]
		
		println(stats)
	}
	
}