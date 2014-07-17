package nl.kii.promise;

/**
 * Thrown when some error occurred during a promise
 */
@SuppressWarnings("all")
public class PromiseException extends Exception {
  public PromiseException(final String msg) {
    super(msg);
  }
  
  public PromiseException(final String msg, final Exception e) {
    super(msg, e);
  }
}
