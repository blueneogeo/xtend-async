package nl.kii.stream.test
import static org.junit.Assert.*
import nl.kii.async.annotation.Atomic
import org.junit.Test

import static extension nl.kii.stream.StreamExtensions.*

class TestStreamErrorHandling {

	@Atomic int counter
	@Atomic int errors
	@Atomic boolean complete
	@Atomic boolean failed

	@Test
	def void testStreamsSwallowExceptions() {
		val s = int.stream
		s
			.map [ it ]
			.map [
				if(it == 2 || it == 4) throw new Exception
				it
			]
			.map [ it ]
			.onEach [ incCounter ]
			.then [ complete = true ]
		s << 1 << 2 << 3 << 4 << 5 finish
		// gets here without an error, and there were two errors, for 2 and 4
		assertEquals(3, counter)
		// the whole operation did not complete without errors
		assertFalse(complete)
	}

	@Test
	def void testStreamsErrorHandlersSwallowExceptions() {
		val s = int.stream
		s
			.map [ it ]
			.map [
				if(it == 2 || it == 4) throw new Exception
				it
			]
			.onError [ incErrors ]
			.map [ it ]
			.onEach [ incCounter ]
			.then [ complete = true ]
			.onError [ failed = true ]
		s << 1 << 2 << 3 << 4 << 5 << finish
		// there were two errors
		assertEquals(2, errors)
		// gets here without an error, and there were two errors, for 2 and 4
		assertEquals(3, counter)
		// the whole operation now does complete, since the error was swallowed
		assertTrue(complete)
		assertFalse(failed)
	}

	@Test
	def void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream() {
		(1..20).stream
			.split [ it % 4 == 0 ]
			.map [
				if(it % 6 == 0) throw new Exception
				it
			]
			.collect
			.onError [ println('error ' + it) ]
			.onEach [ println('result : ' + it) ]
			.then [ println('done') ]
			.onError [ fail(message) ]
	}

	@Test
	def void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream2() {
		val s = int.stream << 1 << 2 << 3 << 4 << 5 << 6 << 7 << 8 << 9 << 10 << finish(0) << finish(1)

		s
			.split [ it % 4 == 0 ]
			.map [
				if(it % 6 == 0) throw new Exception
				it
			]
			.collect
			.onError [ println('error ' + it) ]
			.onEach [ println('result : ' + it) ]
			.then [ println('done') ]
			.onError [ fail(message) ]
	}

	@Test
	def void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream3() {
		val s = int.stream << 1 << 2 << 3 << 4 << finish(0) << 5 << 6 << 7 << 8 << finish(0) << 9 << 10 << finish(0) << finish(1)
			//.split [ it % 4 == 0 ]
		s
			.map [
				if(it % 6 == 0) throw new Exception
				it
			]
			.collect
			.onError [ println('error ' + it) ]
			.onEach [ println('result : ' + it) ]
			.then [ println('done') ]
			.onError [ 
				println('error')
				fail(message)
			]
	}

	@Test
	def void testSplit() {
		(1..10).stream.split [ it % 4 == 0 ]
			.on [ 
				each [ println($1) stream.next ]
				finish [ println('finish ' + $1) stream.next ]
				stream.next
			]
			
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
				.onEach [ ]
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
				.onEach [ ]
				.onError [ caught = it ] // below onEach, listening to the created task
			s << 1 << 2 << finish
		} catch(Exception e) {
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
	
}