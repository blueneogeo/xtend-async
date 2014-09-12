package nl.kii.stream
import static extension com.google.common.base.Throwables.*
/**
 * An exception that occurred in a stream handler, somewhere in the chain of stream operations.
 * <p>
 * Since stream processing is a bunch of messages going up and down the chain, stream errors
 * can be notoriously hard to debug, since the stacktrace becomes a huge mess in which
 * somewhere there is the actual cause. Because of this, all Stream operations store their
 * name on the operation field of the stream, and when caught, the current value being processed
 * and the operation of the stream are passed into the StreamException. When displayed, this
 * exception will try to show you the operation that was being performed, the value that was
 * being processed, and the root cause and location of that root cause, like this:
 * <p>
 * <pre>
 * nl.kii.stream.StreamException: Stream.observe gave error "ack!" for value "3"
 * at nl.kii.stream.test.TestStreamObserving$1.onValue(TestStreamObserving.java:23)
 * </pre>
 */
class StreamException extends Exception {
	
	val static int valueWrapSize = 10
	val static int traceSize = 1
	
	public val String operation
	public val Object value
	
	new(String operation, Object value, Throwable cause) {
		super(cause)
		this.operation = operation
		this.value = value
	}
	
	override getMessage() {
		val root = cause.rootCause
		'''
		Stream.«operation»«IF root!=null && root.message != null» gave error "«root.message»«ENDIF»"«IF value!=null && value.toString.length < valueWrapSize» for value: "«value»"«ENDIF»
		«IF value!=null && value.toString.length >= valueWrapSize»For value: { «value» }«ENDIF»
		«IF cause!=null»
		«FOR e : root.stackTrace.take(traceSize)»
			at «e.className».«e.methodName»(«e.fileName»:«e.lineNumber»)
		«ENDFOR»
		«ENDIF»'''
	}
}

/** 
 * This is a StreamException that was not caught using a stream.onError handler,
 * and that should break out of the stream chain so surrounding code can catch
 * it, or it can be presented in the program output for debugging.
 */
class UncaughtStreamException extends StreamException {

	/** convert a streamexception into an uncaught streamexception */	
	new(StreamException it) {
		super(operation, value, cause)
	}
	
	new(String operation, Object value, Throwable cause) {
		super(operation, value, cause)
	}
	
}
