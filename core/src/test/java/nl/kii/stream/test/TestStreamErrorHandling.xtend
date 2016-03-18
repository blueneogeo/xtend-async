package nl.kii.stream.test

import nl.kii.async.annotation.Atomic
import nl.kii.stream.Stream
import org.junit.Test

import static nl.kii.stream.test.StreamAssert.*
import static org.junit.Assert.*

import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class TestStreamErrorHandling {

	@Atomic int result
	@Atomic int counter
	@Atomic int errors
	@Atomic boolean complete
	@Atomic boolean failed


	@Test
	def void canMonitorErrors() {
		val s = new Stream<Integer>
		s
			.map [ it / 0 ]
			.on(Throwable) [ errors = errors + 1 ]
			.effect [ fail('an error should occur') ]
			.start
		s << 1 << 2 << 3
		assertEquals(3, errors)
	}

	@Test
	def void canMatchErrorTypes() {
		val s = new Stream<Integer>
		s
			.map [ it / 0 ]
			.on(IllegalArgumentException) [ failed = true ]
			.on(ArithmeticException) [ errors = errors + 1 ]
			.effect [ fail('an error should occur') ]
			.start
		s << 1 << 2 << 3
		assertEquals(false, failed)
		assertEquals(3, errors)
	}
	
	@Test
	def void canSwallowErrorTypes() {
		val s = new Stream<Integer>
		s
			.map [ it / 0 ]
			.on(IllegalArgumentException) [ failed = true ]
			.on(ArithmeticException) [ errors = errors + 1 ]
			.effect(Exception) [ errors = errors + 1 ]
			.on(Throwable) [ failed = true ]
			.effect [ fail('an error should occur') ]
			.start
		s << 1 << 2 << 3
		assertEquals(false, failed)
		assertEquals(6, errors)
	}

	@Test
	def void canMapErrors() {
		val s = new Stream<Integer>
		s
			.map [ it / 0 ]
			.on(IllegalArgumentException) [ failed = true ]
			.map(ArithmeticException) [ 10 ]
			.on(Throwable) [ failed = true ]
			.effect [ result = result + it ]
			.start
		s << 1 << 2 << 3
		assertEquals(false, failed)
		assertEquals(30, result) // 3 results, each coverted to 10, added together
	}

	@Test
	def void canFilterMapErrors() {
		val s = new Stream<Integer>
		s
			.map [ it / 0 ]
			.map(IllegalArgumentException) [ 1 ] // should not match, not the correct error type
			.map(ArithmeticException) [ 10 ] // should match, convert error to 20
			.map(Throwable) [ 100 ] // never gets triggered, map above filtered it out
			.effect [ result = result + it ]
			.start
		s << 1 << 2 << 3
		assertEquals(false, failed)
		assertEquals(30, result) // 3 results, each coverted to 20, added together
	}

	@Test
	def void canFilterAsyncMapErrors() {
		val s = new Stream<Integer>
		s
			.map [ it / 0 ]
			.call(IllegalArgumentException) [ 10.promise ] // should not match, not the correct error type
			.call(ArithmeticException) [ 20.promise ] // should match, convert error to 20
			.call(Throwable) [ 30.promise ] // never gets triggered, map above filtered it out
			.effect [ result = result + it ]
			.start
		s << 1 << 2 << 3
		assertEquals(false, failed)
		assertEquals(60, result) // 3 results, each converted to 20, added together
	}

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
			.effect [ incCounter ]
			.start
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
			.effect(Exception) [ incErrors ]
			.map [ it ]
			.effect [ incCounter ]
			.start
			.then [ complete = true ]
			.on(Throwable) [ failed = true ]
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
			.on(Exception, true) [ println('error ' + it) ]
			.effect [ println('result : ' + it) ]
			.start
			.then [ println('done') ]
			.on(Throwable) [ fail(message) ]
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
			.on(Exception, true) [ println('error ' + it) ]
			.effect [ println('result : ' + it) ]
			.start
			.then [ println('done') ]
			.on(Throwable) [ fail(message) ]
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
			.on(Exception, true) [ println('error ' + it) ]
			.effect [ println('result : ' + it) ]
			.start
			.then [ println('done') ]
			.on(Throwable) [ 
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
				.on(Exception) [ caught = it ] // below the filter method, above the oneach, will filter out the error
				.start
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
				.start
				.on(Throwable) [ caught = it ] // below onEach, listening to the created task
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
			.effect(Exception) [ incErrorCount ] 
			.effect [ incCount ]
			.start
			// onError is consumed AFTER the aggregation, so we only get a task with a single error and no finish
			.then [ finished = true ]
			
		assertEquals(7, count) // 3, 6 and 9 had an error, so 10-3 were successful
		assertEquals(3, errorCount) // because of the 10
		assertTrue(finished) // end of iteration
	}
	
	@Test
	def void correctlyHandlesNullEntries() {
		val stream = stream(Integer)
		finished = false
		stream << 5

		stream
			.map [ null ]
			.on(Throwable) [ finished = true ]
			.effect [ finished = false ]
			.start

		assertTrue(finished)
	}
	
}