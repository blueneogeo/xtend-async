package nl.kii.async.util

class AsyncUtils {

	val public static unwantedStacktraces = #[
		'nl.kii.act.*',
		'nl.kii.observe.*',
		'nl.kii.stream.BaseStream.*',
		'nl.kii.stream.StreamEventHandler.*',
		'nl.kii.stream.StreamEventResponder.*',
		'nl.kii.stream.StreamResponder.*',
		'nl.kii.stream.StreamObserver.*'
		// 'nl.kii.stream.+',
		// 'nl.kii.promise.+',
	]

	static def void cleanStackTrace(Throwable t) {
		t.stackTrace = t.stackTrace.cleanStackTraceElements
		if(t.cause != null) t.cause.stackTrace.cleanStackTraceElements // recursive
	}
	
	static def StackTraceElement[] cleanStackTraceElements(StackTraceElement[] stack) {
		stack.filter [ !isUnwanted && lineNumber != 1 ]
	}
	
	static def isUnwanted(StackTraceElement trace) {
		unwantedStacktraces.findFirst [ trace.className.matches(it)] != null
	}
	
}
