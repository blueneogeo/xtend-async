package nl.kii.stream.internal;

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
public class StreamException /* implements Exception  */{
  private final static int valueWrapSize = 10;
  
  private final static int traceSize = 1;
  
  private final static int maxValueStringLength = 500;
  
  public final /* String */Object operation;
  
  public final /* Object */Object value;
  
  public StreamException(final /* String */Object operation, final /* Object */Object value, final /* Throwable */Object cause) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method super is undefined for the type StreamException");
  }
  
  public static java.lang.Object getMessage(final StreamException e) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method cause is undefined for the type StreamException"
      + "\nThe method cause is undefined for the type StreamException"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved");
  }
  
  public java.lang.CharSequence getMessage() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field cause is undefined for the type StreamException"
      + "\nThe method or field cause is undefined for the type StreamException"
      + "\nrootCause cannot be resolved"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved"
      + "\nmessage cannot be resolved"
      + "\n!= cannot be resolved"
      + "\nmessage cannot be resolved"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved"
      + "\ntoString cannot be resolved"
      + "\nlength cannot be resolved"
      + "\n< cannot be resolved"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved"
      + "\ntoString cannot be resolved"
      + "\nlength cannot be resolved"
      + "\n>= cannot be resolved"
      + "\ntoString cannot be resolved"
      + "\nlimit cannot be resolved"
      + "\n!= cannot be resolved"
      + "\nstackTrace cannot be resolved"
      + "\ntake cannot be resolved"
      + "\nclassName cannot be resolved"
      + "\nmethodName cannot be resolved"
      + "\nfileName cannot be resolved"
      + "\nlineNumber cannot be resolved");
  }
  
  public static String limit(final /* String */Object string, final int maxLength) {
    throw new Error("Unresolved compilation problems:"
      + "\n== cannot be resolved"
      + "\n|| cannot be resolved"
      + "\nlength cannot be resolved"
      + "\n<= cannot be resolved"
      + "\nsubstring cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nlength cannot be resolved"
      + "\n- cannot be resolved"
      + "\n+ cannot be resolved");
  }
}
