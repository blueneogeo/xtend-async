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
			.effect [ println(it) ]
			.on(Exception) [ println(it) ]
			.start
		
		println(stats)
	}
	
}