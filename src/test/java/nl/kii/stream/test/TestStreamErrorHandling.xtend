package nl.kii.stream.test
import static org.junit.Assert.*
import nl.kii.async.annotation.Atomic
import org.junit.Test

import static extension nl.kii.stream.StreamExtensions.*

class TestStreamErrorHandling {

	@Test
	def void testNoHandlingShouldTriggerException() {
		try {
			val s = int.stream
			s
				.map [ it ]
				.filter [ 1 / (it % 2) == 0 ] // division by 0 for 2
				.map [ it ]
				.onEach [ println(it) ]
			s << 1 << 2 << finish
			fail('we expected an error for /0')
		} catch(Exception e) {
			e.printStackTrace
			// success
		}
	}

	@Test
	def void testIteratorErrorHandlingShouldCatchException() {
		try {
			val s = (1..20).stream
			s
				//.map [ it ]
				.filter [ 1 / (it % 3) == 0 ] // division by 0 for 3
				//.map [ it ]
				.onError [ printStackTrace ]
				.onEach [ println(it) ]
			println(s)
		} catch(Exception e) {
			e.printStackTrace
			fail('onError should have caught ' + e)
		}
	}

	@Test
	def void testHandlingAboveErrorShouldTriggerException() {
		try {
			val s = int.stream
			s
				.map [ it ]
				.onError [ fail('should not trigger') ]
				.filter [ 1 / (it % 2) == 0 ] // division by 0 for 2
				.map [ it ]
				.onEach [ println(it) ]
			s << 1 << 2 << finish
			fail('we expected an error for /0')
		} catch(Exception e) {
			// success
		}
	}

	@Atomic Throwable caught

	@Test
	def void testHandlingBelowErrorShouldFilterTheException() {
		try {
			val s = int.stream
			s
				.map [ it ]
				.filter [ 1 / (it % 2) == 0 ] // division by 0 for 2
				.map [ it ]
				.onError [ caught = it ] // below the filter method, above the oneach, will filter out the error
				.onEach [ println(it) ]
			s << 1 << 2 << finish
		} catch(Exception e) {
			fail('error should be handled')
		}
		assertNotNull(caught)
	}

	@Test
	def void testHandlingAtTaskShouldFilterTheException() {
		try {
			val s = int.stream
			s
				.map [ it ]
				.filter [ 1 / (it % 2) == 0 ] // division by 0 for 2
				.map [ it ]
				.onEach [ println(it) ]
				.onError [ caught = it ] // below onEach, listening to the created task
			s << 1 << 2 << finish
		} catch(Exception e) {
			e.printStackTrace
			fail('error should be handled')
		}
		assertNotNull(caught)
	}
	
	@Atomic boolean finished
	@Atomic int errorCount
	@Atomic int count
	
	@Test
	def void testErrorHandlingBeforeCollect() {
		finished = false
		errorCount = 0
		count = 0
		val s = (1..10).stream
		s
			.map [ it % 3 ]
			.map [ 100 / it ] // division by 10 for s = 3, 6, 9
			.onError [ incErrorCount ] 
			.onEach [ incCount ]
			// onError is consumed AFTER the aggregation, so we only get a task with a single error and no finish
			.then [ finished = true ]
			
		assertEquals(7, count) // 3, 6 and 9 had an error, so 10-3 were successful
		assertEquals(3, errorCount) // because of the 10
		assertTrue(finished) // end of iteration
	}
	
	@Test
	def void testErrorHandlingAfterCollect() {
		finished = false
		errorCount = 0
		count = 0
		val s = #[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].stream
		val s2 = s
			.map [ it % 3 ]
			.map [ 100 / it ] // division by 10 for s = 3, 6, 9
			// s2.on [ each [ incCount s2.next ] ]
		s2
			.on [ stream, it | each [ incCount stream.next ] ]
			.onError [ incErrorCount ] 
			.then [ finished = true ]
		s2.next
			// onError is consumed AFTER the aggregation, so we only get a task with a single error and no finish
			// in other words, putting onError after onEach means break stream on error
			// however, this solution only works asynchronously! on a single threaded solution the .on would finish
			// processing all data before the .onError is even called.
			
		assertEquals(7, count) // only two values passed, and then we got the 3
		assertFalse(finished) // we did not catch the error in the loop, so the loop has an error and did not finish
		assertEquals(1, errorCount) // only one error because after the onEach task
	}

}