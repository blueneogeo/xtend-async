package nl.kii.stream;

import nl.kii.stream.StreamException;

/**
 * This is a StreamException that was not caught using a stream.onError handler,
 * and that should break out of the stream chain so surrounding code can catch
 * it, or it can be presented in the program output for debugging.
 */
@SuppressWarnings("all")
public class UncaughtStreamException extends StreamException {
  /**
   * convert a streamexception into an uncaught streamexception
   */
  public UncaughtStreamException(final StreamException it) {
    super(it.operation, it.value, it.getCause());
  }
  
  public UncaughtStreamException(final String operation, final Object value, final Throwable cause) {
    super(operation, value, cause);
  }
}
