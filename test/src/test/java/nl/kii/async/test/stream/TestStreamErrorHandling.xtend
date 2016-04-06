package nl.kii.async.test.stream

import nl.kii.async.annotation.Atomic
import nl.kii.stream.Stream
import org.junit.Test

import static extension nl.kii.async.test.AsyncJUnitExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.util.JUnitExtensions.*
import nl.kii.async.AsyncException

class TestStreamErrorHandling {

	@Atomic int result
	@Atomic int counter
	@Atomic int errors
	@Atomic boolean complete
	@Atomic boolean failed

	@Test
	def void streamsErrorsDoNotBreakTheStream() {
		(1..3).stream
			.map [ it / 0 ]
			.on(Throwable) [ errors = errors + 1 println(message) ]
			.start
			.on(Throwable) [ errors = errors + 1 ]
		errors <=> 4 // three from listening inside the stream, one from listening at the tail
	}
	
	@Test(expected=AsyncException)
	def void canBreakOutOfStreamWithAsyncException() {
		(1..3).stream
			.effect [ throw new AsyncException('time to stop streaming!') ]
			.start
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
		failed <=> false
		errors <=> 3
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
			.effect [ throw new AsyncException('an error should occur') ]
			.start
		s << 1 << 2 << 3
		failed <=> false
		errors <=> 6
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
		failed <=> false
		result <=> 30 // 3 results, each coverted to 10, added together
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
		failed <=> false
		result <=> 30 // 3 results, each coverted to 20, added together
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
		failed <=> false
		result <=> 60 // 3 results, each converted to 20, added together
	}

	@Test
	def void testStreamsSwallowExceptions() {
		(1..5).stream
			.map [ it ]
			.map [
				if(it == 2 || it == 4) throw new Exception
				it
			]
			.map [ it ]
			.effect [ incCounter ]
			.start
			.then [ complete = true ]
		// gets here without an error, and there were two errors, for 2 and 4
		counter <=> 3
		// the whole operation did not complete without errors
		complete <=> false
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
		s << 1 << 2 << 3 << 4 << 5 << close
		// there were two errors
		errors <=> 2
		// gets here without an error, and there were two errors, for 2 and 4
		counter <=> 3
		// the whole operation now does complete, since the error was swallowed
		complete <=> true
		failed <=> false
	}

	@Test(expected=Exception)
	def void testAnyErrorInAStreamFailsTheAggregation() {
		(1..5).stream
			.map [ if(it % 2 == 0) throw new Exception else it ]
			.count <=> 5
	}

	@Test
	def void testErrorMapping() {
		(1..5).stream
			.map [ if(it % 2 == 0) throw new Exception else it ]
			.map(Exception) [ -1 ] // transforms the error back to a value, in this case -1
			.count <=> 5
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
			s << 1 << 2 << close
		} catch(Exception e) {
			fail('error should be handled')
		}
		(caught != null) <=> true
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
			s << 1 << 2 << close
		} catch(Exception e) {
			fail('error should be handled')
		}
		(caught != null) <=> true
	}
	
	@Test
	def void testErrorHandlingBeforeCollect() {
		val s = (1..10).stream
		s
			.map [ it % 3 ]
			.map [ 100 / it ] // division by 10 for s = 3, 6, 9
			.effect(Exception) [ incErrors ] 
			.effect [ incCounter ]
			.start
			// onError is consumed AFTER the aggregation, so we only get a task with a single error and no finish
			.then [ complete = true ]

		counter <=> 7
		errors <=> 3
		complete <=> true			
	}
	
	@Test
	def void correctlyHandlesNullEntries() {
		val stream = int.stream
		complete = false
		stream << 5

		stream
			.map [ null ]
			.on(Throwable) [ complete = true ]
			.effect [ complete = false ]
			.start

		complete <=> true
	}
	
	@Test
	def void collapsesStacktraces() {
		try {
		(1..10_000).stream
			.map [ it % 9_000 ]
			.map [ 100 / it ] // division by 0 for 9000
			.start
			// onError is consumed AFTER the aggregation, so we only get a task with a single error and no finish
			.asFuture.get
		} catch(Exception e) {
			// All actor references are filtered out! And we have a nicer stacktrace
			val error = e.cause.stackTrace.findFirst [ trace | trace.className.contains('actor') ]
			(error == null) <=> true
		}
		
	}
	
}
